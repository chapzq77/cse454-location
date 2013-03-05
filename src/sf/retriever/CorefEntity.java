package sf.retriever;

/**
 * Represents a cluster of coreference mentions.
 * This class may also contain the corresponding Wikipedia article ID and NER
 * type of the entity, based on information gleaned from sentences in the
 * article. 
 * 
 * @author Jeffrey Booth
 */
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
	
	/**
	 * Mention that's best representative of this entity's cluster of mentions.
	 */
	public CorefMention repMention;
	
	/**
	 * All mentions.
	 */
	public CorefMention[] mentions;
}
