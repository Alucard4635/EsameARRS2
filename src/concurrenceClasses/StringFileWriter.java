package concurrenceClasses;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class StringFileWriter extends Thread {
	private String toWrite;
	private BufferedWriter targetFile;
	public StringFileWriter(String toWrite,BufferedWriter target) {
		this.toWrite = toWrite;
		this.targetFile = target;
	}
	@Override
	public void run() {
		try {
			targetFile.write(toWrite);
			targetFile.flush();
			targetFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
