package logic.command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import logic.LogicController;

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

	//@author A0112069M
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
