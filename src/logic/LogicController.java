package logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import model.Task;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import edu.emory.mathcs.backport.java.util.Collections;

public class LogicController {
	Parser dateParser = new Parser();
	public static ArrayList <JSONObject> tasksBuffer;
	ArrayList <Task> blockBuffer;
	String fileName;
	Add logicAdd;
	Clear logicClear;
	Delete logicDelete;
	Update logicUpdate;
	Stack<Command> opStack = new Stack<Command>();
   
	private static LogicController singleton = null;
   /* Static 'instance' method */
	
	public static LogicController getInstance( ) {
		if (singleton == null)
		{
			singleton = new LogicController();
		}
		return singleton;
	}
	
	public LogicController() {
		tasksBuffer = new ArrayList<JSONObject>();
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	//Fetch all tasks from file in the beginning.	
	public void init(String fileName) throws IOException
	{
		this.fileName = fileName;
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
	
	//TasksBuffer without Block task
	public ArrayList<JSONObject> getDisplayTasksBuffer() {
		ArrayList<JSONObject> displayTasksBuffer = new ArrayList<JSONObject>();
		for (JSONObject jTask: tasksBuffer) {
			if (!jTask.containsValue("BLOCK")) {
				//System.out.println(jTask);// For Debuging
				displayTasksBuffer.add(jTask);
			}
		}
		//System.out.println(); For Debuging
		return displayTasksBuffer;
	}
	
	private boolean intersectTime(Task taskA, Task taskB) {
		return Math.max(taskA.getStartDate().getTime(), taskB.getStartDate().getTime()) <= Math.min(taskA.getEndDate().getTime(), taskB.getEndDate().getTime());
	}
		
	public boolean add(Task task, boolean...addToStack){
		for (JSONObject jtask: tasksBuffer) {
			if (jtask.containsValue("BLOCK")) {
				Task temp = Converter.jsonToTask(jtask);
				//intersect
				if (intersectTime(temp, task)) {
					System.out.println("Added to a block");
					return false;
				}
			}
		}
		
		logicAdd = new Add();
		logicAdd.setFileName(fileName);
		logicAdd.setTask(task);
		if (addToStack.length == 0 || (addToStack.length > 0 && addToStack[0] == true))
		opStack.add(logicAdd);
		return logicAdd.executeCommand();
	}
	
	public boolean clear(boolean...addToStack){
		logicClear = new Clear();
		logicClear.setFileName(fileName);
		logicClear.setOldTaskBuffer(tasksBuffer);
		opStack.add(logicClear);
		return logicClear.executeCommand();
	}
	
	public String update(JSONObject oldTask, Task newTask, boolean...addToStack){
		logicUpdate = new Update();
		logicUpdate.setFileName(fileName);
		logicUpdate.setOldObj(oldTask);
		logicUpdate.setNewTask(newTask);
		opStack.add(logicUpdate);
		if(logicUpdate.executeCommand()){
			return String.format(Consts.STRING_UPDATE,oldTask.get(Consts.NAME));
		}else{
			return Consts.STRING_NOT_UPDATE;
		}
	}

	class NameComparator implements Comparator<JSONObject>
	{
		public int compare(JSONObject u, JSONObject v)
		{
			return u.get(Consts.NAME).toString().compareTo(v.get(Consts.NAME).toString());
		}
	}
	
	public void sort(){
		/*Map<String, JSONObject> map = new TreeMap<String, JSONObject>();
		String sortKey = "";
		for(int i=0;i<tasksBuffer.size();i++){
			JSONObject obj = tasksBuffer.get(i);
			sortKey = obj.get(Consts.NAME).toString();
			map.put(sortKey, obj);
		}
		clear();
		Task tempTask;
		for(JSONObject taskObj : map.values()){
			tempTask = Converter.jsonToTask(taskObj);
			add(tempTask);
		}*/
		try {
			Collections.sort(tasksBuffer, new NameComparator());
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public String delete(JSONObject task, boolean... addToStack){
		logicDelete = new Delete();
		logicDelete.setFileName(fileName);
		logicDelete.setTask(task);
		if (addToStack.length == 0 || (addToStack.length > 0 && addToStack[0] == true))
		opStack.add(logicDelete);
		if(logicDelete.executeCommand()){
			return String.format(Consts.STRING_DELETE, fileName,task.get(Consts.NAME));
		}else{
			return Consts.USAGE_DELETE;
		}
	}
	
	public String block(String dateString) {
		List<DateGroup> dateGrp = dateParser.parse(dateString);
		if(!dateGrp.isEmpty() && dateGrp.get(0).getDates().size() == 2) {
			Date startDate = dateGrp.get(0).getDates().get(0);
			Date endDate = dateGrp.get(0).getDates().get(1);			
			Task task = new Task("", "BLOCK", startDate, endDate, 0, 0, 0);
			add(task);
			return "BLOCK " + startDate.toString() + " -> " + endDate.toString();
		} else {
			return "BLOCK FAIL";
		}
	}
	
	public String syncWithGoogle(){
		GoogleCal gCal = new GoogleCal();
		if(gCal.isOnline()){
			return Consts.STRING_SYNC_COMPLETE;
		}else{
			return Consts.STRING_USER_NOT_ONLINE;
		}
	}

	public void undo(){
		if (!opStack.isEmpty()) {
			opStack.pop().undo();
		}
	}
	
	public ArrayList<JSONObject> search(String keyword) throws IOException {
		List<DateGroup> dateGrp = dateParser.parse(keyword);
		Date date = null;
		if(!dateGrp.isEmpty()){
			date = dateGrp.get(0).getDates().get(0);
		}
		ArrayList<JSONObject> foundLine = new ArrayList<JSONObject>();
		for (int i = 0; i < tasksBuffer.size(); i++) {
			Task task = Converter.jsonToTask(tasksBuffer.get(i));
			if (task.getName().contains(keyword)) {
				foundLine.add(tasksBuffer.get(i));
			} else if (task.getDescription().contains(keyword)) {
				foundLine.add(tasksBuffer.get(i));
			} else if(date != null){
				//if(dateBefore(task.getStartDate(),date) && dateBefore(date,task.getEndDate())){
				if (task.getStartDate().getTime() <= date.getTime() && date.getTime() <= task.getEndDate().getTime()) {
					foundLine.add(tasksBuffer.get(i));
				}
			}
		}
		System.out.println(foundLine.toString());
		return foundLine;
	}
	
	public Boolean dateBefore(Date x, Date y){
		return Consts.cmpFormatter.format(x).compareTo(Consts.cmpFormatter.format(y)) <= 0;
	}
}
