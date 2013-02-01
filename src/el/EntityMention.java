package el;

/**
 * Query Entity, used for EL.
 * The <code>entityId</code> will be used as the answer for EL.
 * 
 * <code>SFEntity</code> should be used for SF.
 * 
 * @author xiaoling
 * 
 */
public class EntityMention {
	// query id
	public String queryId = null;
	// the entity string
	public String mentionString = null;
	// the document that mentions the entity
	public String mentionDoc = null;
	// the entity id w.r.t. the knowledge base
	public String entityId = null;

	// check if the answer is filled.
	public boolean isValidAnswer() {
		return entityId != null;
	}

	public String toString() {
		if (queryId != null && mentionString != null && mentionDoc != null) {
			if (entityId == null) {
				return String.format("qid:%s, name:%s, doc:%s, entity:null",
						queryId, mentionString, mentionDoc);
			} else {
				return String.format("qid:%s, name:%s, doc:%s, entity:%s",
						queryId, mentionString, mentionDoc, entityId);
			}
		} else {
			return "Uninitialized entity mention.";
		}
	}
}
