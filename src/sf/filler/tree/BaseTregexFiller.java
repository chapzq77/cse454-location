package sf.filler.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import sf.SFConstants;
import sf.filler.Filler;
import sf.retriever.CorefEntity;
import sf.retriever.CorefMention;
import sf.retriever.CorefProvider;
import sf.retriever.NerType;

public abstract class BaseTregexFiller extends Filler {
	
	protected List<String> getMatchNames(String pattern, Tree t, Map<String, String> annotations, CorefProvider coref) {
		TregexPattern p = TregexPattern.compile(pattern);
		TregexMatcher m = p.matcher(t);
		
		List<String> places = new ArrayList<String>();
		while(m.find()) {
			Tree match = m.getMatch();
			for(Tree node : match.children()) {
				if(node.label().value().startsWith("NNP")) {
					places.add(node.firstChild().value());
				}
			}
		}
		
		ArrayList<String> retPlaces = new ArrayList<String>();
		List<String> tokens = Arrays.asList(annotations.get(SFConstants.TOKENS).split(" "));
		for(String place : places) {
			int placeIndex = tokens.indexOf(place);
			if(placeIndex >= 0) {
				Collection<CorefMention> corefs = coref.inRange(placeIndex, placeIndex);
				if(corefs.size() > 0) {
					CorefEntity entity = corefs.iterator().next().entity;
					if(entity.nerType == NerType.LOCATION)
						retPlaces.add(entity.repMention.entity.fullName);
				}
			}
		}
		
		return retPlaces;
	}
	
}
