package sf.retriever;

public class CorefIndex {
	/**
	 * Creates a coreference index for the given document.
	 * 
	 * @param documentId
	 */
	public CorefIndex( long documentId ) {
		// TODO: initialize coreference data from document.
	}
	
	/**
	 * Returns an interface for a single sentence.
	 */
	public CorefProvider getSentenceIndex( int sentenceId ) {
		// TODO: create new provider.
		return new CorefProvider() {
			@Override
			public CorefEntity[] inRange(int rangeStart, int rangeEnd) {
				// TODO Auto-generated method stub
				return new CorefEntity[0];
			}

			@Override
			public CorefEntity[] all() {
				// TODO Auto-generated method stub
				return new CorefEntity[0];
			}
		};
	}
}
