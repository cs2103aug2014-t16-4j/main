package logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONObject;

public class Clear extends Command{
	String fileName;
	public static ArrayList <JSONObject> oldTaskBuffer;	

	public static void setOldTaskBuffer(ArrayList<JSONObject> oldTaskBuffer) {
		Clear.oldTaskBuffer = new ArrayList<JSONObject>(LogicController.tasksBuffer);
	}

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
		LogicController.tasksBuffer = Clear.oldTaskBuffer;
		return true;
	}

}
