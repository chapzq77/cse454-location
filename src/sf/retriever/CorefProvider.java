package sf.retriever;

/**
 * Provides all of the mentions and entities that correspond to data in the
 * coreference database.
 * @author Jeffrey Booth
 */
public interface CorefProvider {
	/**
	 * Returns entity mentions which overlap the given range.
	 * 
	 * @param rangeStart The index of the first token in the range, numbered
	 *                   from 0.
	 * @param rangeEnd The index of the last token in the range, numbered from
	 *                 0.
	 * @return A list of mentions which overlap the given range. The list is
	 *         sorted so that mentions which have more tokens overlapping the
	 *         given range are at the front.
	 */
	CorefMention[] inRange( int rangeStart, int rangeEnd );
	
	/**
	 * Returns all entity mentions in a sentence.
	 * @return
	 */
	CorefMention[] all();
}
