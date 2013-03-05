package sf.filler.tree;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import sf.SFConstants;
import sf.SFEntity;
import sf.retriever.CorefProvider;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;

public class TregexPerPlacesOfResidenceFiller extends BaseTregexFiller {

	private static final String countriesOfResidence = "per:countries_of_residence";
	private static final String statesOfResidence = "per:stateorprovinces_of_residence";
	private static final String citiesOfResidence = "per:cities_of_residence";

	public TregexPerPlacesOfResidenceFiller() {
		slotNames.add(countriesOfResidence);
		slotNames.add(statesOfResidence);
		slotNames.add(citiesOfResidence);
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations,
			CorefProvider sentenceCoref) {
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!isPER(mention))
			return;
		if (!containsName(mention, tokens, sentenceCoref))
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
		List<String> places = getMatchNames("NNP|NNPS >> (NP >> (PP < (IN < /^(in|at)$/) > (VP < (VBD|VBN < /lived/))))", t);
		places.addAll(getMatchNames("NNPS|NNP >> (NP > (PP < (IN < /^in$/) > (VP < (VBD|VBN < /grew/))))", t));
		
		// TODO not very good, need a way to combine multiple word place names
		// TODO also check for LOCATION tags
		for(String placeName : places) {
			if(isCountry(placeName)) {
				mention.addAnswer(countriesOfResidence, placeName, filename);
			} else if(isStateProv(placeName)) {
				mention.addAnswer(statesOfResidence, placeName, filename);
			} else {
				mention.addAnswer(citiesOfResidence, placeName, filename);
			}
		}
	}
}
