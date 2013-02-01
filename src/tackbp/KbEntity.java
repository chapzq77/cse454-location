package tackbp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity node in the KB
 * @author xiaoling
 *
 */
public class KbEntity {
	public enum EntityType{
		PER, ORG, UKN, GPE
	}
	public String wikiTitle = null;
	public EntityType type = null;
	public String kbId = null;
	public String name = null;
	public List<Fact> facts = new ArrayList<Fact>();
	
	public String wikiText = null;
	
	static class Fact {
		String factClass = null;
		Map<String, String> attrValueMap = new HashMap<String, String>();
	}
	public String toString() {
		return "wikiTitle="+wikiTitle +",\n"
				+ "type="+type+",\n"
				+ "id="+kbId+",\n"
				+ "name="+name+",\n"
				+ "facts="+facts+",\n"
				+ "wikiText="+wikiText +"\n";
	}
}
