package sf.filler.tree;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import sf.SFConstants;
import sf.SFEntity;
import sf.filler.Filler;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class TregexPerPlacesOfResidenceFiller extends Filler {

	private static final String countriesOfResidence = "per:countries_of_residence";
	private static final String statesOfResidence = "per:stateorprovinces_of_residence";
	private static final String citiesOfResidence = "per:cities_of_residence";

	public TregexPerPlacesOfResidenceFiller() {
		slotNames.add(countriesOfResidence);
		slotNames.add(statesOfResidence);
		slotNames.add(citiesOfResidence);
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations) {
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!isPER(mention))
			return;
		if (!containsName(mention, tokens))
			return;
		
		String cjtext = annotations.get(SFConstants.CJ);
		Tree t = null;
		
		try {
			t = new PennTreeReader(new StringReader(cjtext)).readTree();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TregexPattern p = TregexPattern.compile("NNP|NNPS >> (NP >> (PP < (IN < /^(in|at)$/) > (VP < (VBD|VBN < /lived/))))");
		TregexMatcher m = p.matcher(t);
		
		TregexPattern p2 = TregexPattern.compile("NNPS|NNP >> (NP > (PP < (IN < /^in$/) > (VP < (VBD|VBN < /grew/))))");
		TregexMatcher m2 = p2.matcher(t);
		
		ArrayList<String> possiblePlaces = new ArrayList<String>();
		while(m.find()) {
			possiblePlaces.add(m.getMatch().firstChild().value());
		}
		while(m2.find()) {
			possiblePlaces.add(m2.getMatch().firstChild().value());
		}
		
		// TODO not very good, need a way to combine multiple word place names
		// TODO also check for LOCATION tags
		for(String place : possiblePlaces) {
			if(isCountry(place)) {
				addAnswer(mention, annotations, place, countriesOfResidence);
			} else if(isStateProv(place)) {
				addAnswer(mention, annotations, place, statesOfResidence);
			} else {
				addAnswer(mention, annotations, place, citiesOfResidence);
			}
		}
	}
}
