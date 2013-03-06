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
		String[] entries = line.split("\t");
		WikiEntry[] result = new WikiEntry[entries.length - 1];
		for ( int i = 1; i < entries.length; i++ ) {
			String[] tokens = entries[i].split(" ");
			WikiEntry entry = new WikiEntry();
			entry.start       = Integer.parseInt( tokens[0] );
			entry.end         = Integer.parseInt( tokens[1] );
			entry.articleName = tokens[2];
			entry.confidence  = Double.parseDouble( tokens[3] );
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
		String[] entries = line.split("\t");
		String[] ners = entries[1].split("\\s+");
		NerType[] result = new NerType[ ners.length ];
		for ( int i = 0; i < ners.length; i++ ) {
			result[i] = NerType.valueOf( ners[i] );
		}
		return result;
	}
}
