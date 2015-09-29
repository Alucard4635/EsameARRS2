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

import dataAnalysis.UserMapException.DataExceptionType;
public class UserMap {


		private static final String CACHE_FILE_STARTING_NAME = "UserMapCacheOf";
		private final int DEFAULT_NUMBER_OF_LINE=1000;//1632803 users in pokec network
		public enum OrderType{NONE,BY_FIRST_WORD}
		private RandomAccessFile reader;
		private ConcurrentHashMap<Long, User> lineStartingByte;//ArrayList<Long>
		private int readingPercentageCompletion=-1;
		private Pattern pattern = Pattern.compile("(\\d*)[.[^.]]*");
		
		public UserMap(File input, int numerOfLine,OrderType type) throws IOException, UserMapException {
			reader=new RandomAccessFile(input, "r");
			File fileCache = new File(CACHE_FILE_STARTING_NAME+input.getName());
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
					inizializeHashLineStartingByte(numerOfLine);
					break;
				}
				writeCache(input);
			}
			setReadingPercentageCompletion(100);
		}
		
		private void writeCache(File input) throws IOException {
			FileOutputStream fos = new FileOutputStream(CACHE_FILE_STARTING_NAME+input.getName());
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(lineStartingByte);
			oos.close();
		}

		private void loadCache(File fileCache) throws IOException, ClassNotFoundException {
			FileInputStream fis = new FileInputStream(fileCache);
			ObjectInputStream ois = new ObjectInputStream(fis);
			lineStartingByte = (ConcurrentHashMap<Long, User>)  ois.readObject();
			ois.close();
		}

		private void inizializeHashLineStartingByte(int numerOfLine) throws IOException, UserMapException {
			long length = reader.length();
			lineStartingByte=new ConcurrentHashMap<Long, User>(numerOfLine);
			long filePointer = reader.getFilePointer();
			
			Matcher matcher;
			String lineID;
			while (filePointer<length) {
				String readLine = reader.readLine();
				matcher = pattern.matcher(readLine);
				if ( matcher.matches() ) {
					lineID = matcher.group(1);
					User user = new User();
					user.parseUser(readLine);
					lineStartingByte.put(Long.parseLong(lineID), user);
					setReadingPercentageCompletion((int)(filePointer/(length/100.0)));
					filePointer = reader.getFilePointer();
				}else {
					throw new UserMapException(DataExceptionType.INCORRECT_FORMAT);
					
					
				}
			}
			
		}


		public User get(long index) throws IOException, UserMapException {
			return lineStartingByte.get(index);
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
