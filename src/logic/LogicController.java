package logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import model.Task;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

public class LogicController {
	Parser dateParser = new Parser();
	public static ArrayList <JSONObject> tasksBuffer;
	String fileName;
	Add logicAdd;
	Clear logicClear;
	Delete logicDelete;
	Update logicUpdate;
	
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
	
	public String update(JSONObject oldTask,Task newTask){
		logicUpdate = new Update();
		logicUpdate.setFileName(fileName);
		logicUpdate.setOldObj(oldTask);
		logicUpdate.setNewTask(newTask);
		if(logicUpdate.executeCommand()){
			return String.format(Consts.STRING_UPDATE,oldTask.get(Consts.NAME));
		}else{
			return Consts.STRING_NOT_UPDATE;
		}
	}
	
	public void sort(){
		Map<String, JSONObject> map = new TreeMap<String, JSONObject>();
		String sortKey = "";
		for(int i=0;i<tasksBuffer.size();i++){
			JSONObject obj = tasksBuffer.get(i);
			sortKey = obj.get(Consts.NAME).toString();
			map.put(sortKey, obj);
		}
		clear();
		Task tempTask;
		for(JSONObject taskObj : map.values()){
			tempTask = jsonToTask(taskObj);
			add(tempTask);
		}
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

	public ArrayList<JSONObject> search(String keyword) throws IOException {
		List<DateGroup> dateGrp = dateParser.parse((keyword));
		Date date = null;
		if(!dateGrp.isEmpty()){
			date = dateParser.parse(keyword).get(0).getDates().get(0);
		}
		ArrayList<JSONObject> foundLine = new ArrayList<JSONObject>();
		for (int i = 0; i < tasksBuffer.size(); i++) {
			Task task = jsonToTask(tasksBuffer.get(i));
			if (task.getName().contains(keyword)) {
				foundLine.add(tasksBuffer.get(i));
			} else if (task.getDescription().contains(keyword)) {
				foundLine.add(tasksBuffer.get(i));
			}
			if(date != null){
				if(dateBefore(task.getStartDate(),date) && dateBefore(date,task.getEndDate())){
					foundLine.add(tasksBuffer.get(i));
				}
			}
		}
		return foundLine;
	}
	
	public Boolean dateBefore(Date x, Date y){
		return Consts.cmpFormatter.format(x).compareTo(Consts.cmpFormatter.format(y)) <= 0;
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
