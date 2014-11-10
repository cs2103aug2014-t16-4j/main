package logic.command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import logic.Consts;
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
			LogicController.tasksBuffer.add(taskToJSON(newTask));
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
		LogicController.tasksBuffer.remove(taskToJSON(newTask));
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
	
	@SuppressWarnings("unchecked")
	public JSONObject taskToJSON(Task task)
	{
		JSONObject jTask=new JSONObject();
		jTask.put(Consts.NAME, task.getName());
		jTask.put(Consts.DESCRIPTION, task.getDescription());
		jTask.put(Consts.STARTDATE, Consts.FORMAT_DATE.format(task.getStartDate()));
		jTask.put(Consts.ENDDATE, Consts.FORMAT_DATE.format(task.getEndDate()));
		jTask.put(Consts.PRIORITY, task.getPriority());
		jTask.put(Consts.FREQUENCY, task.getFrequency());
		jTask.put(Consts.STATUS,task.getStatus());
		return jTask;
	}

}
