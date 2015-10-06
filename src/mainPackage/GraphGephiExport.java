package mainPackage;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Node;
import org.gephi.ranking.api.Ranking;

import dataAnalysis.User;
import dataAnalysis.User.ProfileAttributesField;
import socialNetwork.AbstractNode;
import socialNetwork.DirectionalLink;
import socialNetwork.Focus;
import socialNetwork.GephiExporter;
import socialNetwork.Graph;
import socialNetwork.GraphParser;


public class GraphGephiExport {
	private static final String DELIM = ",";
	private static final String WEIGHT = "WEIGHT";
	private static final String HEIGHT = "HEIGHT";
	private static final String AGE = "AGE";
	private static final String REGION = "REGION";
	private static final String IS_MALE = "isMALE";
	private static final String PUBLIC = "PUBLIC";
	private static final float MAX_NODE_SIZE = 80;
	private static final float MIN_NODE_SIZE = 30;
	private static final int NUMBER_OF_NUMBER_CATEGORY = 10;
	private static final float MAX_EDGE_SIZE = MAX_NODE_SIZE*0.3f;
	private static final float MIN_EDGE_SIZE =MIN_NODE_SIZE*0.8f;
	private static final String[] SELECTED_FOCUS = {AGE, User.ProfileAttributesField.I_LIKE_MOVIES.toString()};
	private static JScrollPane panelForFocusSelection;
	private static JFrame frame;
	private static JPanel panelComboBox;

public static void main(String[] args) {
	File networkFile = null;
	File focusFile=null;
	File profileInfoFile=null;
	try {
		JOptionPane.showMessageDialog(null, "Select Comma Separated Network File", "Network", JOptionPane.INFORMATION_MESSAGE);
		networkFile = getFile(args);
		JOptionPane.showMessageDialog(null, "Select Line Separated Proflie File", "Profile Info", JOptionPane.INFORMATION_MESSAGE);
			profileInfoFile = getFile(args);
		//focusFile=new File("FocusNetworkOf"+profileInfoFile.getName());
	//	if (!focusFile.exists()) {
			focusFile=writeFocus(profileInfoFile);
	//	}
		Graph graph=null;

		if (graph==null) {
			System.out.println(">Creation of graph");
			graph=graphCreation(networkFile,focusFile);
			
		}
		System.out.println(">Evaluation");
		ConcurrentHashMap<AbstractNode,Integer> triadricEvaluation=calculateAllTriadricClousure(graph);
		
		ConcurrentHashMap<Focus,Integer> focusEvaluation=calculateAllFocusClousure(graph);
		
		LinkedList<Object[]> membershipEvaluation=calculateAllMembershipClousure(graph);
		
		//showGUI();
		
			createGephiExporter(graph,triadricEvaluation,focusEvaluation,membershipEvaluation).export("GephiExportOf"+networkFile.getName());
		
		System.out.println("Fine");
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

private static void showGUI() {
	frame = new JFrame();
	Container container = frame.getContentPane();
	container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
	panelComboBox = new JPanel();
	panelForFocusSelection = new JScrollPane();
	panelForFocusSelection.getViewport().setLayout(new BoxLayout(panelForFocusSelection.getViewport(), BoxLayout.X_AXIS));

	panelComboBox.add(panelForFocusSelection);
	container.add(panelComboBox);
	JButton plus = new JButton("+");
	container.add(plus);
	JButton ok = new JButton("OK");
	container.add(ok);
	
	ok.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {frame.setVisible(false);}});
	plus.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {GraphGephiExport.addAComboBox();}});
	panelForFocusSelection.setPreferredSize(new Dimension(300, 200));
	frame.pack();
	frame.setMinimumSize(frame.getSize());
	frame.setVisible(true);
	}
private static void addAComboBox() {
	JComboBox<String> jComboBox = new JComboBox<String>(getFocusStartingNames());
	jComboBox.setMaximumSize(new Dimension(300,25));
	panelForFocusSelection.getViewport().add(jComboBox);
	panelForFocusSelection.repaint();
}
private static String[] getFocusStartingNames() {
	ProfileAttributesField[] values = User.ProfileAttributesField.values();
	String[] labels=new String[values.length+User.NUMBERO_OF_NON_FILLABLE_FIELD-1];
	labels[5]= WEIGHT ;
	labels[4]= HEIGHT ;
	labels[3]= AGE ;
	labels[2]= REGION ;
	labels[1]= IS_MALE ;
	labels[0]= PUBLIC ;
	for (int i = User.NUMBERO_OF_NON_FILLABLE_FIELD; i < labels.length; i++) {
		labels[i]=values[i-User.NUMBERO_OF_NON_FILLABLE_FIELD].toString();
	}
	return labels;
}

