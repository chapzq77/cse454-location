/**
 * 
 */
package tests;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.Test;

import sf.retriever.*;
import sf.retriever.CorefMention.Animacy;
import sf.retriever.CorefMention.Gender;
import sf.retriever.CorefMention.Plurality;
import sf.retriever.CorefMention.Type;

/**
 * @author Jeffrey Booth
 *
 */
public class CorefIndexTest {
	public static CorefMention makeSampleCoref( CorefEntity e ) {
		CorefMention m = new CorefMention();
		m.animacy = Animacy.INANIMATE;
		m.gender  = Gender.NEUTRAL;
		m.number  = Plurality.SINGULAR;
		m.type    = Type.PROPER;
		m.start   = 0;
		m.end     = 6;
		m.head    = 6;
		m.mentionSpan = "A brand new and bigger Air China";;
		m.entity = e;
		e.mentions.add( m );
		return m;
	}
	
	@Test
	public void testOneDocTwoClustersTwoMentionsTwoSentences() {
		CorefIndex idx = (new IndexMaker())
			.part(0, 8, 1)
			.part(0, 9, 2)
			.make();
		idx.nextDoc(0);
		
		CorefEntity e1 = new CorefEntity();
		CorefMention m1 = makeSampleCoref( e1 );
		
		CorefEntity e2 = new CorefEntity();
		CorefMention m2 = makeSampleCoref( e2 );
		
		// Check first sentence
		CorefProvider p = idx.getSentenceProvider(1);
		assertEquals( Arrays.asList( m1 ), p.all() );
		
		// Check second sentence
		p = idx.getSentenceProvider(2);
		assertEquals( Arrays.asList( m2 ), p.all() );
	}
	
	@Test
	public void testOneDocOneClusterTwoMentionsOneSentence() {
		CorefIndex idx = (new IndexMaker())
			.part(0, 8, 1)
			.part(0, 8, 1)
			.make();
		idx.nextDoc(0);
		
		CorefEntity e = new CorefEntity();
		CorefMention m1 = makeSampleCoref( e ),
				     m2 = makeSampleCoref( e );
		
		// Check first sentence
		CorefProvider p = idx.getSentenceProvider(1);
		assertEquals( Arrays.asList( m1, m2 ), p.all() );
	}
	
	@Test
	public void testOneDocOneClusterTwoMentionsTwoSentences() {
		String SPAN = "A brand new and bigger Air China";
		CorefIndex idx = (new IndexMaker())
			.part(0, 8, 1)
			.part(0, 8, 2)
			.make();
		idx.nextDoc(0);
		
		CorefEntity e = new CorefEntity();
		CorefMention m1 = makeSampleCoref( e ),
				     m2 = makeSampleCoref( e );
		
		// Check first sentence
		CorefProvider p = idx.getSentenceProvider(1);
		assertEquals( Arrays.asList( m1 ), p.all() );
		
		// Check second sentence
		p = idx.getSentenceProvider(2);
		assertEquals( Arrays.asList( m2 ), p.all() );
	}
	
	@Test
	public void testOneDoc() {
		CorefIndex idx = (new IndexMaker())
			.part(0, 8, 1)
			.make();
		idx.nextDoc(0);
		
		CorefEntity e = new CorefEntity();
		CorefMention m = makeSampleCoref( e );
		
		CorefProvider p = idx.getSentenceProvider(1);
		assertEquals( Arrays.asList( m ), p.all() );
	}

	@Test
	public void testSimple() {
		(new IndexMaker()).make();
	}

}
