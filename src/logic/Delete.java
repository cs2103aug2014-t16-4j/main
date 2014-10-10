package logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
		return false;
	}

}
