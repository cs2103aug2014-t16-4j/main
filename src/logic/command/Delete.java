//@author A0117993R
package logic.command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import logic.Consts;
import logic.Converter;
import logic.LogicController;
import model.Task;

import org.json.simple.JSONObject;

public class Delete extends Command {
	JSONObject task;
	String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public JSONObject getTask() {
		return task;
	}

	public void setTask(JSONObject task) {
		this.task = task;
	}

	public boolean executeCommand() {
		try {
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fstream);
			if (LogicController.tasksBuffer.contains(task)) {
				LogicController.tasksBuffer.remove(task);
				bw.write("");
				for (JSONObject jTask : LogicController.tasksBuffer) {
					bw.write(jTask.toString() + "\r\n");
				}

				bw.close();
				return true;
			}else{
				bw.close();
				return false;
			}
		} catch (IOException e) {
		}
		return false;
	}

	//@author A0112069M
	public boolean undo() {
		LogicController.getInstance().add(Converter.jsonToTask(task), false);
		return true;
	}

}
