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
	static enum Animacy { ANIMATE, INANIMATE, UNKNOWN }
	static enum Type { PRONOMINAL, PROPER, NOMINAL	}
	static enum Plurality { SINGULAR, PLURAL, UNKNOWN }
	static enum Gender { FEMALE, UNKNOWN, MALE, NEUTRAL }
	
	/** Entity being referred to in this mention. */
	CorefEntity entity;
	
	/** Starting index. */
	int start;
	
	/** Ending index. */
	int end;
	
	/**
	 * Head index.
	 * 
	 * In linguistics, the head of a phrase is the word that determines the
	 * syntactic type of that phrase, or analogously, the stem that determines
	 * the semantic category of a compound of which it is a part.
	 */
	int head;
	
	/**
	 * The actual mention string
	 */
	String mentionSpan;
	
	/**
	 * The mention type
	 */
	Type type;
	
	/**
	 * Mention plurality
	 */
	Plurality number;
	
	/**
	 * Gender
	 */
	Gender gender;
	
	/**
	 * Animacy
	 */
	Animacy animacy;
}
