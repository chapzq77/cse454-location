package sf.retriever;

import java.util.ArrayList;
import java.util.List;

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
	public List<CorefMention> mentions;
	
	public CorefEntity() {
		mentions = new ArrayList<CorefMention>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result
				+ ((mentions == null) ? 0 : mentions.hashCode());
		result = prime * result + ((nerType == null) ? 0 : nerType.hashCode());
		result = prime * result
				+ ((repMention == null) ? 0 : repMention.hashCode());
		result = prime * result + ((wikiId == null) ? 0 : wikiId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CorefEntity other = (CorefEntity) obj;
		if (fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!fullName.equals(other.fullName))
			return false;
		if (mentions == null) {
			if (other.mentions != null)
				return false;
		} else if (!mentions.equals(other.mentions))
			return false;
		if (nerType != other.nerType)
			return false;
		if (repMention == null) {
			if (other.repMention != null)
				return false;
		} else if (!repMention.equals(other.repMention))
			return false;
		if (wikiId == null) {
			if (other.wikiId != null)
				return false;
		} else if (!wikiId.equals(other.wikiId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CorefEntity [wikiId=" + wikiId + ", nerType=" + nerType
				+ ", fullName=" + fullName + ", repMention=" + repMention
				+ ", mentions=" + mentions + "]";
	}
}
