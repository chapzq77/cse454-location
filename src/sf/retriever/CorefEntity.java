package sf.retriever;

public class CorefEntity {
	/**
	 * ID of Wikipedia article. Will be null if no Wiki article corresponds to
	 * this entry.
	 */
	public String wikiId;
	
	/**
	 * Named Entity Recognizer type (person, organization, location, or other).
	 */
	public NerType nerType;
	
	/**
	 * Full "canonical" name of the entity.
	 */
	public String fullName;
}
