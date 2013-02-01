package sf.filler;

import java.util.Map;

import sf.SFEntity;

/**
 *
 * @author Xiao Ling
 */

public abstract class Filler {
	public String slotName = null;
	public abstract void predict(SFEntity mention, Map<String, String> annotations);
}
