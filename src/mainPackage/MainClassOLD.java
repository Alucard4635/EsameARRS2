package mainPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import concurrenceClasses.OrderedThreadPool;
import concurrenceClasses.ThreadHomophiliaCalculator;
import dataAnalysis.DataArray;
import dataAnalysis.DataArrayException;
import dataAnalysis.User;
import dataAnalysis.DataArray.OrderType;
import dataAnalysis.DataArrayException.DataExceptionType;


public class MainClassOLD {
	
private static final String DATA_ARRAY_SEPARATING_CHARS = "\t";
private static final int DATA_ARRAY_STARTING_SIZE = 1632803;
/*private static final int WAIT_TIME = 50;
private static final int NUMBER_OF_THREAD = 8;
private static StringBuilder buffer=new StringBuilder();
private static OrderedThreadPool<ThreadHomophiliaCalculator> pool;
private static Object ThreadNodeIDString;
private static Collection<Long> threadNodeTargetList;*/
private static final String RELATION_SEPARATING_CHARS = "\t";
private static DataArray profiles;
private static User userCaller;
private static User userTarget;
private static double percentualePrecedente=-1;

public static void main(String[] args) {
	File networkFile = null;
	File profileInfoFile=null;

	try {
		JOptionPane.showMessageDialog(null, "Select Tab Separated Network File", "Network", JOptionPane.INFORMATION_MESSAGE);
		networkFile = getFile(args);
		JOptionPane.showMessageDialog(null, "Select Line Separated Proflie File", "Profile Info", JOptionPane.INFORMATION_MESSAGE);
		profileInfoFile = getFile(args);
		writeHomofiliaValues(networkFile,profileInfoFile);
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


private static void writeHomofiliaValues(File networkFile, File profileInfoFile)
		throws FileNotFoundException, IOException, DataArrayException{
	RandomAccessFile reader=new RandomAccessFile(networkFile, "r");
	System.out.println("Indicizzo");
	profiles=new DataArray(profileInfoFile,DATA_ARRAY_STARTING_SIZE,OrderType.BY_FIRST_WORD,DATA_ARRAY_SEPARATING_CHARS);
	System.out.println("Inizio Analisi");
	BufferedWriter writer=new BufferedWriter(new FileWriter("ResultOf"+networkFile.getName()));
	
//	pool=new OrderedThreadPool<ThreadHomophiliaCalculator>(NUMBER_OF_THREAD,WAIT_TIME);
//	threadNodeTargetList=new LinkedList<Long>();
	
	long length = reader.length();
	String arcToBuild;
	StringTokenizer nodeToken;
	long filePointer = reader.getFilePointer();
	String nodeIDString;
	String nodeTargetString;
	while (filePointer<length) {
		arcToBuild = reader.readLine();
		if (arcToBuild==null) {
			break;
		}
		nodeToken=new StringTokenizer(arcToBuild,RELATION_SEPARATING_CHARS);
		nodeIDString = nodeToken.nextToken();
		nodeTargetString = nodeToken.nextToken();
		
//		writeConcurrency(nodeIDString,nodeTargetString,writer, reader);
		try {
			writer.append(nodeIDString+","+nodeTargetString+","+(Integer.toString((int)(calculateHomofilia(nodeIDString,nodeTargetString)*100.0)))+"\n");
		} catch (Exception e) {
		}
		printPercentuale(reader);
	}
	writer.flush();
	writer.close();
//	completeWriting(writer);
	System.out.println("Fine");

}

/*private static void completeWriting(RandomAccessFile writer) throws IOException {
	Collection<ThreadHomophiliaCalculator> threadUnwited = pool.getThread();
	for (ThreadHomophiliaCalculator calculator : threadUnwited) {
		if (calculator!=null) {
			writer.writeBytes(calculator.getResult().toString());
		}
	}
	writer.close();
}*/

private static void printPercentuale(RandomAccessFile reader) throws IOException {
	int percentuale = (int) (((double)(reader.getFilePointer()))/(reader.length()/100.0));
	if (percentuale!=percentualePrecedente) {
		percentualePrecedente=percentuale;
		System.out.println(percentuale+"%");
	}
}



/*private static void writeConcurrency(String nodeIDString,
		String nodeTargetString, RandomAccessFile writer, RandomAccessFile reader) {
	if (ThreadNodeIDString==null) {
		ThreadNodeIDString=nodeIDString;
	}
	if (nodeIDString.equals(ThreadNodeIDString)) {
		threadNodeTargetList.add(Long.parseLong(nodeTargetString));
	}else {
		ThreadNodeIDString=null;
		try {
			ThreadHomophiliaCalculator replaceThread = pool.replaceThread(
					new ThreadHomophiliaCalculator(Long.parseLong(nodeIDString), threadNodeTargetList, profiles));
			if (replaceThread!=null) {
				writer.writeBytes(replaceThread.getResult().toString());
				printPercentuale(reader);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		threadNodeTargetList=new LinkedList<Long>();
	}

private static void write(RandomAccessFile writer, String string) throws IOException {
	buffer.append(string);
	if (buffer.length()>2048) {
		writer.writeBytes(buffer.toString());
		buffer=new StringBuilder();
	}
	
}
 */

private static double calculateHomofilia(String nodeIDString, String nodeTargetString) throws IOException, DataArrayException {
	long nodeID=Long.parseLong(nodeIDString);
	long nodeTarget=Long.parseLong(nodeTargetString);
	if (userCaller==null) {
		userCaller = new User();
		String profileInfo = profiles.get(nodeID);
		userCaller.parseUser(profileInfo);
	}else {
		if(!(userCaller.getUser_id()==nodeID)) {
			String profileInfo = profiles.get(nodeID);
			userCaller.parseUser(profileInfo);
		}
	}
	
	String profileTarget;
	if (userTarget==null) {
		userTarget = new User();
		profileTarget = profiles.get(nodeTarget);
		userTarget.parseUser(profileTarget);
	}else{
		if(!(userTarget.getUser_id()==nodeTarget)) {
			profileTarget = profiles.get(nodeTarget);
			userTarget.parseUser(profileTarget);
		}
	}

	return userCaller.calculateHomophiliaMacth(userTarget);
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
