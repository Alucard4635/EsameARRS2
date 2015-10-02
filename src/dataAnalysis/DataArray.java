package dataAnalysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dataAnalysis.DataArrayException.DataExceptionType;


public class DataArray {
	private final static int DEFAULT_NUMBER_OF_LINE=1000;//1632803 users in pokec network
	public enum OrderType{NONE,BY_FIRST_WORD}
	private RandomAccessFile reader;
	private ConcurrentHashMap<Long, Long> lineStartingByte;//ArrayList<Long>
	private int readingPercentageCompletion=-1;
	private Pattern pattern = Pattern.compile("(\\d*)[.[^.]]*");
	
	public DataArray(File input) throws IOException, DataArrayException {
		this(input, DEFAULT_NUMBER_OF_LINE);
	}
	
	public DataArray(File input, int numerOfLine) throws IOException, DataArrayException {
		this(input, numerOfLine, OrderType.NONE, null);
	}
	public DataArray(File input, int numerOfLine,OrderType type, String separatingChars) throws IOException, DataArrayException {
		reader=new RandomAccessFile(input, "r");
		File fileCache = new File("DataArrayCacheOf"+input.getName());
		if (fileCache.exists()) {
			try {
				System.out.println("trovatoFileDiCahce");
				System.out.println("Loading:");
				System.out.println("0%");
				loadCache(fileCache);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}else{
			switch (type) {
			case BY_FIRST_WORD:
				inizializeHashLineStartingByte(numerOfLine, separatingChars);
				break;
	
			default:
				inizializeLineStartingByte(numerOfLine);
				break;
			}
			writeCache(input);
		}
		setReadingPercentageCompletion(100);
	}
	
	private void writeCache(File input) throws IOException {
		FileOutputStream fos = new FileOutputStream("DataArrayCacheOf"+input.getName());
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(lineStartingByte);
		oos.close();
	}

	private void loadCache(File fileCache) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(fileCache);
		ObjectInputStream ois = new ObjectInputStream(fis);
		lineStartingByte = (ConcurrentHashMap<Long, Long>)  ois.readObject();
		ois.close();
	}

	private void inizializeHashLineStartingByte(int numerOfLine, String separatingChars) throws IOException, DataArrayException {
		long length = reader.length();
		lineStartingByte=new ConcurrentHashMap<Long, Long>(numerOfLine);
		long filePointer = reader.getFilePointer();
		
		Matcher matcher;
		String lineID;
		while (filePointer<length) {
			String readLine = reader.readLine();
			matcher = pattern.matcher(readLine);
			if ( matcher.matches() ) {
				lineID = matcher.group(1);
				lineStartingByte.put(Long.parseLong(lineID), filePointer);
				setReadingPercentageCompletion((int)(filePointer/(length/100.0)));
				filePointer = reader.getFilePointer();
			}else {
				throw new DataArrayException(DataExceptionType.INCORRECT_FORMAT);
			}
		}
		
	}

	private void inizializeLineStartingByte(int numerOfLine) throws IOException {
		long length = reader.length();
		lineStartingByte=new ConcurrentHashMap<Long, Long>(numerOfLine);
		long filePointer = reader.getFilePointer();
		long index = 0;
		while (filePointer<length) {
			lineStartingByte.put(index++,filePointer);
			reader.readLine();
			setReadingPercentageCompletion((int)(filePointer/(length/100.0)));
			filePointer = reader.getFilePointer();
		}
	}

	public String get(long index) throws IOException, DataArrayException {
		Long position = lineStartingByte.get(index);
		if (position==null) {
			throw new DataArrayException(DataExceptionType.INDEX_NOT_FOUND);
		}
		synchronized (reader) {
			reader.seek(position);
			return reader.readLine();
		}
	}
	public void close() throws IOException {
		reader.close();
	}

	public double getReadingPercentageCompletion() {
		return readingPercentageCompletion;
	}

	public void setReadingPercentageCompletion(int readingPercentageCompletion) {
		if (this.readingPercentageCompletion!=readingPercentageCompletion) {
			System.out.println(readingPercentageCompletion+"%");
			this.readingPercentageCompletion = readingPercentageCompletion;
		} 
	}

}
