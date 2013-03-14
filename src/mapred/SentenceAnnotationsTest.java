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
		assertArrayEquals( packed, sa.pack( true ) );
	}
	
	@Test
	public void testNer() {
		assertEquals( "stanfordner", sa.get("stanfordner") );
	}
	
	@Test
	public void testWikiAnnotation() {
		assertEquals( "wiki1\twiki2", sa.get("wikification") );
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
		assertArrayEquals( packed2, sa.pack( true ) );
	}
	
	@Test
	public void testMissingWikiAnnotation() {
		sa.remove("wikification");
		String[] packed2 = Arrays.copyOf( packed, packed.length - 3 );
		assertArrayEquals( packed2, sa.pack( true ) );
	}
	
	@Test
	public void testNoSentenceId() {
		String[] packed2 = Arrays.copyOfRange( packed, 1, packed.length );
		String[] result = sa.pack( false );
		assertArrayEquals( packed2, result );
	}
}