private static GephiExporter createGephiExporter(Graph graph,
		ConcurrentHashMap<AbstractNode, Integer> triadricEvaluation,
		ConcurrentHashMap<Focus, Integer> focusEvaluation,
		LinkedList<Object[]> membershipEvaluation) {
	GephiExporter gephi = new GephiExporter();
	AttributeColumn triadricColum = gephi.addAttributeColumn("TriadricClousure", AttributeType.INT);
	AttributeColumn homophiliaValueColumn = gephi.addAttributeColumn("HomophiliaValue", AttributeType.INT);

	AttributeColumn membershipColum = gephi.addAttributeColumn("MembershipClousure", AttributeType.INT);
	AttributeColumn focusColum = gephi.addAttributeColumn("FocusClousure", AttributeType.INT);
	
	Enumeration<AbstractNode> nodes = triadricEvaluation.keys();
	
	//NODE
	int nodesNumber;
	double homophiliaSum;
	double value;
	AbstractNode abstractNode;
	Node createdNode;
	Collection<DirectionalLink> adiacencyList;
	Node target;
	double weight;
	while (nodes.hasMoreElements()) {
		abstractNode = (AbstractNode) nodes.nextElement();
		createdNode = gephi.getNode(abstractNode.getId());
		gephi.addNode(createdNode);
		adiacencyList = abstractNode.getAdiacencyList();
		homophiliaSum=0;
		nodesNumber=0;
		for (DirectionalLink directionalLink : adiacencyList) {
			target = gephi.getNode(directionalLink.getTarget().getId());
			gephi.addNode(target);
			weight = (directionalLink.getWeight()/100)*(MAX_EDGE_SIZE-MIN_EDGE_SIZE)+MIN_EDGE_SIZE;
			homophiliaSum+=weight;
			gephi.addEdge(gephi.createEdge(createdNode, target, (float)weight, true)) ;
			nodesNumber++;
		}
		gephi.setAttributeValue(createdNode, triadricColum, triadricEvaluation.get(abstractNode));
		if (nodesNumber>0) {
			value = homophiliaSum/nodesNumber;
		}else {
			value=0;
		}
		gephi.setAttributeValue(createdNode, homophiliaValueColumn, value);
	}
	gephi.setSizeByColumn(triadricColum, MIN_NODE_SIZE, MAX_NODE_SIZE, Ranking.NODE_ELEMENT);
	gephi.setColorByColumn(homophiliaValueColumn, new Color[]{new Color(0xA3CCA3), new Color(0x009933)},Ranking.NODE_ELEMENT);
	//gephi.setColorByColumn(, new Color[]{new Color(0xCCFFCC), new Color(0x003300)},Ranking.EDGE_ELEMENT);


	
	//FOCUS
	Enumeration<Focus> allFocus = focusEvaluation.keys();
	while (allFocus.hasMoreElements()) {
		Focus aFocus = (Focus) allFocus.nextElement();
		for (int i = 0; i < SELECTED_FOCUS.length; i++) {
			if (aFocus.getId().startsWith(SELECTED_FOCUS[i].toString())) {
				Node focusNode = gephi.getNode(aFocus.getId(), (aFocus.getId().toLowerCase()));
				gephi.addNode(focusNode);
				gephi.setAttributeValue(focusNode, focusColum, focusEvaluation.get(aFocus));
				break;
			}
		}
	}
	gephi.setSizeByColumn(focusColum, MIN_NODE_SIZE, MAX_NODE_SIZE,Ranking.NODE_ELEMENT);
	gephi.setColorByColumn(focusColum, new Color[]{new Color(0x9999FF), new Color(0x6600CC)},Ranking.NODE_ELEMENT);
	
	//FOCUS' LINKS
	for (Object[] focusLink : membershipEvaluation) {
		AbstractNode aEnrolledNode=(AbstractNode) focusLink[0];
		Focus aFocus=(Focus) focusLink[1];
		for (int i = 0; i < SELECTED_FOCUS.length; i++) {
			if (aFocus.getId().startsWith(SELECTED_FOCUS[i].toString())) {
				int membershipClousure=(int) focusLink[2];
				Node node = gephi.getNode(aEnrolledNode.getId());
				Node focusNode = gephi.getNode(aFocus.getId());
				gephi.addEdge(gephi.createEdge(node, focusNode, membershipClousure, true)) ;
				break;
			}
		}
	}

	return gephi;
	
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


private static Graph graphCreation(File networkFile, File focusFile) throws IOException {
	System.out.println(">Parsing Node Network");
	Graph graph = parseNetwork(networkFile,DELIM);
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

private static Graph parseNetwork(File networkFile, String delim)
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
	graph = GraphParser.parse(lines, delim);
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

private static Integer removeUnit(String nextToken) {
		return Integer.parseInt(nextToken.replaceAll(" |kg|cm", ""));
		
}


private static void append(BufferedWriter writer, String currentid, String focusName,double weight, String content) throws IOException {
	writer.append(currentid+DELIM+focusName+DELIM+weight+DELIM+content+"\n");
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
