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
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.stream.Stream;

import javax.swing.JFileChooser;

import socialNetwork.AbstractNode;
import socialNetwork.DirectionalLink;
import socialNetwork.Graph;
import socialNetwork.GraphParser;


public class GraphFileSplitter {
	
private static final int DISTANCE_TARGET = 2;
private static File networkFile;
private static Graph graph;
private static File directorySplit;
//private static int numberOfZero= (int) Math.log10(SPLIT_NUMBER);
private static int numberOfFileCreated=0;

public static void main(String[] args) {
	networkFile = null;
	try {
		networkFile = getFile(args);
		graph=graphCreation();
		System.out.println("Splitting");
		splitGraph();
		System.out.println("Fine");
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

private static Graph graphCreation() throws IOException {
	Graph graph = null;
	File fileCache = new File("GraphOf"+networkFile.getName());
	if (fileCache.exists()) {
		try {
			System.out.println("trovatoFileDiCahce");
			System.out.println("Loading:");
			System.out.println("0%");
			graph=(Graph) loadCache(fileCache);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}else{
		BufferedReader reader=new BufferedReader(new FileReader(networkFile));
		Stream<String> lines = reader.lines();
		long count=lines.count();
		long line = 0;
		int percent=0;
		long percentTmp;
		
		reader=new BufferedReader(new FileReader(networkFile));
		lines = reader.lines();
		
		graph = GraphParser.parse(lines, "\t");
		
		reader.close();
		writeCache(fileCache);
		line+=100;
		percentTmp = (line)/count;
		if (percentTmp!=percent) {
			percent=(int)percentTmp;
			System.out.println(percent+"%");
		}
	}
	return graph;
}




private static void splitGraph() throws IOException {
	AbstractNode firstNode;
	LinkedList<AbstractNode> toVisit;
	KeySetView<String,AbstractNode> allNodes = graph.getNodes().keySet();
	Collection<DirectionalLink> adiacency;
	AbstractNode currentNode;
	AbstractNode target;
	
	
	int size = allNodes.size();
	int percent=-1;
	int status;
	int nodeWrited = 0;
	int distance;
	int nodeCurrentDistance;
	ConcurrentHashMap<String, BufferedWriter> fileHash=new ConcurrentHashMap<String, BufferedWriter>();
	String targetID;
	toVisit = new LinkedList<AbstractNode>();
	BufferedWriter fileToWrite = null;

	//StringBuilder stringBuilder=null;

	for (String id : allNodes) {
		System.out.println(id);
		firstNode = graph.getNode(id);
		if (allNodes.remove(id)) {
			distance = 0;
			fileToWrite=flush(fileToWrite);
			//stringBuilder=getBuilder(stringBuilder);
			toVisit.add(firstNode);
			nodeCurrentDistance = 1;
			while (!toVisit.isEmpty()) {
				currentNode = toVisit.pop();
				adiacency=currentNode.getAdiacencyList();
				fileHash.put(currentNode.getId(), fileToWrite);

				//add this node to filehash
				
				nodeCurrentDistance--;
				nodeWrited+=100;
				status = nodeWrited/size;
				
				
				for (DirectionalLink link : adiacency) {
					target = link.getTarget();
					targetID = target.getId();
					if (allNodes.remove(targetID)) {
						if (fileHash.get(targetID)==null) {
							fileHash.put(targetID, fileToWrite);
						}
						
						toVisit.add(target);
						
						appendLink(currentNode, targetID, fileToWrite);

					}else {
						BufferedWriter oldFile = fileHash.get(targetID);
						if (fileToWrite.equals(oldFile)) {
							appendLink(currentNode, targetID, oldFile);
							fileHash.put(targetID, oldFile);
						}else {
							appendLink(currentNode, targetID, fileToWrite);
							fileHash.put(targetID, fileToWrite);
						}
					}
//					stringBuilder.append(currentNode.getId());
//					stringBuilder.append("\t");
//					stringBuilder.append(targetID);
//					stringBuilder.append("\n");
					
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
						fileToWrite=flush(fileToWrite);
						//stringBuilder=getBuilder(stringBuilder);
						distance = 0;
						nodeCurrentDistance=1;
					}
					
				}
			}
		}
	}
	flushAllAndClose(fileHash.values());
	
}

private static void flushAllAndClose(Collection<BufferedWriter> values) throws IOException {
	for (BufferedWriter bufferedWriter : values) {
		bufferedWriter.flush();
		bufferedWriter.close();
	}
}

private static void appendLink(AbstractNode currentNode, String targetID,
		BufferedWriter oldFile) throws IOException {
	oldFile.append(currentNode.getId());
	oldFile.append("\t");
	oldFile.append(targetID);
	oldFile.append("\n");
}



private static BufferedWriter flush(BufferedWriter fileToWrite) throws IOException {
	if (fileToWrite!=null) {
		fileToWrite.flush();
	}
	return getNewFile();
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

private static void writeCache(File input) throws IOException {
	FileOutputStream fos = new FileOutputStream("GraphOf"+input.getName());
	ObjectOutputStream oos = new ObjectOutputStream(fos);
	oos.writeObject(graph);
	oos.close();
}

private static Object loadCache(File fileCache) throws IOException, ClassNotFoundException {
	FileInputStream fis = new FileInputStream(fileCache);
	ObjectInputStream ois = new ObjectInputStream(fis);
	Object obj =  ois.readObject();
	ois.close();
	return obj;
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
