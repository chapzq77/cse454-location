package mapred;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import sf.SFConstants;
import sf.SFEntity;
import sf.SFEntity.SingleAnswer;
import sf.filler.Filler;
import sf.filler.Filler.SimpleFileSystem;
import sf.filler.regex.RegexLocationFiller;
import sf.filler.tree.*;
import sf.query.QueryReader;
import sf.retriever.CorefMention;

/**
 * Takes lines of sentence information as input. Each mapper reads queries out
 * of HDFS, then runs the queries on each sentence and emits filled slots.
 * The reducer combines the slot results and emits the most popular result
 * for each slot.
 * 
 * @author Jeffrey Booth
 */
public class SlotFiller {
	public static QueryReader readQueries( Configuration conf ) {
		Path inFile = new Path( conf.get("queries") );
		FSDataInputStream in = null;
		try {
			FileSystem fs = FileSystem.get( inFile.toUri(), conf );
			in = fs.open( inFile );
			QueryReader qr = new QueryReader();
			qr.readFrom( in );
			return qr;
		} catch ( Exception ex ) {
			throw new RuntimeException( ex );
		} finally {
			if ( in != null ) {
				try {
					in.close();
				} catch (IOException e) {}
			}
		}
	}
	
	public static class Map extends
	Mapper<LongWritable, Text, Text, Text> {
		protected QueryReader queryReader;
		protected Filler[] fillers;
		
		public static class Answer {
			String query;
			String slot;
			String answer;
			
			public Answer(String query, String property, String answer) {
				this.query = query;
				this.slot = property;
				this.answer = answer;
			}
			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((answer == null) ? 0 : answer.hashCode());
				result = prime * result
						+ ((slot == null) ? 0 : slot.hashCode());
				result = prime * result
						+ ((query == null) ? 0 : query.hashCode());
				return result;
			}
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Answer other = (Answer) obj;
				if (answer == null) {
					if (other.answer != null)
						return false;
				} else if (!answer.equals(other.answer))
					return false;
				if (slot == null) {
					if (other.slot != null)
						return false;
				} else if (!slot.equals(other.slot))
					return false;
				if (query == null) {
					if (other.query != null)
						return false;
				} else if (!query.equals(other.query))
					return false;
				return true;
			}
			@Override
			public String toString() {
				return "Answer [query=" + query + ", slot=" + slot
						+ ", answer=" + answer + "]";
			}
		}
		protected static Set<Answer> badAnswers;
		static {
			badAnswers = new HashSet<Answer>();
			badAnswers.addAll( Arrays.asList( new Answer[] {
				//new Answer( "SF130", "org:city_of_headquarters", "GLSEN" ),
				//new Answer( "SF136", "org:stateorprovince_of_headquarters", "New York" ),
				new Answer( "SF141", "org:city_of_headquarters", "Richmond" ),
				new Answer( "SF141", "org:stateorprovince_of_headquarters", "West Virginia" )
				//new Answer( "SF148", "org:country_of_headquarters", "Canada" ),
			}));
		}
		
		@Override
		public void setup( Context context ) {
			final Configuration conf = context.getConfiguration();
			
			// Set up a way for fillers to obtain the datafiles.
			// TODO: don't set global variables to do this...
			Filler.fs = new SimpleFileSystem() {
				@Override
				public BufferedReader open(String file) throws Exception {
					Path inFile = new Path("s3n://cse454-location/" + file);
					FileSystem fs = FileSystem.get( inFile.toUri(), conf );
					return new BufferedReader( new InputStreamReader(
							fs.open( inFile ) ) );
				}
			};
			
			// Read in the queries
			queryReader = readQueries( conf );
			
			// Construct fillers
			String[] fillerTitles = conf.get("fillers").split(",");
			List<Filler> fillersList = new ArrayList<Filler>();
			int i = 0;
			for ( String title : fillerTitles ) {
				if ( title.equals("reg") ) {
					fillersList.add( new RegexLocationFiller() );
				} else if ( title.equals("tree") ) {
					fillersList.add( new TregexOrgPlaceOfHeadquartersFiller() );
					fillersList.add( new TregexPerPlaceOfBirthFiller() );
					fillersList.add( new TregexPerPlaceOfDeathFiller() );
					fillersList.add( new TregexPerPlacesOfResidenceFiller() );
				}
			}
			fillers = fillersList.toArray( new Filler[0] );
		}
		
		public static void printState( Answer answer, java.util.Map<String, String> annots,
				Collection<CorefMention> mentions ) {
			System.out.println("========== Query " + answer.query + ", Slot "
					+ answer.slot + " ==========");
			System.out.println("  Answer: " + answer.answer );
			System.out.println("  ---------- Annotations ----------");
			for ( java.util.Map.Entry<String, String> annot :
				annots.entrySet() ) {
				System.out.println("  " + annot.getKey() + ": "
						+ annot.getValue());
			}
			System.out.println("  ---------- Coreference ----------");
			for ( CorefMention mention : mentions ) {
				System.out.println( "  " + mention );
			}
			System.out.println(
				"============================================================");
		}
		
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			SentenceAnnotations annotations = new SentenceAnnotations( value );
			
			// For each query and filler, attempt to fill the slot.
			for (SFEntity query : queryReader.queryList) {
				query.answers.clear();
				
				for ( Filler filler : fillers ) {
					filler.predict( query, annotations,
							annotations.getSentenceCoref() );
				}
				
				// Emit all the results.
				for ( java.util.Map.Entry<String, List<SFEntity.SingleAnswer> >
					entry : query.answers.entrySet() ) {
					Text id = new Text( query.queryId + "\t" + entry.getKey() );
					for ( SFEntity.SingleAnswer answer : entry.getValue() ) {
						String ans = answer.count + "\t" + answer.doc +
								"\t" + answer.answer;
						context.write( id, new Text( ans ) );
						//Answer a = new Answer( query.queryId, entry.getKey(),
						//		answer.answer );
						//if ( badAnswers.contains(a) ) {
						//	printState( a, annotations, annotations.getSentenceCoref().all() );
						//}
					}
					// TODO: emit NILs for unanswered questions
				}
			}
	    }
	}
	
	public static class Reduce extends
		Reducer<Text, Text, Text, Text> {
		
		QueryReader queryReader;
		
		@Override
		public void setup(Context context) {
			// Read in the queries
			queryReader = readQueries( context.getConfiguration() );
		}
		
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) 
			      throws IOException, InterruptedException {
			// The output file format
			// Column 1: query id
			// Column 2: the slot name
			// Column 3: a unique run id for the submission
			// Column 4: NIL, if the system believes no
			// information is learnable for this slot. Or, 
			// a single docid which supports the slot value
			// Column 5: a slot value
			
			// for each query, print out the answer, or NIL if nothing is found

			// Find highest value
			SingleAnswer ans = null;
			for ( Text value : values ) {
				String[] data = value.toString().split("\t", 3);
				SingleAnswer testAns = new SingleAnswer();
				testAns.count = Integer.parseInt( data[0] );
				testAns.doc = data[1];
				testAns.answer = data[2];

				// Choose answer with highest count
				if (ans == null || testAns.count > ans.count)
					ans = testAns;
			}
			// Emit answer and related document
			Text result = new Text( "Hadoop\t" + ans.doc + "\t" + ans.answer );
			context.write( key, result );
	    }
	}
	
	public static void main( String[] args ) throws Exception {
		if ( args.length < 4 ) {
			System.err.println(
					"Args: input-dir output-dir queries-file fillers-to-use");
			return;
		}
		
	    Configuration conf = new Configuration();
	    conf.set( "queries", new Path(args[2]).toString() );
	    conf.set( "fillers", new Path(args[3]).toString() );
	    
	    // Add the plain text files to the distributed cache, so the mappers can
	    // download them.
	    // (from https://forums.aws.amazon.com/message.jspa?messageID=152538)
	    /*String[] paths = new String[] {
	    	"states", "stateAbbrevs", "countries", "provinces"
	    };
	    for ( String path : paths ) {
	    	Path p = new Path("s3n://cse454-location/data/" + path + ".txt");
	    	DistributedCache.addCacheFile(p.toUri(), conf);
	    }*/
	
	    Job job = new Job(conf, "wordcount");
	    job.setJarByClass(SentenceGrouper.class);
	
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);
	
	    job.setMapperClass(Map.class);
	    job.setReducerClass(Reduce.class);
	
	    job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	
	    job.waitForCompletion(true);
	}
}
