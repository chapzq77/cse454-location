package sf.retriever;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Provide coreference information for a single sentence.
 * 
 * @author Jeffrey Booth
 */
public class SentenceProvider implements CorefProvider {
	Collection<CorefMention> mentions;
	
	public SentenceProvider( Collection<CorefMention> mentions ) {
		if ( mentions == null )
			this.mentions = Collections.emptyList();
		else {
			// TODO: it's a little hacky to do the sorting in here...
			List<CorefMention> mentionsList =
					new ArrayList<CorefMention>( mentions );
			Collections.sort( mentionsList, new Comparator<CorefMention>() {
				@Override
				public int compare(CorefMention arg0, CorefMention arg1) {
					return (int) (arg0.id - arg1.id);
				}
			});
			this.mentions = mentionsList;
		}
	}

	@Override
	public Collection<CorefMention> inRange(int rangeStart, int rangeEnd) {
		List<CorefMention> results = new ArrayList<CorefMention>();
		for ( CorefMention mention : mentions ) {
			if ( mention.end >= rangeStart && mention.start <= rangeEnd )
				results.add( mention );
		}
		return results;
	}

	@Override
	public Collection<CorefMention> all() {
		return mentions;
	}
}
