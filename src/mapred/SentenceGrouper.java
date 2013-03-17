package mapred;

import java.io.IOException;
import java.util.*;
import java.util.regex.*;

import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import util.StringUtil;

/**
 * Groups all of the data from the sentence files into a single line.
 * @author Jeffrey Booth
 */
public class SentenceGrouper {
	/**
	 * Emits sentences by ID, and includes their source.
	 */
	public static class Map extends
		Mapper<LongWritable, Text, LongWritable, StringArrayWritable> {
		
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// Determine type
			FileSplit split = (FileSplit)context.getInputSplit();
			String type = split.getPath().getName().split("\\.")[1];
			
			// Get file data
			String line = value.toString();
			String[] tokens = line.split("\t", 2);
			if ( tokens.length < 2 ) return;
			long sentenceId = Long.parseLong( tokens[0] );
			String contents = tokens[1];
			
			String[] pair = new String[] { type, contents };
			context.write( new LongWritable( sentenceId ),
					new StringArrayWritable( pair ) );
	    }
	}
	
	/**
	 * Groups sentence data together into a single line.
	 */
	public static class Reduce extends
		Reducer<LongWritable, StringArrayWritable, LongWritable, Text> {
		
		@Override
		public void reduce(LongWritable key, Iterable<StringArrayWritable> values, Context context) 
			      throws IOException, InterruptedException {
			SentenceAnnotations annotations = new SentenceAnnotations();
					
			// Store tokens in map
			for ( ArrayWritable value : values ) {
				String[] tokensWithId = value.toStrings();
				annotations.put( tokensWithId[0], tokensWithId[1] );
			}
			
			context.write(key, annotations.toText( false, false ) );
	    }
	}
	
	public static void main( String[] args ) throws Exception {
	    Configuration conf = new Configuration();

	    Job job = new Job(conf, "SentenceGrouper");
	    job.setJarByClass(SentenceGrouper.class);

	    job.setOutputKeyClass(LongWritable.class);
	    job.setMapOutputValueClass(StringArrayWritable.class);
	    job.setOutputValueClass(Text.class);

	    job.setMapperClass(SentenceGrouper.Map.class);
	    job.setReducerClass(SentenceGrouper.Reduce.class);

	    job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);

	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));

	    job.waitForCompletion(true);
	}
}
