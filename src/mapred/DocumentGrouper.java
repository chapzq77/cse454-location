package mapred;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

/**
 * Groups the sentence data by document ID.
 * @author Jeffrey Booth
 */
public class DocumentGrouper {
	public static class Map extends
		Mapper<LongWritable, Text, LongWritable, Text> {
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// Unpack document tokens
			
			// TODO: need to fix the dirty data that causes this...
			String[] s = value.toString().split("\t", 2);
			if ( s.length < 2 || s[1].length() == 0 )
				return;
			
			SentenceAnnotations sa = new SentenceAnnotations( value );
			
			// Get article ID
			long articleId = Long.parseLong( sa.get( "articleIDs" ) );
			
			System.out.println("Mapping key " + key);
			
			context.write( new LongWritable( articleId ), value );
	    }
	}
	
	public static class Reduce extends
		Reducer<LongWritable, Text, LongWritable, Text> {
		@Override
		public void reduce(LongWritable key, Iterable<Text> values, Context context) 
			      throws IOException, InterruptedException {
			System.out.println("Reducing key " + key);
			
			// Concatenate documents
			List<String> sentences = new ArrayList<String>();
			for ( Text value : values ) {
				sentences.add( value.toString() );
			}
			
			// Output the document ID and the packed results
			// TODO: leave out article ID from sentence annotations
			StringBuffer buf = new StringBuffer();
			buf.append( sentences.size() + "" );
			for ( String s : sentences ) {
				buf.append( "\t" + s.length() + "\t" + s );
			}
			context.write( key, new Text( buf.toString() ) );
	    }
	}
	
	public static void main( String[] args ) throws Exception {
	    Configuration conf = new Configuration();

	    Job job = new Job(conf, "DocumentGrouper");
	    job.setJarByClass(SentenceGrouper.class);

	    job.setOutputKeyClass(LongWritable.class);
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
