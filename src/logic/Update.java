package logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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

	public boolean executeCommand() {
		try {
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fstream);
			LogicController.tasksBuffer.remove(oldObj);
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
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject taskToJSON(Task task)
	{
		JSONObject jTask=new JSONObject();
		jTask.put(Consts.NAME, task.getName());
		jTask.put(Consts.DESCRIPTION, task.getDescription());
		jTask.put(Consts.STARTDATE, Consts.formatter.format(task.getStartDate()));
		jTask.put(Consts.ENDDATE, Consts.formatter.format(task.getEndDate()));
		jTask.put(Consts.PRIORITY, task.getPriority());
		jTask.put(Consts.FREQUENCY, task.getFrequency());
		return jTask;
	}

}
