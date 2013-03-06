package sf.filler.tree;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import sf.filler.Filler;

public abstract class BaseTregexFiller extends Filler {
	
	protected List<String> getMatchNames(String pattern, Tree t) {
		TregexPattern p = TregexPattern.compile(pattern);
		TregexMatcher m = p.matcher(t);
		
		Tree parent = null;
		List<String> places = new ArrayList<String>();
		StringBuilder place = new StringBuilder();
		if(m.find()) {
			parent = m.getMatch().parent();
			place.append(m.getMatch().firstChild().value());
			while(m.find()) {
				if(m.getMatch().parent().equals(parent)) {
					place.append(" ");
					place.append(m.getMatch().firstChild().value());
				} else {
					places.add(place.toString());
					place = new StringBuilder();
					place.append(m.getMatch().firstChild().value());
				}
			}
		}
		places.add(place.toString());
		
		return places;
	}
	
}
