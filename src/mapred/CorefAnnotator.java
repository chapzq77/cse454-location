package mapred;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import sf.SFConstants;
import sf.retriever.CorefEntity;
import sf.retriever.CorefMention;
import sf.retriever.NerType;
import sf.retriever.Util;
import sf.retriever.WikiEntry;

/**
 * Joins document coreference data with the sentences in the document.
 * In the reduce phase, wiki mentions and NER types for the mentioned entities
 * are computed, then the documents are split up into sentences again.
 * 
 * That way, the actual slot filling MapReduce job can operate on individual
 * sentences instead of documents.
 * 
 * @author Jeffrey Booth
 */
public class CorefAnnotator {
	public static class Map extends
	Mapper<LongWritable, Text, LongWritable, Text> {
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String valueStr = value.toString();
			
			// Get article ID
			String[] info = valueStr.split("\t", 2);
			long articleId = Long.parseLong( info[0] );
			
			// Determine type of data
			FileSplit split = (FileSplit)context.getInputSplit();
			boolean isCoref = split.getPath().getName().indexOf("coref") >= 0; 
			String type = isCoref ? "coref" : "article";
			
			// Send things along for annotation
			context.write( new LongWritable( articleId ),
					new Text( type + "\t" + info[1] ) );
	    }
	}
	
	public static class Reduce extends
		Reducer<LongWritable, Text, LongWritable, Text> {
		
		protected static class SentenceData {
			public SentenceAnnotations sa;
			public WikiEntry[] wikiEntries;
			public NerType[] nerEntries;
			public List<CorefMention> mentions;
			
			public SentenceData( String line ) {
				sa = new SentenceAnnotations( line.split("\t") );

				// Get wiki data
				String wikiLine = sa.get( SFConstants.WIKI );
				wikiEntries = Util.parseWiki( wikiLine );
				
				// Get NER data
				String nerLine = sa.get( SFConstants.STANFORDNER );
				nerEntries = Util.parseNer( nerLine );
			}
		}
		
		@Override
		public void reduce(LongWritable key, Iterable<Text> values, Context context) 
			      throws IOException, InterruptedException {
			// Load sentences and coref mentions
			java.util.Map<Long, SentenceData> sentences =
					new HashMap<Long, SentenceData>();
			List<CorefMention> mentions = new ArrayList<CorefMention>();
			java.util.Map<Long, CorefEntity> entitiesMap =
					new HashMap<Long, CorefEntity>();
			
			for ( Text value : values ) {
				String[] parts = value.toString().split("\t", 2);
				if ( parts[0].equals("article") ) {
					// Load all the sentences
					// TODO: better error handling...
					String data = parts[1];
					int start = data.indexOf("\t");
					int count = Integer.parseInt( data.substring( 0, start ) );
					start += 1;
					for ( int i = 0; i < count; i++ ) {
						int delim = data.indexOf("\t", start);
						int len = Integer.parseInt( data.substring( start, delim ) );
						start = delim + 1;
						String annot = data.substring( start, start + len );
						start += len + 1;
						
						SentenceData sd = new SentenceData( annot );
						sentences.put( sd.sa.sentenceId, sd );
					}
				} else {
					// Load the coref mention
					String[] tokens = (key + "\t" + parts[1]).split("\t");
					mentions.add( new CorefMention( tokens, entitiesMap ) );
				}
			}
			
			// For each coref entity, find the wiki and NER types
			for ( CorefEntity entity : entitiesMap.values() ) {
				java.util.Map<String, Double> possibleArticles =
						new HashMap<String, Double>();
				java.util.Map<NerType, Double> possibleNers =
						new HashMap<NerType, Double>();
				
				for ( CorefMention mention : entity.mentions ) {
					SentenceData sd = sentences.get( mention.sentenceId );
					if ( sd == null ) {
						System.err.println("Missing sentence " + mention.sentenceId);
					}
					sd.sa.mentions.add( mention );
					
					// See if wiki entries match this entity
					for ( WikiEntry entry : sd.wikiEntries ) {
						if ( !mention.overlaps( entry.start, entry.end ) )
							continue;
						Double voteObj = possibleArticles.get( entry.articleName );
						double vote = voteObj == null ? 0 : voteObj;
						possibleArticles.put( entry.articleName,
								vote + entry.confidence );
					}
					
					// Use the NER type of the "head" token in the sentence to
					// determine the entity type.
					if ( mention.head >= 0 && mention.head <
							sd.nerEntries.length ) {
						NerType headNer = sd.nerEntries[ mention.head ];
						Double voteObj = possibleNers.get( headNer );
						double vote = voteObj == null ? 0 : voteObj;
						possibleNers.put( headNer, vote + 1 );
					}
				}
				
				// Update wiki info.
				double bestScore = 0;
				for ( java.util.Map.Entry<String, Double> entry :
					possibleArticles.entrySet() ) {
					double score = entry.getValue();
					if ( score > bestScore ) {
						bestScore = score;
						entity.wikiId = entry.getKey();
						entity.fullName = entity.wikiId.replace('_', ' ');
					}
				}
				
				// Update NER info.
				bestScore = 0;
				for ( java.util.Map.Entry<NerType, Double> entry :
					possibleNers.entrySet() ) {
					double score = entry.getValue(); 
					if ( score > bestScore ) {
						bestScore = score;
						entity.nerType = entry.getKey();
					}
				}
			}
			
			// And, at last, output the sentence annotations using the
			// sentence ID as the key.
			for ( java.util.Map.Entry<Long, SentenceData> sentence :
				sentences.entrySet() ) {
				context.write( new LongWritable( sentence.getKey() ),
						sentence.getValue().sa.toText( false, true ) );
			}
	    }
	}
	
	public static void main( String[] args ) throws Exception {
	    Configuration conf = new Configuration();
	
	    Job job = new Job(conf, "wordcount");
	    job.setJarByClass(SentenceGrouper.class);
	
	    job.setOutputKeyClass(LongWritable.class);
	    job.setOutputValueClass(Text.class);
	
	    job.setMapperClass(Map.class);
	    job.setReducerClass(Reduce.class);
	
	    job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileInputFormat.addInputPath(job, new Path(args[1]));
	    FileOutputFormat.setOutputPath(job, new Path(args[2]));
	
	    job.waitForCompletion(true);
	}
}
