package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * A utility class for easier file operations.
 * @author xiaoling
 *
 */
public class FileUtil {
	public static String getTextFromFile(String filename){
		StringBuilder sb = new StringBuilder();
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			
			String line = null;
			while ((line = reader.readLine())!=null){
				sb.append(line+"\n");
			}
			reader.close();
		}catch(Exception e){e.printStackTrace();}
		return sb.toString();
	}
	
	public static void writeTextToFile(String text, String filename){
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter
                    (new FileOutputStream(filename),"UTF8"));
			pw.print(text);
			pw.close();
		}catch(Exception e){e.printStackTrace();}
	}
	
	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }
	static public void copyFile(String src, String dst){
		writeTextToFile(getTextFromFile(src), dst);
	}
	
	public static String joinPaths( String... paths ) {
		if ( paths.length == 0 )
			return "";
		File f = null;
		for ( String path : paths ) {
			if ( f == null ) f = new File( path );
			else f = new File( f, path );
		}
		return f.toString();
	}
}
