package tests;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import sf.retriever.CorefEntity;
import sf.retriever.CorefIndex;
import sf.retriever.CorefMention;
import sf.retriever.CorefProvider;

public class CorefProviderTest {
	CorefIndex idx;
	CorefEntity e;
	CorefMention m1, m2;
	CorefProvider p;
	
	@Before
	public void setUp() throws Exception {
		idx = (new IndexMaker())
			.part(0, 8, 1, 3, 7)
			.part(0, 8, 1, 2, 5)
			.make();
		idx.nextDoc(0);
		e = new CorefEntity();
		m1 = CorefIndexTest.makeSampleCoref( e );
		m1.start = 2;
		m1.end = 5;
		m2 = CorefIndexTest.makeSampleCoref( e );
		m2.start = 1;
		m2.end = 3;
		p = idx.getSentenceProvider(1);
	}
	
	private void testRange( int start, int end, Object... expected ) {
		assertEquals( Arrays.asList(expected), p.inRange( start, end ) );
	}
	
	@Test
	public void testOverlapSecond() {
		testRange( 4, 5, m1 );
	}
	
	@Test
	public void testOverlapFirst() {
		testRange( 0, 1, m2 );
	}
	
	@Test
	public void testOverlapMiddle() {
		testRange( 2, 3, m1, m2 );
	}
	
	@Test
	public void testOverlapAll() {
		testRange( 0, 8, m1, m2 );
	}
	
	@Test
	public void testNoOverlapLeft() {
		testRange( 0, 0 );
	}
	
	@Test
	public void testNoOverlapRight() {
		testRange( 6, 7 );
	}
}
