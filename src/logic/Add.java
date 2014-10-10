package logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

import model.Task;

public class Add extends Command{
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

	public boolean executeCommand() {
		try {
			FileWriter fstream = new FileWriter(fileName, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			JSONObject jTask = taskToJSON(task);
			bufferedWriter.write(jTask.toString()+"\r\n");
			bufferedWriter.close();
			LogicController.tasksBuffer.add(jTask);
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
