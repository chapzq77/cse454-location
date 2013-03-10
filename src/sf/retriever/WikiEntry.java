package sf.retriever;

import java.util.ArrayList;
import java.util.Collection;

public class WikiEntry {
	/** Zero-based index of the first token in the sentence. */
	public int start;
	
	/** Zero-based index of the last token in the sentence. */
	public int end;
	
	/** Wikipedia article name. */
	public String articleName;
	
	/** Confidence that the article matches the sequence of tokens. */
	public double confidence;
}
