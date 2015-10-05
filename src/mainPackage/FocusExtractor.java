package mainPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JFileChooser;
import dataAnalysis.User;
import dataAnalysis.User.ProfileAttributesField;


public class FocusExtractor {

	private static final String WEIGHT = "WEIGHT";
	private static final String HEIGHT = "HEIGHT";
	private static final String AGE = "AGE";
	private static final String REGION = "REGION";
	private static final String IS_MALE = "isMALE";
	private static final String PUBLIC = "PUBLIC";
	
private static final int NUMBER_OF_NUMBER_CATEGORY = 10;




public static void main(String[] args) {
	File profileInfoFile=null;
	try {
		profileInfoFile = getFile(args);
		writeFocus(profileInfoFile);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
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
			minorField=new StringTokenizer(content, ",");
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
				minorField=new StringTokenizer(attributiveField, ",");
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
	writer.append(currentid+","+focusName+","+weight+","+content+"\n");
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
