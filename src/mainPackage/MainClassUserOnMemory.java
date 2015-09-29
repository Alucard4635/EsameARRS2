package mainPackage;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import concurrenceClasses.ThreadHomophiliaCalculatorOnMemory;
import concurrenceClasses.OrderedThreadPool;
import dataAnalysis.User;
import dataAnalysis.UserMap;
import dataAnalysis.UserMapException;
import dataAnalysis.UserMap.OrderType;
import dataAnalysis.UserMapException.DataExceptionType;


public class MainClassUserOnMemory {
	
private static final int WAIT_TIME = 50;
private static final int NUMBER_OF_THREAD = 8;
private static File networkFile;
private static File profileInfoFile;
private static UserMap profiles;
private static User userCaller;
private static User userTarget;
private static double percentualePrecedente=-1;
private static StringBuilder buffer=new StringBuilder();
private static OrderedThreadPool<ThreadHomophiliaCalculatorOnMemory> pool;
private static long ThreadNodeIDString=-1;
private static Collection<Long> threadNodeTargetList;

public static void main(String[] args) {
	networkFile = null;
	try {
		networkFile = getFile(args);
		profileInfoFile = getFile(args);
		//splitFile(networkFile,profileInfoFile);
		writeHomofiliaValues();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (UserMapException e) {
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

private static void writeHomofiliaValues()
		throws FileNotFoundException, IOException, UserMapException{
	RandomAccessFile reader=new RandomAccessFile(networkFile, "r");
	System.out.println("Indicizzo");
	profiles=new UserMap(profileInfoFile,1632803,OrderType.BY_FIRST_WORD);
	System.out.println("Inizio Analisi");
	RandomAccessFile writer=new RandomAccessFile(new File("ResultOf"+networkFile.getName()), "rw");
	
	pool=new OrderedThreadPool<ThreadHomophiliaCalculatorOnMemory>(NUMBER_OF_THREAD,WAIT_TIME);
	threadNodeTargetList=new LinkedList<Long>();
	
	long length = reader.length();
	String arcToBuild;
	StringTokenizer nodeToken;
	long filePointer = reader.getFilePointer();
	long nodeID;
	long nodeTarget;
	while (filePointer<length) {
		arcToBuild = reader.readLine();
		if (arcToBuild==null) {
			break;
		}
		nodeToken=new StringTokenizer(arcToBuild,"\t");
		nodeID = Long.parseLong(nodeToken.nextToken());
		nodeTarget = Long.parseLong(nodeToken.nextToken());
		writeConcurrency(nodeID,nodeTarget,writer, reader);

	}
	completeWriting(writer);
	System.out.println("Fine");

}

private static void completeWriting(RandomAccessFile writer) throws IOException {
	Collection<ThreadHomophiliaCalculatorOnMemory> threadUnwited = pool.getThread();
	for (ThreadHomophiliaCalculatorOnMemory calculator : threadUnwited) {
		if (calculator!=null) {
			writer.writeBytes(calculator.getResult().toString());
		}
	}
	writer.close();
}

private static void printPercentuale(RandomAccessFile reader) throws IOException {
	int percentuale = (int) (((double)(reader.getFilePointer()))/(reader.length()/100.0));
	if (percentuale!=percentualePrecedente) {
		percentualePrecedente=percentuale;
		System.out.println(percentuale+"%");
	}
}



private static void writeConcurrency(long nodeID,
		long nodeTarget, RandomAccessFile writer, RandomAccessFile reader) {
	if (ThreadNodeIDString==-1) {
		ThreadNodeIDString=nodeID;
	}
	if (nodeID==ThreadNodeIDString) {
		threadNodeTargetList.add(nodeTarget);
	}else {
		ThreadNodeIDString=-1;
		try {
			ThreadHomophiliaCalculatorOnMemory replaceThread = pool.replaceThread(
					new ThreadHomophiliaCalculatorOnMemory(nodeID, threadNodeTargetList, profiles));
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
}

private static void write(RandomAccessFile writer, String string) throws IOException {
	buffer.append(string);
	if (buffer.length()>2048) {
		writer.writeBytes(buffer.toString());
		buffer=new StringBuilder();
	}
	
}

private static double calculateHomofilia(long nodeID, long nodeTarget) throws IOException, UserMapException {

	
	if(userCaller==null||!(userCaller.getUser_id()==(nodeID))) {
		userCaller = profiles.get(nodeID);
	}

	
	if(userTarget==null||!(userCaller.getUser_id()==(nodeID))) {
		userTarget=profiles.get(nodeTarget);
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
