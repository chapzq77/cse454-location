import java.io.PrintStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sf.SFConstants;
import sf.retriever.ProcessedCorpus;

/**
 * Sentence Finder
 *
 * This class finds sentences containing both:
 *  * a person or organization
 *  * a location
 * These classifications are given by the Stanford Named Entity Recognizer.
 *
 * @author Jeff Booth
 */

public class SentenceFinder {
	static final int SENTENCE_LIMIT = 100000;

	static class CorpusWriter implements AutoCloseable {
		protected Map<String, PrintStream> dataWriters = null;

		public CorpusWriter() throws IOException {
			this( "data/sentences", SFConstants.dataTypes );
		}

		public CorpusWriter( String basePath, String[] dataTypes ) throws IOException {
			dataWriters = new HashMap<String, PrintStream>();
			try {
				for ( String dataType : dataTypes ) {
					String filename = basePath + "." + dataType;
					PrintStream writer = new PrintStream( filename );
					dataWriters.put( dataType, writer );
				}
			} catch( IOException e ) {
				// Close existing writers and rethrow.
				close( e );
				throw e;
			}
		}

		@Override
		public void close() throws IOException {
			close( null );
		}

		public void write( Map<String, String> values ) throws IOException {
			for ( Map.Entry<String, String> entry : values.entrySet() ) {
				String dataType = entry.getKey();
				String value = entry.getValue();
				PrintStream writer = dataWriters.get( dataType );
				if ( writer != null ) {
					writer.println( value );
				}
			}
		}

		// Attempt to close all of the writers.
		// If closing one throws an exception, then add that exception
		// to a suppressed exception list and keep closing the others.
		private void close( Throwable exToThrow ) {
			for ( PrintStream ps : dataWriters.values() ) {
				try {
					ps.close();
				} catch ( Throwable t ) {
					if ( exToThrow == null )
						exToThrow = t;
					else
						exToThrow.addSuppressed( t );
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// initialize the corpus
		// FIXME replace the list by a generic class with an input of slot
		// name and an output of all the relevant files from the answer file

		try( ProcessedCorpus corpus = new ProcessedCorpus();
			 CorpusWriter corpusWriter = new CorpusWriter() ) {
			Map<String, String> annotations = null;
			int c = 0;
			int validSentences = 0;
			while (corpus.hasNext()) {
				annotations = corpus.next();
				if (c++ % 100000 == 0) {
					System.err.print("finished reading " + c + " lines\r");
				}

				// If the annotations contain a Person or Organization, and a Location, then write out the sentence.
				String namedEntities = annotations.get(SFConstants.STANFORDNER);
				if ( namedEntities.indexOf( "LOCATION" ) != -1 &&
					( namedEntities.indexOf( "PERSON" ) != -1 ||
					  namedEntities.indexOf( "ORGANIZATION" ) != -1 ) ) {

					// Output the annotations.
					corpusWriter.write( annotations );

					// Leave once we have enough valid sentences.
					validSentences++;
					if ( validSentences >= SENTENCE_LIMIT )
						return;
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
}
