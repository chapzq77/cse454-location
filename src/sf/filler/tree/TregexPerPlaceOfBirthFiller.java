package sf.filler.tree;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;

import sf.SFConstants;
import sf.SFEntity;
import sf.retriever.CorefProvider;

public class TregexPerPlaceOfBirthFiller extends BaseTregexFiller {

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
		if (containsName(mention, tokens, sentenceCoref) != -1)
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
		
		List<String> places = getMatchNames("NNP|NNPS >> (NP > (PP < (IN < /^in$/) > (VP < (VBD|VBN < /born/))))", t, annotations, sentenceCoref);
		
		// TODO not very good, need a way to combine multiple word place names
		// TODO also check for LOCATION tags
		//System.out.println(places);
		for(String placeName : places) {
			if(isCountry(placeName)) {
				if(!mention.ignoredSlots.contains(countryOfBirth)) {
					mention.addAnswer(countryOfBirth, placeName, filename);
				}
			} else if(isStateProv(placeName)) {
				if(!mention.ignoredSlots.contains(stateOfBirth)) {
					mention.addAnswer(stateOfBirth, placeName, filename);
				}
			} else if(placeName.length() > 1){
				if(!mention.ignoredSlots.contains(cityOfBirth)) {
					mention.addAnswer(cityOfBirth, placeName, filename);
				}
			}
		}
	}

}
