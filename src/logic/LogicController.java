package logic;

import java.util.ArrayList;

import model.Task;

import org.json.simple.JSONObject;

public class LogicController {
	String fileName;
	static ArrayList <JSONObject> tasksBuffer;
	CommandEnum selectedCommand = CommandEnum.INVALID;
	Add add;

	public LogicController(String fileName) {
		this.fileName = fileName;
		tasksBuffer = new ArrayList<JSONObject>();
	}
	
	public boolean add(Task task){
		add = new Add();
		add.setFileName(fileName);
		return add.executeCommand();
	}
	
	public Task jsonToTask(JSONObject obj){
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
