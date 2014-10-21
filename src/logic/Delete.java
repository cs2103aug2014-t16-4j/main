package logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import model.Task;

import org.json.simple.JSONObject;

public class Delete extends Command{
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
		try{
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fstream);
			LogicController.tasksBuffer.remove(task);
			bw.write("");

			for (JSONObject jTask: LogicController.tasksBuffer) {
				bw.write(jTask.toString()+"\r\n");
			}

			bw.close();		
			return true;
		}catch(IOException e){
		}
		return false;
	}

	public boolean undo() {
		LogicController.getInstance().add(jsonToTask(task), false);
		return true;
	}

	private static Task jsonToTask(JSONObject obj){
		Task temp = null;
		try{
			temp = new Task(obj.get(Consts.NAME).toString());
			temp.setDescription(obj.get(Consts.DESCRIPTION).toString());
			temp.setStartDate(Consts.formatter.parse(obj.get(Consts.STARTDATE).toString()));
			temp.setEndDate(Consts.formatter.parse(obj.get(Consts.ENDDATE).toString()));
			temp.setFrequency((int) obj.get(Consts.FREQUENCY));
			temp.setPriority((int) obj.get(Consts.PRIORITY));
		}catch(Exception e){
			//e.printStackTrace();
		}
		return temp;	
	}
}
