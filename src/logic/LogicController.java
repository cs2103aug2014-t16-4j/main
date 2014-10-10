package logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import model.Task;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LogicController {
	public static ArrayList <JSONObject> tasksBuffer;
	String fileName;
	Add logicAdd;
	Clear logicClear;
	Delete logicDelete;
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public LogicController(String fileName) {
		this.fileName = fileName;
		tasksBuffer = new ArrayList<JSONObject>();
	}
	
	//Fetch all tasks from file in the beginning.	
	public void init() throws IOException
	{
		JSONParser jsonParser = new JSONParser();
		String line;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			while ((line = in.readLine()) != null) {
				JSONObject obj = (JSONObject) jsonParser.parse(line);
				tasksBuffer.add(obj);
			}
			in.close();
		} catch (FileNotFoundException | ParseException e) {
			e.printStackTrace();
		}
	}
		
	public boolean add(Task task){
		logicAdd = new Add();
		logicAdd.setFileName(fileName);
		logicAdd.setTask(task);
		return logicAdd.executeCommand();
	}
	
	public boolean clear(){
		logicClear = new Clear();
		logicClear.setFileName(fileName);
		return logicClear.executeCommand();
	}
	
	public String delete(JSONObject task){
		logicDelete = new Delete();
		logicDelete.setFileName(fileName);
		logicDelete.setTask(task);
		if(logicDelete.executeCommand()){
			return String.format(Consts.STRING_DELETE, fileName,task.get(Consts.NAME));
		}else{
			return Consts.USAGE_DELETE;
		}
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
		}
		return temp;	
	}

	
}
