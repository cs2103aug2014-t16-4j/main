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

public class Update extends Command{
	String fileName;
	JSONObject oldObj;
	Task newTask;

	public JSONObject getOldObj() {
		return oldObj;
	}

	public void setOldObj(JSONObject oldObj) {
		this.oldObj = oldObj;
	}

	public Task getNewTask() {
		return newTask;
	}

	public void setNewTask(Task newTask) {
		this.newTask = newTask;
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
			LogicController.tasksBuffer.remove(oldObj);
			System.out.println(newTask.getStatus());
			LogicController.tasksBuffer.add(Converter.taskToJSON(newTask));
			for (JSONObject jTask: LogicController.tasksBuffer) {
				bw.write(jTask.toString()+"\r\n");
			}

			bw.close();		
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public boolean undo() {
		LogicController.tasksBuffer.remove(Converter.taskToJSON(newTask));
		LogicController.tasksBuffer.add(oldObj);

		try {
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fstream);
			for (JSONObject jTask: LogicController.tasksBuffer) {
				bw.write(jTask.toString()+"\r\n");
			}
			bw.close();		
		} catch (IOException e) {
		}

		return true;
	}

}
