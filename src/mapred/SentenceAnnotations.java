package mapred;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.io.Text;

import sf.retriever.CorefProvider;
import util.StringUtil;

public class SentenceAnnotations extends HashMap<String, String> {
	public static Map<String, Integer> sizes;
	public static String[] ordering = {
		"meta",
		"text",
		"tokens",
		"stanfordlemma",
		"tokenSpans",
		"stanfordpos",
		"cj",
		"depsStanfordCCProcessed",
		"depsStanfordCCProcessed2",
		"stanfordner",
		"articleIDs"
	};
	
	static {
		sizes = new TreeMap<String, Integer>();
		sizes.put( "meta", 4 );
		sizes.put( "text", 1 );
		sizes.put( "tokens", 1 );
		sizes.put( "stanfordlemma", 1 );
		sizes.put( "tokenSpans", 1 );
		sizes.put( "stanfordpos", 1 );
		sizes.put( "cj", 1 );
		sizes.put( "depsStanfordCCProcessed", 1 );
		sizes.put( "depsStanfordCCProcessed2", 1 );
		sizes.put( "stanfordner", 1 );
		sizes.put( "articleIDs", 1 );
	}
	
	public long sentenceId, articleId;
	
	public SentenceAnnotations() {}
	
	public SentenceAnnotations( String str ) {
		this( str.split("\t") );
	}
	
	public SentenceAnnotations( Text text ) {
		this( text.toString() );
	}
	
	public SentenceAnnotations( String[] packed ) {
		// Read sentence ID
		sentenceId = Long.parseLong( packed[0] );
		
		// Read out all the packed fields
		int idx = 1;
		for ( String title : ordering ) {
			int size = sizes.get( title );
			put( title,
					StringUtil.join( packed, "\t", idx, size ) );
			idx += size;
		}
		
		// Read wikification as the last item, since it has varying size.
		if ( idx + 1 < packed.length ) {
			int wikiSize = Integer.parseInt( packed[idx] );
			put( "wikification",
					StringUtil.join( packed, "\t", idx + 1, wikiSize ) );
		}
	}
	
	public CorefProvider getSentenceCoref() {
		// TODO: finish
		return null;
	}
	
	// Repack the data
	public String[] pack( boolean useSentenceId ) {
		List<String> result = new ArrayList<String>();
		
		if ( useSentenceId )
			result.add( "" + sentenceId );
		
		// Read out all the packed fields
		for ( String title : ordering ) {
			int size = sizes.get( title );
			String value = get( title );
			String[] tokens;
			if ( value != null ) {
				tokens = value.split("\t");
				if ( tokens.length != size ) {
					int oldSize = tokens.length;
					tokens = Arrays.copyOf( tokens, size );
					for ( int i = oldSize; i < size; i++ )
						tokens[i] = "";
				}
				result.addAll( Arrays.asList( tokens ) );
			} else {
				for ( int i = 0; i < size; i++ )
					result.add( "" );
			}
		}
		String wiki = get( "wikification" );
		if ( wiki != null ) {
			String[] tokens = wiki.split("\t");
			result.add( tokens.length + "" );
			result.addAll( Arrays.asList( tokens ) );
		}
		
		return result.toArray(new String[0]);
	}
	
	public String toString( boolean useSentenceId ) {
		return StringUtil.join( pack( useSentenceId ), "\t" );
	}
	
	public Text toText( boolean useSentenceId ) {
		return new Text( toString( useSentenceId ) );
	}
}
