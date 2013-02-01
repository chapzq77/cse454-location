package tackbp;


public class KbpConstants {
	// the data folder. TODO CHANGE it if you mount the volume elsewhere.
	public static String rootPath = "/kbp/data/";

	public static final String processedDocPath = rootPath + "09nwa/";;

	// knowledge base
	public static final String kbPath = KbpConstants.rootPath + "TAC09KBPEvalRefKB/data/";
	public static final int kbFileStart = 1;
	public static final int kbFileEnd = 89; // exclusive
	public static final String kbFileFormat = "kb_part-%04d.xml";

}
