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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import dataAnalysis.User;
import dataAnalysis.User.ProfileAttributesField;
import socialNetwork.AbstractNode;
import socialNetwork.DirectionalLink;
import socialNetwork.Focus;
import socialNetwork.Graph;
import socialNetwork.GraphParser;


public class GraphEvaluator {

	private static final String DELIM = ",";
	private static final String WEIGHT = "WEIGHT";
	private static final String HEIGHT = "HEIGHT";
	private static final String AGE = "AGE";
	private static final String REGION = "REGION";
	private static final String IS_MALE = "isMALE";
	private static final String PUBLIC = "PUBLIC";
	private static final int NUMBER_OF_NUMBER_CATEGORY = 10;

public static void main(String[] args) {
	File networkFile = null;
	File focusFile=null;
	File profileInfoFile=null;
	try {
		JOptionPane.showMessageDialog(null, "Select Comma Separated Network File", "Network", JOptionPane.INFORMATION_MESSAGE);
		networkFile = getFile(args);
		JOptionPane.showMessageDialog(null, "Select Line Separated Proflie File", "Profile Info", JOptionPane.INFORMATION_MESSAGE);
		profileInfoFile = getFile(args);
		focusFile=new File("FocusNetworkOf"+profileInfoFile.getName());
		if (!focusFile.exists()) {
			focusFile=writeFocus(profileInfoFile);
		}
		Graph graph=null;
//		File fileCache = new File("CacheGraphOf"+networkFile.getName());
//		if (fileCache.exists()) {
//			graph=loadCacheGraph(fileCache);
//		}
		if (graph==null) {
			System.out.println(">Creation of graph");
			graph=graphCreation(networkFile,focusFile);
			
//			writeCache(fileCache,graph);
		}
		System.out.println(">Evaluation");
		ConcurrentHashMap<AbstractNode,Integer> toWriteTriadic=calculateAllTriadricClousure(graph);
		File csv=new File("TriadicClousureOF"+networkFile.getName());
		writeCsvTriadicClousure(toWriteTriadic,csv);
		toWriteTriadic=null;
		
		ConcurrentHashMap<Focus,Integer> toWrite=calculateAllFocusClousure(graph);
		csv=new File("FocusClousureOF"+networkFile.getName());
		writeCsvFocusClousure(toWrite,csv);
		toWrite=null;
		
		LinkedList<Object[]> toWriteMembership=calculateAllMembershipClousure(graph);
		csv=new File("MembershipClousureOF"+networkFile.getName());
		writeCsvMembershipClousure(toWriteMembership,csv);
		
		System.out.println("Fine");
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

private static void writeCsvMembershipClousure(LinkedList<Object[]> toWriteMembership,
		File csv) throws IOException {
	BufferedWriter writer=new BufferedWriter(new FileWriter(csv));
	for (int i = 0; i < toWriteMembership.size(); i++) {
		Object[] line = toWriteMembership.pop();
		writer.append(line[0]+DELIM+line[1]+DELIM+line[2]+"\n");
	}
	writer.flush();
	writer.close();
	
}

private static LinkedList<Object[]> calculateAllMembershipClousure(
		Graph graph) {
	Collection<Focus> focusList = graph.getFocus().values();
	LinkedList<Object[]> resultList=new LinkedList<Object[]>();
	Object[] result;
	ConcurrentHashMap<String, AbstractNode> nodes;
	Collection<AbstractNode> values;
	double membershipClousure;
	for (Focus aFocus : focusList) {
		nodes = aFocus.getNodes();
		values = nodes.values();
		for (AbstractNode aEnrolledNode : values) {
			membershipClousure = aFocus.getMembershipClousure(aEnrolledNode);
			result=new Object[3];
			result[0]=aEnrolledNode ;
			result[1]=aFocus;
			result[2]=(int) (membershipClousure*100);
			resultList.add(result);
		}
	}
	return resultList;
}

private static void writeCsvTriadicClousure(ConcurrentHashMap<AbstractNode, Integer> toWritetriadic, File csv) throws IOException {
	BufferedWriter writer=new BufferedWriter(new FileWriter(csv));
	KeySetView<AbstractNode, Integer> keySet = toWritetriadic.keySet();
	for (AbstractNode aNode : keySet) {
		writer.append(aNode.getId()+DELIM+toWritetriadic.get(aNode)+"\n");
	}
	writer.flush();
	writer.close();
}

private static ConcurrentHashMap<Focus, Integer> calculateAllFocusClousure(
		Graph graph) {
	Collection<Focus> nodes = graph.getFocus().values();
	ConcurrentHashMap<Focus, Integer> result=new ConcurrentHashMap<Focus, Integer>();
	for (Focus aFocus : nodes) {
		double focusClousure = aFocus.getFocusClousure();
		result.put(aFocus, (int) (focusClousure*100));
	}
	return result;
}

private static void writeCsvFocusClousure(ConcurrentHashMap<Focus,Integer> toWrite,File file) throws IOException {
	BufferedWriter writer=new BufferedWriter(new FileWriter(file));
	KeySetView<Focus,Integer> keySet = toWrite.keySet();
	for (Focus aFocus : keySet) {
		writer.append(aFocus.getId()+DELIM+toWrite.get(aFocus)+"\n");
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
		System.out.println(">File Di Cahce Found");
		System.out.println(">Loading:");
		System.out.println("0%");
		graph = (Graph) loadCache(fileCache);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	}
		System.out.println("100%");

	return graph;
}

private static Graph graphCreation(File networkFile, File focusFile) throws IOException {
	System.out.println(">Parsing Node Network");
	Graph graph = parseNetwork(networkFile);;
	System.out.println(">Parsing Focus Network");
	return parseFocus(focusFile,graph);
}

private static Graph parseFocus(File focusFile, Graph graph) throws IOException {
	BufferedReader reader=new BufferedReader(new FileReader(focusFile));
	Stream<String> lines = reader.lines();
	long count=lines.count();
	long line = 0;
	int percent=0;
	long percentTmp;
	reader.close();

	reader=new BufferedReader(new FileReader(focusFile));
	lines = reader.lines();
	graph = GraphParser.parseFocus(lines, DELIM,graph);
	reader.close();
	
	
	line+=100;
	percentTmp = (line)/count;
	if (percentTmp!=percent) {
		percent=(int)percentTmp;
		System.out.println(percent+"%");
	}
	return graph;
}

private static Graph parseNetwork(File networkFile)
		throws IOException {
	Graph graph;
	BufferedReader reader=new BufferedReader(new FileReader(networkFile));
	Stream<String> lines = reader.lines();
	long count=lines.count();
	long line = 0;
	int percent=0;
	long percentTmp;
	reader.close();

	reader=new BufferedReader(new FileReader(networkFile));
	lines = reader.lines();
	graph = GraphParser.parse(lines, DELIM);
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


private static File writeFocus(File profileInfoFile) throws IOException {
//	BufferedReader reader=new BufferedReader(new FileReader(networkFile));
	RandomAccessFile reader=new RandomAccessFile(profileInfoFile,"r");
	
	File fileToWrite = new File("FocusNetworkOf"+profileInfoFile.getName());
	fileToWrite.createNewFile();
	BufferedWriter writer = new BufferedWriter(new FileWriter(fileToWrite));
	System.out.println("Inizio Analisi");
	
	byte[] b = new byte[(int) reader.length()];
	reader.readFully(b);
	String all =new String(b);
	b=null;
	StringTokenizer rows = new StringTokenizer(all,"\n");
	all=null;
	long percent=-1;
	int line = 0;
	int count = rows.countTokens();
	long percentTmp;
	ConcurrentHashMap<String, String> profiles=new ConcurrentHashMap<String, String>();
	String focusName;
	String content;
	int regionCounter=0;
	ProfileAttributesField[] attributes = User.ProfileAttributesField.values();
	int[] attributeCounter=new int[attributes.length];
	int number;
	while (rows.hasMoreTokens()) {
		StringTokenizer field=new StringTokenizer(rows.nextToken(), "\t\r\n");
		StringTokenizer minorField;
		String currentid = field.nextToken();
		content=PUBLIC+field.nextToken();
		if (!content.contains("null")) {
			focusName = profiles.get(content);//PUBLIC
			focusName =addToHash(profiles, focusName, content);
			append(writer,currentid,focusName,0, content);
		}
		
		field.nextToken();//skip percentual
		
		content=IS_MALE+field.nextToken();
		if (!content.contains("null")) {
			focusName = profiles.get(content);//ISMALE
			focusName =addToHash(profiles, focusName, content);
			append(writer,currentid,focusName, 0, content);
		}

		
		content=REGION+field.nextToken();
		if (!content.contains("null")) {
			focusName = profiles.get(content);//REGION
			if (focusName==null) {
				focusName =  REGION+regionCounter++;
				profiles.put(content, focusName);
			}
			append(writer,currentid,focusName, 0, content);
		}

		field.nextToken();//skip last_login=
		field.nextToken();//skip registration
		try{
			number = Integer.parseInt(field.nextToken());
			
			content=AGE+number/NUMBER_OF_NUMBER_CATEGORY;
			if (!content.contains("null")) {
				focusName = profiles.get(content);//AGE
				focusName = addToHash(profiles, focusName, content);
				append(writer,currentid,focusName, 0, content);
			}
		}catch(NumberFormatException e){				}

		content = field.nextToken();
		if (!content.contains("null")) {
			minorField=new StringTokenizer(content, DELIM);
			if (minorField.hasMoreTokens()) {
				try{
				number=removeUnit(minorField.nextToken());
				content = HEIGHT+number/NUMBER_OF_NUMBER_CATEGORY;//HEIGHT
				if (!content.contains("null")) {
					focusName =  profiles.get(content);
					focusName =addToHash(profiles, focusName, content);
					append(writer,currentid,focusName, 0, content);
				}
				}catch(NumberFormatException e){				}
			}
			if (minorField.hasMoreTokens()) {
				try{
				number=removeUnit(minorField.nextToken());
				content =WEIGHT+number/NUMBER_OF_NUMBER_CATEGORY;//WEIGHT 
				if (!content.contains("null")) {
					focusName =  profiles.get(content);
					focusName =addToHash(profiles, focusName, content);
					append(writer,currentid,focusName, 0, content);
				}
				}catch(NumberFormatException e){				}
			}
		}
		
		int attributeIndex = 0;
		while (field.hasMoreTokens()) {
			String attributiveField =  field.nextToken();
			if (attributiveField.length()>0&&!attributiveField.equals("null")) {
				minorField=new StringTokenizer(attributiveField, DELIM);
				while(minorField.hasMoreTokens()){
					String minor = minorField.nextToken();
					while (minor.startsWith(" ")&&minor.length()>1) {
						minor=minor.substring(1);
					}
					String minorAttribute =attributes[attributeIndex].toString();
					content =minorAttribute+minor;
					focusName = profiles.get(content);
					if (focusName==null) {
						focusName =minorAttribute+attributeCounter[attributeIndex]++;
						profiles.put(content,focusName );
					}
					append(writer,currentid,focusName, 0, content);


				}
			}
			attributeIndex++;
		}
		line+=100;
		percentTmp = (line)/count;
		if (percentTmp!=percent) {
			percent=(int)percentTmp;
			System.out.println(percent+"%");
		}
	}
	writer.flush();
	writer.close();
	return fileToWrite;
}
private static void append(BufferedWriter writer, String currentid, String focusName,double weight, String content) throws IOException {
	writer.append(currentid+DELIM+focusName+DELIM+weight+DELIM+content+"\n");
}


private static Integer removeUnit(String nextToken) {
		return Integer.parseInt(nextToken.replaceAll(" |kg|cm", ""));
		
}


private static void append(BufferedWriter writer, String currentid, String focusName) throws IOException {
	writer.append(currentid+DELIM+focusName+"\n");
}




private static String addToHash(ConcurrentHashMap<String, String> profiles,
		String focusName, String content) {
	if (focusName==null) {
		profiles.put(content, content);
		return content;
	}
	return focusName;
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
