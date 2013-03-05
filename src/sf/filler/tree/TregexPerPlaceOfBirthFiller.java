package sf.filler.tree;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import sf.SFConstants;
import sf.SFEntity;
import sf.filler.Filler;
import sf.retriever.CorefProvider;

public class TregexPerPlaceOfBirthFiller extends Filler {

	private static final String countryOfBirth = "per:country_of_birth";
	private static final String stateOfBirth = "per:stateorprovince_of_birth";
	private static final String cityOfBirth = "per:city_of_birth";

	public TregexPerPlaceOfBirthFiller() {
		slotNames.add(countryOfBirth);
		slotNames.add(stateOfBirth);
		slotNames.add(cityOfBirth);
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations,
			CorefProvider sentenceCoref) {
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!isPER(mention))
			return;
		if (!containsName(mention, tokens))
			return;
		
		String cjtext = annotations.get(SFConstants.CJ);
		String filename = getFilename(annotations);
		Tree t = null;
		
		try {
			t = new PennTreeReader(new StringReader(cjtext)).readTree();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TregexPattern p = TregexPattern.compile("NNP|NNPS >> (NP > (PP < (IN < /^in$/) > (VP < (VBD|VBN < /born/))))");
		TregexMatcher m = p.matcher(t);
		
		ArrayList<String> possiblePlaces = new ArrayList<String>();
		while(m.find()) {
			possiblePlaces.add(m.getMatch().firstChild().value());
		}
		
		// TODO not very good, need a way to combine multiple word place names
		// TODO also check for LOCATION tags
		for(String place : possiblePlaces) {
			if(isCountry(place)) {
				mention.addAnswer(countryOfBirth, place, filename);
			} else if(isStateProv(place)) {
				mention.addAnswer(stateOfBirth, place, filename);
			} else {
				mention.addAnswer(cityOfBirth, place, filename);
			}
		}
	}

}
