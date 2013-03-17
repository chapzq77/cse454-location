package mapred;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class SentenceAnnotationsTest {
	public static String[] packed = {
		"100",
		"meta1",
		"meta2",
		"meta3",
		"meta4",
		"text",
		"tokens",
		"stanfordlemma",
		"tokenSpans",
		"stanfordpos",
		"cj",
		"depsStanfordCCProcessed",
		"depsStanfordCCProcessed2",
		"stanfordner",
		"articleIDs",
		"2",
		"wiki1",
		"wiki2"
	};
	
	SentenceAnnotations sa;
	
	@Before
	public void setUp() {
		sa = new SentenceAnnotations( packed );
	}
	
	@Test
	public void testUnpackAndPack() {
		assertArrayEquals( packed, sa.pack( true, false ) );
	}
	
	@Test
	public void testNer() {
		assertEquals( "100\tstanfordner", sa.get("stanfordner") );
	}
	
	@Test
	public void testWikiAnnotation() {
		assertEquals( "100\twiki1\twiki2", sa.get("wikification") );
	}
	
	@Test
	public void testSentenceId() {
		assertEquals( 100, sa.sentenceId );
	}
	
	@Test
	public void testMissingNerAnnotation() {
		sa.remove("stanfordner");
		String[] packed2 = Arrays.copyOf( packed, packed.length );
		packed2[13] = "";
		assertArrayEquals( packed2, sa.pack( true, false ) );
	}
	
	@Test
	public void testMissingWikiAnnotation() {
		sa.remove("wikification");
		String[] packed2 = Arrays.copyOf( packed, packed.length - 3 );
		assertArrayEquals( packed2, sa.pack( true, false ) );
	}
	
	@Test
	public void testNoSentenceId() {
		String[] packed2 = Arrays.copyOfRange( packed, 1, packed.length );
		String[] result = sa.pack( false, false );
		assertArrayEquals( packed2, result );
	}
	
	@Test
	public void testCorefMentions() {
		String[] packed2 = Arrays.copyOfRange( packed, 0, packed.length + 18 );
		String[] corefs = new String[] {
			"1",
			"1",
			"Rambo",
			"PERSON",
			"Rambo!",
			"1",
			"2",
			"5",
			"9",
			"5",
			"Rambo the Great American Fighter",
			"PROPER",
			"SINGULAR",
			"MALE",
			"ANIMATE",
			"100",
			"1",
			"true"
		};
		System.arraycopy( corefs, 0, packed2, packed.length, 18 );
		sa = new SentenceAnnotations( packed2 );
		String[] result = sa.pack( true, true );
		assertArrayEquals( packed2, result );
	}
}
