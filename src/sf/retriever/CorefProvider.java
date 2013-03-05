package sf.retriever;

/**
 * Provides all of the entities that correspond to data in the coreference
 * database.
 * @author Jeffrey Booth
 */
public interface CorefProvider {
	/**
	 * Returns entities which overlap the given range.
	 * 
	 * @param rangeStart The index of the first token in the range, numbered
	 *                   from 0.
	 * @param rangeEnd The index of the last token in the range, numbered from
	 *                 0.
	 * @return A list of entities which overlap the given range. The list is
	 *         sorted so that Entities which have more tokens overlapping the
	 *         given range are at the front.
	 */
	CorefEntity[] inRange( int rangeStart, int rangeEnd );
	
	/**
	 * Returns all entities in a sentence.
	 * @return
	 */
	CorefEntity[] all();
}
