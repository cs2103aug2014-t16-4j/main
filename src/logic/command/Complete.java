package logic.command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import logic.Consts;
import logic.LogicController;

import org.json.simple.JSONObject;

public class Complete extends Command{
	String fileName;
	JSONObject task;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	int status;

	public JSONObject getTask() {
		return task;
	}

	public void setTask(JSONObject task) {
		this.task = task;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public boolean executeCommand() {
		try {
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fstream);
			task.put(Consts.STATUS, status);
			bw.write("");
			for (JSONObject jTask : LogicController.tasksBuffer) {
				bw.write(jTask.toString() + "\r\n");
			}
			bw.close();
			return true;
		} catch (IOException e) {
			System.err.println("Cannot complete task.");
			return false;
		}
	}
	
	@Override
	public boolean undo() {
		// TODO Auto-generated method stub
		return false;
	}

}
