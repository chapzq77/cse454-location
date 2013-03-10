package sf.retriever;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import sf.SFConstants;
import tackbp.KbpConstants;

/**
 * An iterator of sentences and their annotations from the corpus. Do not forget
 * to call close() after use.
 * 
 * @author xiaoling
 * 
 */
public class ProcessedCorpus implements Iterator<Map<String, String>>, AutoCloseable {
	/**
	 * Creates a ProcessedCorpus for the complete dataset.
	 */
	public ProcessedCorpus() throws Exception {
		this( KbpConstants.processedDocPath );
	}

	/**
	 * Creates a ProcessedCorpus for the dataset at the given path,
	 * that contains all data types.
	 *
	 * @param path a directory, ending with a slash, that contains
	 *             the datafiles.
	 */
	public ProcessedCorpus( String path ) throws Exception {
		this( path, SFConstants.dataTypes );
	}

	/**
	 * Creates a ProcessedCorpus for the dataset at the given path,
	 * containing only the given data types.
	 *
	 * @param path a directory, ending with a slash, that contains
	 *             the datafiles.
	 * @param dts a list of data type names.
	 */
	public ProcessedCorpus( String path, String[] dts ) throws Exception {
		dataTypes = dts;
		init( path );
	}

	protected String[] dataTypes = null;

	protected Map<String, BufferedReader> dataReaders = null;

	protected Map<String, String> cur = null;
	
	// caching the line when hasNext is called.
	protected String cache = null;

	private void init( String path ) throws Exception {
		dataReaders = new HashMap<String, BufferedReader>();
		cur = new HashMap<String, String>();
		if (dataTypes.length == 0) {
			throw new Exception("No data type is specified");
		}
		for (String dataType : dataTypes) {
			try {
				String filename = new File( new File( path ),
						SFConstants.prefix + "." + dataType ).getPath();
				if (new File(filename).exists()) {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(
									new FileInputStream(filename), "UTF-8"));
					dataReaders.put(dataType, reader);
					cur.put(dataType, null);
				} else {
					throw new Exception("The data type [" + dataType
							+ "] does not exist. filename = "+filename);
				}
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void close() throws IOException {
		for (BufferedReader reader : dataReaders.values())
			reader.close();
	}

	/**
	 * read the next line from dataTypes[0], cache it for later use.
	 */
	@Override
	public boolean hasNext() {
		if (cache != null) {
			return true;
		} else {
			try {
				cache = dataReaders.get(dataTypes[0]).readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (cache == null) {
				cur =null ;
				return false;
			} else {
				return true;
			}
		}
	}

	@Override
	public Map<String, String> next() {
		try {
			if (cur==null) {
				return cur;
			}
			if (cache != null) {
				cur.put(dataTypes[0], cache);
				cache = null;
			} else {
				cur.put(dataTypes[0], dataReaders.get(dataTypes[0]).readLine());
			}
			for (int i = 1; i < dataTypes.length; ++i) {
				cur.put(dataTypes[i], dataReaders.get(dataTypes[i]).readLine());
			}
			return cur;
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
