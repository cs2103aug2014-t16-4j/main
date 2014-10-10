package logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Clear extends Command{
	String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean executeCommand() {
		try {
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fstream);
			bw.write("");
			bw.close();
			LogicController.tasksBuffer.clear();
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public boolean undo() {
		return false;
	}

}
