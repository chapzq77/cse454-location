package sf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	// answers: the key is slot type and the value is a List of 
	// <code>SingleAnswer</code> objects
	public Map<String, List<SingleAnswer>> answers = new HashMap<String, List<SingleAnswer>>();

	public static class SingleAnswer {
		// answer (location)
		public String answer = null;
		// The document from which the answer is extracted.
		public String doc = null;
		// count of times this answer has been found
		public int count = 0;
		@Override
		public String toString() {
			return answer;
		}
	}
	
	// adds answer/count to ongoing list.
	public void addAnswer(String slotName, String location, String docName) {
		
		// at least one answer exists for this slot
		if (answers.containsKey(slotName)) {
			
			// this Answer is already in list, increment count and add the doc
			boolean found = false;
			for (SingleAnswer ans : answers.get(slotName)) {
				if (ans.answer.equals(location)) {
					ans.count++;
					found = true;
					break;
				}
			}
			
			// this Answer is not in list, add it with a count of 1
			if (!found) {
				SingleAnswer ans = new SingleAnswer();
				ans.answer = location;
				ans.doc = docName;
				ans.count = 1;
				answers.get(slotName).add(ans);
			}
			
		// no answers exist for this slot yet
		} else {
			// create SingleAnswer
			SingleAnswer ans = new SingleAnswer();
			ans.answer = location;
			ans.doc = docName;
			ans.count = 1;
			
			// create List of Answers containing SingleAnswer
			List<SingleAnswer> ansList = new ArrayList<SingleAnswer>();
			ansList.add(ans);
			
			// put in Map
			answers.put(slotName, ansList);
		}
	}
}
