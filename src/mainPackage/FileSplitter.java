package mainPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import javax.swing.JFileChooser;

import concurrenceClasses.ThreadHomophiliaCalculatorOnMemory;
import concurrenceClasses.OrderedThreadPool;
import dataAnalysis.User;
import dataAnalysis.UserMap;
import dataAnalysis.UserMapException;


public class FileSplitter {
	
private static final int SPLIT_NUMBER = 5;
private static final long USER_NUMBER = 1632803;
private static long[] idThreshold ;
private static final int WAIT_TIME = 50;
private static final int NUMBER_OF_THREAD = 8;
private static int topUserCurrentWriter=1;
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
private static BufferedWriter[] writers;
private static long lineNumber;
private static int currentLineNumber;
private static int partialLineNumber;
private static long averageLineNumber;
private static long maxID;

public static void main(String[] args) {
	networkFile = null;
	try {
		networkFile = getFile(args);
		profileInfoFile = getFile(args);
		//splitFile(networkFile,profileInfoFile);
		spltFile();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
//	 catch (UserMapException e) {
//		if (e.getType().equals(DataExceptionType.INCORRECT_FORMAT)) {
//			JOptionPane.showMessageDialog(null, "Formato non corretto", "format exception", JOptionPane.ERROR_MESSAGE);
//		}else {
//			if (e.getType().equals(DataExceptionType.INDEX_NOT_FOUND)) {
//				JOptionPane.showMessageDialog(null, "Utente non trovato", "Id not found", JOptionPane.ERROR_MESSAGE);
//			}
//		}
//		e.printStackTrace();
//	}
}

private static void spltFile() throws IOException {
	BufferedReader reader=new BufferedReader(new FileReader(networkFile));
	Stream<String> lines = reader.lines();
	writers = inizializeWriterArray();
	idThreshold=inizializeThreshold();
	//lineNumber = lines.count();
	averageLineNumber = lineNumber/SPLIT_NUMBER+1;
	partialLineNumber=0;
	String link;
	StringTokenizer nodeToken;
	long nodeID;
	long nodeTarget;
//	for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
//		link = (String) iterator.next();
//		if (link==null) {
//			break;
//		}
//		nodeToken=new StringTokenizer(link,"\t");
//		nodeID = Long.parseLong(nodeToken.nextToken());
//		nodeTarget = Long.parseLong(nodeToken.nextToken());
//		evaluateThreshold(nodeID, nodeTarget);
//	}
//	reader=new BufferedReader(new FileReader(networkFile));
//	lines = reader.lines();
	for (Iterator<String> iterator2 = lines.iterator(); iterator2.hasNext();) {
		link = (String) iterator2.next();
		if (link==null) {
			break;
		}
		nodeToken=new StringTokenizer(link,"\t");
		nodeID = Long.parseLong(nodeToken.nextToken());
		nodeTarget = Long.parseLong(nodeToken.nextToken());
		writeSplit(nodeID, nodeTarget);
	}
	completeWrite();
	System.out.println("SplitComplete");
}

private static long[] inizializeThreshold() {
	long[] t = new long[SPLIT_NUMBER+1];
	for (int i = 0; i < t.length; i++) {
		t[i]=((USER_NUMBER/SPLIT_NUMBER)+1)*(i);
	}
	return t;
}

private static void evaluateThreshold(long nodeID, long nodeTarget) {
	if (nodeID>idThreshold[topUserCurrentWriter]) {
		topUserCurrentWriter++;
	}
	if (nodeTarget>idThreshold[topUserCurrentWriter]||nodeTarget<idThreshold[topUserCurrentWriter-1]) {
		//writers[writers.length-1].write(stringBuilder.toString());
	}else {
		//writers[topUserCurrentWriter-1].write(stringBuilder.toString());
	}
}

private static void completeWrite() throws IOException {
	for (BufferedWriter bufferedWriter : writers) {
		bufferedWriter.flush();
	}
	
}




private static void writeSplit(long nodeID, long nodeTarget) throws IOException {
	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append(nodeID);
	stringBuilder.append("	");
	stringBuilder.append(nodeTarget);
	stringBuilder.append("\n");

	
	if (nodeID>idThreshold[topUserCurrentWriter]) {
		topUserCurrentWriter++;
	}
	if (nodeTarget>idThreshold[topUserCurrentWriter]||nodeTarget<idThreshold[topUserCurrentWriter-1]) {
		writers[writers.length-1].write(stringBuilder.toString());
	}else {
		writers[topUserCurrentWriter-1].write(stringBuilder.toString());
	}
	
}
private static BufferedWriter[] inizializeWriterArray() throws IOException {
	BufferedWriter[] result = new BufferedWriter[SPLIT_NUMBER+1];
	String nameNetworkFile = networkFile.getName();
	String path = networkFile.getParent();
	StringBuilder stringBuilder= new StringBuilder();
	stringBuilder.append(path);
	stringBuilder.append("\\SplitOf");
	stringBuilder.append(nameNetworkFile);
	File dir = new File(stringBuilder.toString());
	dir.mkdir();
	int numberOfZero = (int) Math.log10(SPLIT_NUMBER);
	
	for (int i = 1; i <=result.length; i++) {
		stringBuilder = new StringBuilder();
		stringBuilder.append(dir.getAbsolutePath());
		stringBuilder.append("\\");
		for (int j = numberOfZero-(int) Math.log10(i); j > 0; j--) {
			stringBuilder.append("0");
		}
		stringBuilder.append(i);
		stringBuilder.append("PartOf");
		stringBuilder.append(nameNetworkFile);
		result[i-1]=new BufferedWriter(new FileWriter(new File(stringBuilder.toString())));
	}
	return result;
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

	
	if(userTarget==null||!(userTarget.getUser_id()==nodeTarget)) {
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
