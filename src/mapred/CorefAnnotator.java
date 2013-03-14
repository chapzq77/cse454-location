package mapred;

import java.io.IOException;

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
			String type = split.getPath().getName().split("\\.")[1];
			
			// Send things along for annotation
			context.write( new LongWritable( articleId ),
					new Text( type + "\t" + info[1] ) );
	    }
	}
	
	public static class Reduce extends
		Reducer<LongWritable, Text, LongWritable, Text> {
		public void reduce(Text key, Iterable<IntWritable> values, Context context) 
			      throws IOException, InterruptedException {
			// Match up sentences to coreference items
			
			// Iterate over sentences, looking at mentions for each sentence
			// to guess the wiki and NER types.
			
			// Unpack sentences from document group, and write each sentence
			// out with coreference annotations.
			// Each annotation contains:
			//  - all coreference mentions for the sentence. The mentions
			//    include 
	    }
	}
	
	public static void main( String[] args ) throws Exception {
	    Configuration conf = new Configuration();
	
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
