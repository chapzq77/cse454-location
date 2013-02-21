package sf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tackbp.KbEntity;
import el.EntityMention;

/**
 * Slot Filling Entity.
 * 
 * It's constructed when reading the queries.
 * The field <code>answers</code> is filled by slot fillers.
 * 
 * @author xiaoling
 * 
 */
public class SFEntity extends EntityMention {
	// entity type
	public KbEntity.EntityType entityType = null;
	// slots that can be ignored.
	public List<String> ignoredSlots = new ArrayList<String>();
	// answers: the key is slot type and the value is an <code>Answer</code>
	// object.
	// An <code>Answer</code> object should be either
	// list-valued(<code>ListAnswer</code>)
	// or single-valued (<code>SingleAnswer</code>).
	
	// TODO: this is specific to SingleAnswer answers right now
	public Map<String, List<SingleAnswer>> answers = new HashMap<String, List<SingleAnswer>>();

	public static abstract class Answer {

	}

	public static class ListAnswer extends Answer {
		// answers
		public List<String> listAnswers = new ArrayList<String>();
		// The documents from which the answers are extracted.
		// The number of docs MUST be the same as the number of answers.
		public List<String> listDocs = new ArrayList<String>();
		@Override
		public String toString() {
			return listAnswers.toString();
		}
	}

	public static class SingleAnswer extends Answer {
		// answer
		public String answer = null;
		// The document from which the answer is extracted.
		public Set<String> doc = null;
		public int count = 0;
		@Override
		public String toString() {
			return answer;
		}
	}
}
