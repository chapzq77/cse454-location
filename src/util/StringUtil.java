package util;

public class StringUtil {
	public static String join( String[] strings, String delim ) {
		return join( strings, delim, 0, strings.length );
	}
	
	public static String join( String[] strings, String delim, int offset,
			int len ) {
		StringBuffer buf = new StringBuffer();
		for ( int i = offset; i < offset + len; i++ ) {
			String string = strings[i];
			if ( buf.length() > 0 ) buf.append( delim );
			buf.append( string );
		}
		return buf.toString();
	}
}
