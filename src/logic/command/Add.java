//@author A0117993R

package logic.command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

import logic.Converter;
import logic.LogicController;
import model.Task;

public class Add extends Command {
	Task task;
	String fileName;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
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
			FileWriter fstream = new FileWriter(fileName, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			JSONObject jTask = Converter.taskToJSON(task);
			bufferedWriter.write(jTask.toString() + "\r\n");
			bufferedWriter.close();
			LogicController.tasksBuffer.add(jTask);
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public boolean undo() {
		LogicController.getInstance().delete(Converter.taskToJSON(task), false);
		return true;
	}
}
