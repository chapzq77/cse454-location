package mapred;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.hadoop.io.Text;

import sf.retriever.CorefEntity;
import sf.retriever.CorefMention;
import sf.retriever.CorefProvider;
import sf.retriever.SentenceProvider;
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
	
	public List<CorefMention> mentions;
	
	public long sentenceId, articleId;
	
	public SentenceAnnotations() {
		mentions = new ArrayList<CorefMention>();
	}
	
	public SentenceAnnotations( String str ) {
		this( str.split("\t") );
	}
	
	public SentenceAnnotations( Text text ) {
		this( text.toString() );
	}
	
	public SentenceAnnotations( String[] packed ) {
		this();
		
		// Read sentence ID
		sentenceId = Long.parseLong( packed[0] );
		
		// Read out all the packed fields
		int idx = 1;
		for ( String title : ordering ) {
			int size = sizes.get( title );
			put( title, sentenceId + "\t" +
					StringUtil.join( packed, "\t", idx, size ) );
			idx += size;
		}
		
		// Read wikification as the last item, since it has varying size.
		if ( idx < packed.length ) {
			int wikiSize = Integer.parseInt( packed[idx++] );
			put( "wikification", sentenceId + "\t" +
					StringUtil.join( packed, "\t", idx, wikiSize ) );
			idx += wikiSize;
		}
			
		// Read coref entities and mentions if they exist.
		if ( idx < packed.length ) {				
			// Unpack entities
			int numCorefEntities = Integer.parseInt( packed[idx++] );
			Map<Long, CorefEntity> corefEntities =
					new HashMap<Long, CorefEntity>();
			for ( int i = 0; i < numCorefEntities; i++ ) {
				CorefEntity e = new CorefEntity();
				idx = e.unpack( packed, idx );
				corefEntities.put( e.id, e );
			}
			
			// Unpack mentions
			if ( idx < packed.length ) {
				int numCorefMentions = Integer.parseInt( packed[idx++] );
				for ( int i = 0; i < numCorefMentions; i++ ) {
					CorefMention m = new CorefMention();
					idx = m.unpack( packed, idx, corefEntities );
					if ( m.sentenceId == sentenceId )
						mentions.add( m );
				}
			}
		}
	}
	
	public CorefProvider getSentenceCoref() {
		return new SentenceProvider( mentions );
	}
	
	// Repack the data
	public String[] pack( boolean useSentenceId, boolean useCoref ) {
		List<String> result = new ArrayList<String>();
		
		if ( useSentenceId )
			result.add( "" + sentenceId );
		
		// Read out all the packed fields
		for ( String title : ordering ) {
			int size = sizes.get( title );
			String value = get( title );
			if ( value != null ) {
				String[] tokens = value.split("\t");
				if ( tokens.length != size + 1 ) {
					int oldSize = tokens.length;
					tokens = Arrays.copyOfRange( tokens, 1, size + 1 );
					for ( int i = oldSize; i < size; i++ )
						tokens[i] = "";
				} else {
					tokens = Arrays.copyOfRange( tokens, 1, size + 1 );
				}
				result.addAll( Arrays.asList( tokens ) );
			} else {
				for ( int i = 0; i < size; i++ )
					result.add( "" );
			}
		}
		
		// Get wikification data.
		String wiki = get( "wikification" );
		if ( wiki != null ) {
			String[] tokens = wiki.split("\t");
			tokens = Arrays.copyOfRange( tokens, 1, tokens.length );
			result.add( tokens.length + "" );
			result.addAll( Arrays.asList( tokens ) );
		} else {
			result.add( "0" );
		}
		
		if ( useCoref ) {
			// Collect the coref entities mentioned in this sentence.
			Set<CorefEntity> entities = new HashSet<CorefEntity>();
			for ( CorefMention mention : mentions ) {
				if ( mention.entity != null )
					entities.add( mention.entity );
			}
			
			// Pack the coref entities.
			result.add( entities.size() + "" );
			Set<CorefMention> repMentions = new HashSet<CorefMention>();
			for ( CorefEntity entity : entities ) {
				result.addAll( Arrays.asList( entity.pack() ) );
				if ( entity.repMention != null )
					repMentions.add( entity.repMention );
			}
			
			// Pack the coref mentions.
			repMentions.addAll( mentions );
			result.add( repMentions.size() + "" );
			for ( CorefMention mention : repMentions ) {
				result.addAll( Arrays.asList( mention.pack() ) );
			}
		}
		
		return result.toArray(new String[0]);
	}
	
	public String toString( boolean useSentenceId, boolean useCoref ) {
		return StringUtil.join( pack( useSentenceId, useCoref ), "\t" );
	}
	
	public Text toText( boolean useSentenceId, boolean useCoref ) {
		return new Text( toString( useSentenceId, useCoref ) );
	}
}
