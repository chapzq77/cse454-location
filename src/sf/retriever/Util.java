package sf.retriever;

import java.util.ArrayList;
import java.util.Collection;

public class Util {
	/**
	 * Parses the wiki entries in a line of the file sentences.wikification.
	 * @param line
	 * @return The wiki entries. 
	 */
	public static WikiEntry[] parseWiki( String line ) {
		if ( line == null || line.length() == 0 )
			return new WikiEntry[0];
		String[] entries = line.split("\t");
		WikiEntry[] result = new WikiEntry[entries.length - 1];
		for ( int i = 1; i < entries.length; i++ ) {
			String[] tokens = entries[i].split(" ");
			WikiEntry entry = new WikiEntry();
			if ( tokens.length > 0 ) entry.start       = Integer.parseInt( tokens[0] );
			if ( tokens.length > 1 ) entry.end         = Integer.parseInt( tokens[1] );
			if ( tokens.length > 2 ) entry.articleName = tokens[2];
			if ( tokens.length > 3 ) entry.confidence  = Double.parseDouble( tokens[3] );
			result[i - 1] = entry;
		}
		return result;
	}
	
	/**
	 * Parses the NER entries in a line of the file sentences.stanfordner.
	 * @param line
	 * @return The NER entries.
	 */
	public static NerType[] parseNer( String line ) {
		if ( line == null || line.length() == 0 )
			return new NerType[0];
		String[] entries = line.split("\t");
		if ( entries.length < 2 ) {
			return new NerType[0];
		}
		String[] ners = entries[1].split("\\s+");
		NerType[] result = new NerType[ ners.length ];
		for ( int i = 0; i < ners.length; i++ ) {
			result[i] = NerType.valueOf( ners[i] );
		}
		return result;
	}
}
