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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.stream.Stream;

import javax.swing.JFileChooser;

import socialNetwork.AbstractNode;
import socialNetwork.DirectionalLink;
import socialNetwork.Graph;
import socialNetwork.GraphParser;


public class GraphTriadricEvaluator {
	
private static final int DISTANCE_TARGET = 2;
private static File networkFile;
private static File directorySplit;
//private static int numberOfZero= (int) Math.log10(SPLIT_NUMBER);
private static int numberOfFileCreated=0;

public static void main(String[] args) {
	networkFile = null;
	try {
		networkFile = getFile(args);
		File fileCache = new File("CacheGraphOf"+networkFile.getName());
		Graph graph=null;
//		if (fileCache.exists()) {
//			graph=loadCacheGraph(fileCache);
//		}
		if (graph==null) {
			System.out.println("Creation of graph");
			graph=graphCreation();
//			writeCache(fileCache,graph);
		}
		System.out.println("Evaluation");
		ConcurrentHashMap<AbstractNode,Integer> toWritetriadic=calculateAllTriadricClousure(graph);
		File csv=new File("TriadicClousureOF"+networkFile.getName());
		writeCsv(toWritetriadic,csv);
		System.out.println("Fine");
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

private static void writeCsv(ConcurrentHashMap<AbstractNode,Integer> toWrite,File file) throws IOException {
	BufferedWriter writer=new BufferedWriter(new FileWriter(file));
	KeySetView<AbstractNode, Integer> keySet = toWrite.keySet();
	for (AbstractNode aNode : keySet) {
		writer.append(aNode.getId()+","+toWrite.get(aNode)+"\n");
	}
	writer.flush();
	writer.close();
}

private static ConcurrentHashMap<AbstractNode,Integer> calculateAllTriadricClousure(
		Graph graph) {
	Collection<AbstractNode> nodes = graph.getNodes().values();
	ConcurrentHashMap<AbstractNode, Integer> result=new ConcurrentHashMap<AbstractNode, Integer>();
	for (AbstractNode aNode : nodes) {
		double triadricClosureValue = aNode.triadricClosure();
		result.put(aNode, (int) (triadricClosureValue*100));
	}
	return result;
}

private static Graph loadCacheGraph(File fileCache) throws IOException {
	Graph graph = null;
	try {
		System.out.println("trovatoFileDiCahce");
		System.out.println("Loading:");
		System.out.println("0%");
		graph = (Graph) loadCache(fileCache);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	}
		System.out.println("100%");

	return graph;
}

private static Graph graphCreation() throws IOException {
	Graph graph = null;
	
		BufferedReader reader=new BufferedReader(new FileReader(networkFile));
		Stream<String> lines = reader.lines();
		long count=lines.count();
		long line = 0;
		int percent=0;
		long percentTmp;
		reader.close();

		reader=new BufferedReader(new FileReader(networkFile));
		lines = reader.lines();
		graph = GraphParser.parse(lines, "\t");
		reader.close();
		
		
		line+=100;
		percentTmp = (line)/count;
		if (percentTmp!=percent) {
			percent=(int)percentTmp;
			System.out.println(percent+"%");
		}
	return graph;
}



private static void writeCache(File input,Object graph) throws IOException {
	FileOutputStream fos = new FileOutputStream(input.getName());
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
