package mapred;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Text;

public class StringArrayWritable extends ArrayWritable {
	public StringArrayWritable() {
		super(Text.class);
	}
	
	protected static Text[] getTexts( String[] items ) {
		Text[] texts = new Text[items.length];
		for ( int i = 0; i < items.length; i++ ) {
			texts[ i ] = new Text( items[ i ] );
		}
		return texts;
	}
	
	public StringArrayWritable( String[] items ) {
		super( Text.class, getTexts( items ) );
	}
}
