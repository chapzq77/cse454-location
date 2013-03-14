package mapred;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
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
import sf.query.QueryReader;

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
			FileSystem fs = FileSystem.get( conf );
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
		
		@Override
		public void setup( Context context ) {
			Configuration conf = context.getConfiguration();
			
			// Read in the queries
			queryReader = readQueries( conf );
			
			// Construct fillers
			String[] fillerTitles = conf.get("fillers").split(",");
			fillers = new Filler[ fillerTitles.length ];
			int i = 0;
			for ( String title : fillerTitles ) {
				try {
					fillers[ i++ ] = (Filler) Class.forName( title ).newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
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
						Text ans = new Text( answer.count + "\t" + answer.doc +
								"\t" + answer.answer );
						context.write( id, ans );
					}
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
			for (SFEntity query : queryReader.queryList) {
				for ( String slot : SFConstants.slotNames ) {
					if ( query.ignoredSlots.contains( slot ) ) continue;
					
					List<SingleAnswer> answers = query.answers.get( slot );
					Text result;
					if ( answers != null && answers.size() > 0 ) {
						// Find highest value
						SingleAnswer ans = answers.get(0);
						for (SingleAnswer a : answers ) {
							if (a.count > ans.count) // choose answer with highest count
								ans = a;
						}
						// Emit answer and related document
						result = new Text( "Hadoop\t" + ans.doc + "\t" +
								ans.answer ); 
					} else {
						// Emit NIL
						result = new Text( "Hadoop\tNIL\t" );
					}
					context.write( key, result );
				}
			}
	    }
	}
	
	public static void main( String[] args ) throws Exception {
		if ( args.length < 4 ) {
			System.err.println(
					"Args: input-dir output-dir queries-file fillers-file");
			return;
		}
		
	    Configuration conf = new Configuration();
	    conf.set( "queries", new Path(args[2]).toString() );
	    conf.set( "fillers", new Path(args[3]).toString() );
	
	    Job job = new Job(conf, "wordcount");
	    job.setJarByClass(SentenceGrouper.class);
	
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	
	    job.setMapperClass(Map.class);
	    job.setReducerClass(Reduce.class);
	
	    job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	
	    job.waitForCompletion(true);
	}
}
