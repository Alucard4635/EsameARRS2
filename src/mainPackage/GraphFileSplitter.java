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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.stream.Stream;

import javax.swing.JFileChooser;

import socialNetwork.AbstractNode;
import socialNetwork.DirectionalLink;
import socialNetwork.Graph;
import socialNetwork.Node;
import concurrenceClasses.StringFileWriter;
import concurrenceClasses.ThreadHomophiliaCalculatorOnMemory;
import concurrenceClasses.OrderedThreadPool;
import dataAnalysis.User;
import dataAnalysis.UserMap;


public class GraphFileSplitter {
	
private static final int SPLIT_NUMBER = 3;
private static final long USER_NUMBER = 1632803;
private static long[] idThreshold ;
private static final int WAIT_TIME = 50;
private static final int NUMBER_OF_THREAD = 8;
private static final int DISTANCE_TARGET = 2;
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
private static Graph graph;
private static AbstractNode caller;
private static File directorySplit;
private static int numberOfZero= (int) Math.log10(SPLIT_NUMBER);
private static int numberOfFileCreated=0;

public static void main(String[] args) {
	networkFile = null;
	try {
		networkFile = getFile(args);
		profileInfoFile = getFile(args);
		graphCreation();
		System.out.println("Splitting");
		splitGraph();
		System.out.println("Fine");
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

private static void graphCreation() throws IOException {
	/*File fileCache = new File("GraphOf"+networkFile.getName());
	if (fileCache.exists()) {
		try {
			System.out.println("trovatoFileDiCahce");
			System.out.println("Loading:");
			System.out.println("0%");
			loadCache(fileCache);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}else{*/
		BufferedReader reader=new BufferedReader(new FileReader(networkFile));
		Stream<String> lines = reader.lines();
		long count=30622564;
		String link;
		StringTokenizer nodeToken;
		graph=new Graph();
		long line = 0;
		int percent=0;
		long p;
		double weight;
		for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
			link = (String) iterator.next();
			if (link==null) {
				break;
			}
			nodeToken=new StringTokenizer(link,"\t");
			String nodeOne = nodeToken.nextToken();
			String nodeTWO = nodeToken.nextToken();
			weight=0;
			if (nodeToken.hasMoreTokens()) {
				weight = Double.parseDouble( nodeToken.nextToken());
			}
			createGraphComponent(nodeOne, nodeTWO,weight);
			line+=100;
			p = (line)/count;
			if (p!=percent) {
				percent=(int)p;
				System.out.println(percent+"%");
				}
			}
		reader.close();
	//}
}

private static void writeCache(File input) throws IOException {
	FileOutputStream fos = new FileOutputStream("GraphOf"+input.getName());
	ObjectOutputStream oos = new ObjectOutputStream(fos);
	oos.writeObject(graph);
	oos.close();
}

private static void loadCache(File fileCache) throws IOException, ClassNotFoundException {
	FileInputStream fis = new FileInputStream(fileCache);
	ObjectInputStream ois = new ObjectInputStream(fis);
	graph = (Graph)  ois.readObject();
	ois.close();
}

private static void splitGraph() throws IOException {
	AbstractNode firstNode;
	LinkedList<AbstractNode> toVisit;
	KeySetView<String,AbstractNode> allNodes = graph.getNodes().keySet();
	LinkedList<AbstractNode> borderNode=new LinkedList<AbstractNode>();
	Collection<DirectionalLink> adiacency;
	AbstractNode currentNode;
	AbstractNode target;
	
	int size = allNodes.size();
	int percent=-1;
	int status;
	int nodeWrited = 0;
	int distance;
	int nodeCurrentDistance;
	String targetID;
	toVisit = new LinkedList<AbstractNode>();
	StringBuilder stringBuilder=null;

	for (String id : allNodes) {
		System.out.println(id);
		firstNode = graph.getNode(id);
		if (allNodes.remove(id)) {
			distance = 0;
			stringBuilder=getBuilder(stringBuilder);
			toVisit.add(firstNode);
			nodeCurrentDistance = 1;
			while (!toVisit.isEmpty()) {
				currentNode = toVisit.pop();
				adiacency=currentNode.getAdiacencyList();
				
				nodeCurrentDistance--;
				nodeWrited+=100;
				status = nodeWrited/size;
				
				for (DirectionalLink link : adiacency) {
					target = link.getTarget();
					targetID = target.getId();
					if (allNodes.remove(targetID)) {
						
						toVisit.add(target);
					}
					stringBuilder.append(currentNode.getId());
					stringBuilder.append("\t");
					stringBuilder.append(targetID);
					stringBuilder.append("\n");
					
				}
				if (status!=percent) {
					percent=status;
					System.out.println(percent+"%");
				}
				if(nodeCurrentDistance==0){
					distance++;
					System.out.println("dis: "+distance);
					nodeCurrentDistance=toVisit.size();
					if (distance==DISTANCE_TARGET) {
						stringBuilder=getBuilder(stringBuilder);
						distance = 0;
						nodeCurrentDistance=1;
					}
					
				}
			}
		}
	}
	
}



private static StringBuilder getBuilder(StringBuilder stringBuilder) throws IOException {
	if (stringBuilder!=null) {
		BufferedWriter fileToWrite = getNewFile();
		String toWrite = stringBuilder.toString();
		fileToWrite.write(toWrite);
		fileToWrite.flush();
		fileToWrite.close();
		
	}

	return new StringBuilder();
}

private static BufferedWriter getNewFile() throws IOException {
	StringBuilder stringBuilder;
	String nameNetworkFile = networkFile.getName();
	if (directorySplit==null) {
		stringBuilder= new StringBuilder();
		stringBuilder.append(networkFile.getParent());
		stringBuilder.append("\\GraphSplitOf");
		stringBuilder.append(nameNetworkFile);
		directorySplit = new File(stringBuilder.toString());
		directorySplit.mkdir();
	}
	stringBuilder = new StringBuilder();
	stringBuilder.append(directorySplit.getAbsolutePath());
	stringBuilder.append("\\");
	stringBuilder.append(numberOfFileCreated);
	stringBuilder.append("PartOf");
	stringBuilder.append(nameNetworkFile);
	numberOfFileCreated++;
	File file = new File(stringBuilder.toString());
	file.createNewFile();
	System.out.println("Creato "+file.getAbsolutePath());
	
	return new BufferedWriter(new FileWriter(file));
	
}

private static void createGraphComponent(String nodeID, String nodeTarget, double weight) {
	if (!nodeID.equals(caller)) {
		caller = graph.getNode(nodeID);
	}
	AbstractNode target=graph.getNode(nodeTarget);
	caller.directionalLink(target,weight);

}

private static void spltFile() throws IOException {
	BufferedReader reader=new BufferedReader(new FileReader(networkFile));
	Stream<String> lines = reader.lines();
	writers = inizializeWriterArray();
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
		//writeSplit(nodeID, nodeTarget);
	}
	completeWrite();
	System.out.println("SplitComplete");
}



private static void completeWrite() throws IOException {
	for (BufferedWriter bufferedWriter : writers) {
		bufferedWriter.flush();
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


private static void printPercentualeRandomAccessFile(RandomAccessFile reader) throws IOException {
	int percentuale = (int) (((double)(reader.getFilePointer()))/(reader.length()/100.0));
	if (percentuale!=percentualePrecedente) {
		percentualePrecedente=percentuale;
		System.out.println(percentuale+"%");
	}
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
