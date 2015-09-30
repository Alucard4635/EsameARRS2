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

import concurrenceClasses.OrderedThreadPool;
import concurrenceClasses.ThreadHomophiliaCalculator;
import dataAnalysis.DataArray;
import dataAnalysis.DataArrayException;
import dataAnalysis.User;
import dataAnalysis.DataArray.OrderType;
import dataAnalysis.DataArrayException.DataExceptionType;


public class profileExtractor {
	
public static void main(String[] args) {
	File networkFile = null;
	File profileInfoFile=null;
	try {
		networkFile = getFile(args);
		profileInfoFile = getFile(args);
		getProfiles(networkFile,profileInfoFile);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (DataArrayException e) {
		if (e.getType().equals(DataExceptionType.INCORRECT_FORMAT)) {
			JOptionPane.showMessageDialog(null, "Formato non corretto", "format exception", JOptionPane.ERROR_MESSAGE);
		}else {
			if (e.getType().equals(DataExceptionType.INDEX_NOT_FOUND)) {
				JOptionPane.showMessageDialog(null, "Utente non trovato", "Id not found", JOptionPane.ERROR_MESSAGE);
			}
		}
		e.printStackTrace();
	}
	
}




private static void getProfiles(File networkFile, File profileInfoFile) throws IOException, DataArrayException {
//	BufferedReader reader=new BufferedReader(new FileReader(networkFile));
	RandomAccessFile reader=new RandomAccessFile(networkFile,"r");

	System.out.println("Indicizzo");
	DataArray dataProfiles = new DataArray(profileInfoFile,1632803,OrderType.BY_FIRST_WORD,"\t");
	System.out.println("Inizio Analisi");
	File fileToWrite = new File("ProfileOf"+networkFile.getName());
	fileToWrite.createNewFile();
	BufferedWriter writer = new BufferedWriter(new FileWriter(fileToWrite));
	
	byte[] b = new byte[(int) reader.length()];
	reader.readFully(b);
	String all =new String(b);
	b=null;
	StringTokenizer t = new StringTokenizer(all);
	all=null;
	long percent=-1;
	int line = 0;
	int count = t.countTokens();
	long percentTmp;
	ConcurrentHashMap<String, String> profiles=new ConcurrentHashMap<String, String>();
	while (t.hasMoreTokens()) {
		
		String ID = t.nextToken();
		if (profiles.get(ID)==null) {
			profiles.put(ID, dataProfiles.get(Long.parseLong(ID)));
			writer.append(dataProfiles.get(Long.parseLong(ID))+"\n");
		}
		line+=100;
		percentTmp = (line)/count;
		if (percentTmp!=percent) {
			percent=(int)percentTmp;
			System.out.println(percent+"%");
			writer.flush();
		}
	}
	writer.flush();
	writer.close();
//	Stream<String> lines = reader.lines();
//	long count=lines.count();
//	long lineNum = 0;
//	int percent=0;
//	long percentTmp;
//	reader=new BufferedReader(new FileReader(networkFile));
//	lines = reader.lines();
//	StringTokenizer tok;
//	for (Iterator<String> iterator = lines.iterator(); iterator
//			.hasNext();) {
//		String line = iterator.next();
//		tok = new StringTokenizer(line);
//		tok.nextToken();
//		tok.nextToken();
//
//		
//		lineNum+=100;
//		percentTmp = (lineNum)/count;
//		if (percentTmp!=percent) {
//			percent=(int)percentTmp;
//			System.out.println(percent+"%");
//		}
//	}
//		nodeToken=new StringTokenizer(line,"	\t");
//		nodeIDString = nodeToken.nextToken();
//	//	writer.append(profiles.get(Long.parseLong(nodeIDString)));
//		nodeTargetString = nodeToken.nextToken();
//	//	writer.append(profiles.get(Long.parseLong(nodeTargetString)));
//
//
//	}
//	writer.flush();
//	writer.close();

	
}




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
