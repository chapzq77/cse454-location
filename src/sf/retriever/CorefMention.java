package sf.retriever;

/**
 * Represents a *mention* of an entity which has coreferences.
 * 
 * This class indicates where the entity is mentioned, as well as its
 * animacy, mention type, plurality, and gender.
 * 
 * @author Jeffrey Booth
 */
public class CorefMention {
	public static enum Animacy { ANIMATE, INANIMATE, UNKNOWN }
	public static enum Type { PRONOMINAL, PROPER, NOMINAL	}
	public static enum Plurality { SINGULAR, PLURAL, UNKNOWN }
	public static enum Gender { FEMALE, UNKNOWN, MALE, NEUTRAL }
	
	/** Entity being referred to in this mention. */
	public CorefEntity entity;
	
	/** Starting index, numbered *from zero*. */
	public int start;
	
	/** Ending index, numbered *from zero*. */
	public int end;
	
	/**
	 * Head index, numbered *from zero*.
	 * 
	 * In linguistics, the head of a phrase is the word that determines the
	 * syntactic type of that phrase, or analogously, the stem that determines
	 * the semantic category of a compound of which it is a part.
	 */
	public int head;
	
	/**
	 * The actual mention string
	 */
	public String mentionSpan;
	
	/**
	 * The mention type
	 */
	public Type type;
	
	/**
	 * Mention plurality
	 */
	public Plurality number;
	
	/**
	 * Gender
	 */
	public Gender gender;
	
	/**
	 * Animacy
	 */
	public Animacy animacy;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((animacy == null) ? 0 : animacy.hashCode());
		result = prime * result + end;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result + head;
		result = prime * result
				+ ((mentionSpan == null) ? 0 : mentionSpan.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + start;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		CorefMention other = (CorefMention) obj;
		if (animacy != other.animacy)
			return false;
		if (end != other.end)
			return false;
		if (gender != other.gender)
			return false;
		if (head != other.head)
			return false;
		if (mentionSpan == null) {
			if (other.mentionSpan != null)
				return false;
		} else if (!mentionSpan.equals(other.mentionSpan))
			return false;
		if (number != other.number)
			return false;
		if (start != other.start)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CorefMention [start=" + start + ", end="
				+ end + ", head=" + head + ", mentionSpan=" + mentionSpan
				+ ", type=" + type + ", number=" + number + ", gender="
				+ gender + ", animacy=" + animacy + "]";
	}
}
