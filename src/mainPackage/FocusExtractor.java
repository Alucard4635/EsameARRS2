package mainPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import socialNetwork.Focus;
import concurrenceClasses.OrderedThreadPool;
import concurrenceClasses.ThreadHomophiliaCalculator;
import dataAnalysis.AnalyzableData;
import dataAnalysis.DataArray;
import dataAnalysis.DataArrayException;
import dataAnalysis.User;
import dataAnalysis.DataArray.OrderType;
import dataAnalysis.DataArrayException.DataExceptionType;
import dataAnalysis.User.ProfileAttributesField;


public class FocusExtractor {
	
public static void main(String[] args) {
	File networkFile = null;
	File profileInfoFile=null;
	try {
		networkFile = getFile(args);
		profileInfoFile = getFile(args);
		//getFocus(profileInfoFile);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
}




//private static ConcurrentHashMap<String, Focus> getFocus(File profileInfoFile) throws IOException, DataArrayException {
////	BufferedReader reader=new BufferedReader(new FileReader(networkFile));
//	RandomAccessFile reader=new RandomAccessFile(profileInfoFile,"r");
//	System.out.println("Inizio Analisi");
//	
//	byte[] b = new byte[(int) reader.length()];
//	reader.readFully(b);
//	String all =new String(b);
//	b=null;
//	StringTokenizer rows = new StringTokenizer(all,"\n");
//	all=null;
//	long percent=-1;
//	int line = 0;
//	int count = rows.countTokens();
//	long percentTmp;
//	ConcurrentHashMap<String, String> profiles=new ConcurrentHashMap<String, String>();
//	String focusName;
//	String content;
//	int regionCounter=0;
//	while (rows.hasMoreTokens()) {
//		StringTokenizer field=new StringTokenizer(rows.nextToken(), "\t");
//		StringTokenizer minorField;
//		field.nextToken();//skip ID
//		content="PUBLIC"+field.nextToken();
//		focusName = profiles.get(content);//PUBLIC
//		addToHash(profiles, focusName, content);
//		
//		field.nextToken();//skip percentual
//		
//		content="isMALE"+field.nextToken();
//		focusName = profiles.get(content);//ISMALE
//		addToHash(profiles, focusName, content);
//		
//		content=field.nextToken();
//		focusName = profiles.get(content);//REGION
//		if (focusName==null) {
//			profiles.put(content, "REGION"+regionCounter++);
//		}
//
//		field.nextToken();//skip last_login=
//		field.nextToken();//skip registration
//		
//		content="AGE"+field.nextToken();
//		focusName = profiles.get(content);//AGE
//		addToHash(profiles, focusName, content);
//
//		content = field.nextToken();
//		minorField=new StringTokenizer(content, ",");
//		if (minorField.hasMoreTokens()) {
//			content = "HEIGHT"+minorField.nextToken();//HEIGHT
//			focusName =  profiles.get(content);
//			addToHash(profiles, focusName, content);
//		}
//		if (minorField.hasMoreTokens()) {
//			content ="WEIGHT"+minorField.nextToken();//PARSEWEIGHT 
//			focusName =  profiles.get(content);
//			addToHash(profiles, focusName, content);
//		}
//		
//		int attributeIndex = 0;
// 		int minorIndex=0;
//		ProfileAttributesField[] attributes = User.ProfileAttributesField.values();
//		while (field.hasMoreTokens()) {
//			String attributiveField =  field.nextToken();
//			if (!attributiveField.equals("null")) {
//				minorField=new StringTokenizer(attributiveField, ",");
//				minorIndex=0;
//				while(minorField.hasMoreTokens()){
//					String minor = minorField.nextToken();
//					while (minor.startsWith(" ")&&minor.length()>1) {
//						minor=minor.substring(1);
//					}
//					String minorAttribute =attributes[attributeIndex].get(minorIndex).toString()
//					content =minorAttribute+minor;
//					focusName = profiles.get(content);
//					if (focusName==null) {
//					profiles.put(content, minorAttribute+attributeCounter++);
//					}
//					minorIndex++
//
//				}
//			}
//			attributeIndex++;
//		}
//	}
//
//
//	
//}




private static void addToHash(ConcurrentHashMap<String, String> profiles,
		String focusName, String content) {
	if (focusName==null) {
		profiles.put(content, content);
	}
}


File fileToWrite = new File("FocusOf"+networkFile.getName());
fileToWrite.createNewFile();
BufferedWriter writer = new BufferedWriter(new FileWriter(fileToWrite));


private static File getFile(String[] args) throws IOException {
	String filename = null;
	File selectedFile = null;
	
	if (args.length > 0) {
		filename = args[0];
	}
	JFileChooser fc=null;
	fc = new JFileChooser(new File(".").getCanonicalPath());
	fc.setMultiSelectionEnabled(false);
	int responce = fc.showOpenDialog(null);
	if (responce == JFileChooser.APPROVE_OPTION) {
		selectedFile = fc.getSelectedFile();

	} else if (responce == JFileChooser.ERROR_OPTION) {
		System.err.println("error while opening JFileChooser");
		System.out.println("Enter File Name: ");
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(System.in));
			filename = bufferedReader.readLine();
	} else {
		System.exit(0);
	}
	if (selectedFile==null) {
		selectedFile=new File(filename);
	}
	return selectedFile;
}
}
