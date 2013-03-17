package sf.retriever;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
	/**
	 * The number of string tokens used to represent a CorefMention in
	 * documents.coref.
	 */
	public static final int NUM_TOKENS = 15;
	
	/** ID number for this mention (scope: document). */
	public long id;
	
	/** Global sentence ID. */
	public long sentenceId;
	
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
	
	public CorefMention() {}
	
	/**
	 * Unpacks this mention from a list of tokens.
	 * 
	 * @param tokens Tokens to unpack from.
	 * @param entitesMap Maps entity IDs to already-created entities.
	 * 
	 * @return the index of the token following the last token unpacked.
	 */
	public int unpack( String[] tokens, int offset,
			Map<Long, CorefEntity> entitiesMap ) {
		int i = offset;
		id           = Long.parseLong( tokens[i++] );
		start        = Integer.parseInt( tokens[i++] );
		end          = Integer.parseInt( tokens[i++] );
		head         = Integer.parseInt( tokens[i++] );
		mentionSpan  = tokens[i++];
		type         = Type.valueOf( tokens[i++] );
		number       = Plurality.valueOf( tokens[i++] );
		gender       = Gender.valueOf( tokens[i++] );
		animacy      = Animacy.valueOf( tokens[i++] );
		sentenceId   = Long.parseLong( tokens[i++] );
		long clusterId = Long.parseLong( tokens[i++] );
		boolean repMention = Boolean.parseBoolean( tokens[i++] );
		addCorefEntity( entitiesMap, clusterId, repMention );
		return i;
	}
	
	/**
	 * Packs this mention into a list of tokens.
	 * @return the packed list of tokens.
	 */
	public String[] pack() {
		return new String[] {
			"" + id,
			"" + start,
			"" + end,
			"" + head,
			mentionSpan,
			"" + type,
			"" + number,
			"" + gender,
			"" + animacy,
			"" + sentenceId,
			"" + ( entity == null ? -1 : entity.id ),
			"" + ( entity != null && entity.repMention == this )
		};
	}
	
	/**
	 * Adds to, or retrieves this mention's coref entity in, the given entities
	 * map; then adds this mention to the located coref entity.
	 * 
	 * @param entitiesMap Maps entity cluster IDs to entities.
	 * @param clusterId The ID of the cluster; should be >= 0.
	 * @param repMention If true, the entity will make this mention its
	 *                   representative mention.
	 */
	protected void addCorefEntity( Map<Long, CorefEntity> entitiesMap,
			long clusterId, boolean repMention ) {
		if ( clusterId < 0 ) return;
		CorefEntity entity = entitiesMap.get( clusterId );
		if ( entity == null ) {
			entity = new CorefEntity();
			entity.id = clusterId;
			entitiesMap.put( clusterId, entity );
		}
		entity.mentions.add( this );
		this.entity = entity;
		if ( repMention ) {
			entity.repMention = this;
		}
	}
	
	/**
	 * Creates a new CorefMention.
	 * 
	 * @param tokens A sequence of at least 15 String tokens read from
	 *               a line of documents.coref.
	 * @param entitiesMap Maps entity IDs to CorefEntity objects. CorefMention
	 *                    uses this map to find the value for its entity field.
	 *                    If that value doesn't exist, it is created and added
	 *                    to the map.
	 */
	public CorefMention( String[] tokens, Map<Long, CorefEntity> entitiesMap ) {
		if ( tokens.length <= 14 )
			throw new IllegalArgumentException("Can't create CorefMention " +
					"with only " + tokens.length + " tokens.");
		
		id           = Long.parseLong( tokens[2] );
		start        = Integer.parseInt( tokens[5] ) - 1;
		end          = Integer.parseInt( tokens[6] ) - 2; // TODO: check
		head         = Integer.parseInt( tokens[7] ) - 1;
		mentionSpan  = tokens[9];
		type         = Type.valueOf( tokens[10] );
		number       = Plurality.valueOf( tokens[11] );
		gender       = Gender.valueOf( tokens[12] );
		animacy      = Animacy.valueOf( tokens[13] );
		sentenceId   = Long.parseLong( tokens[3] );
		
		long clusterId = Long.parseLong( tokens[1] );
		boolean repMention = tokens[14].equals("true");
		addCorefEntity( entitiesMap, clusterId, repMention );
	}
	
	/**
	 * Determines if a token overlaps the tokens in this mention. 
	 * 
	 * @param token Zero-based index of the token in the sentence.
	 * @return true if the token overlaps this mention.
	 */
	public boolean overlaps( int token ) {
		return token >= start && token <= end;
	}
	
	/**
	 * Determines if a token range overlaps the tokens in this mention. 
	 * 
	 * @param token Zero-based index of the *start* of the token range.
	 * @param token Zero-based index of the *end* of the token range.
	 * @return true if the token overlaps this mention.
	 */
	public boolean overlaps( int start, int end ) {
		return start <= this.end && end >= this.start;
	}

	@Override
	public int hashCode() {
		return (int) id;
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
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CorefMention [id=" + id + ", start=" + start + ", end="
				+ end + ", head=" + head + ", mentionSpan=" + mentionSpan
				+ ", type=" + type + ", number=" + number + ", gender="
				+ gender + ", animacy=" + animacy + "]";
	}
}
