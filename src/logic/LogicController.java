package logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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

import com.google.gson.JsonObject;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import edu.emory.mathcs.backport.java.util.Collections;

public class LogicController {
	private static final int TIME_EPS = 2;

	Parser dateParser = new Parser();
	public static ArrayList <JSONObject> tasksBuffer;
	public static String fileName;
	ArrayList <Task> blockBuffer;
	Add logicAdd;
	Clear logicClear;
	Delete logicDelete;
	Update logicUpdate;
	GoogleCal gCal;
	Stack<Command> opStack = new Stack<Command>();
	String authToken = "";
   
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
		gCal = new GoogleCal();
	}
	
	public void setFileName(String fileName) {
		LogicController.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	//Fetch all tasks from file in the beginning.	
	public void init(String fileName) throws IOException
	{
		LogicController.fileName = fileName;
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
	
	//TasksBuffer without Block task (Timed)
	public ArrayList<JSONObject> getTimedTasksBuffer() {
		ArrayList<JSONObject> displayTasksBuffer = new ArrayList<JSONObject>();
		for (JSONObject jTask: tasksBuffer) {
			if (Converter.jsonToTask(jTask).getStatus() == Consts.STATUS_TIMED_TASK) {
				//System.out.println(jTask);// For Debuging
				displayTasksBuffer.add(jTask);
			}
		}
		//System.out.println(); For Debuging
		return displayTasksBuffer;
	}
	
	//TasksBuffer without Block task (Floating)
	public ArrayList<JSONObject> getFloatingTasksBuffer() {
		ArrayList<JSONObject> displayTasksBuffer = new ArrayList<JSONObject>();
		for (JSONObject jTask: tasksBuffer) {
			if (Converter.jsonToTask(jTask).getStatus() == Consts.STATUS_FLOATING_TASK) {
				//System.out.println(jTask);// For Debuging
				displayTasksBuffer.add(jTask);
			}
		}
		//System.out.println(); For Debuging
		return displayTasksBuffer;
	}
	
	//TasksBuffer with Block task (Timed)
	public ArrayList<JSONObject> getBlockTasksBuffer() {
		ArrayList<JSONObject> displayTasksBuffer = new ArrayList<JSONObject>();
		for (JSONObject jTask: tasksBuffer) {
			if (Converter.jsonToTask(jTask).getStatus() == Consts.STATUS_BLOCK_TASK) {
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
		
	private boolean intersectTime(Date startDate1, Date endDate1, Date startDate2, Date endDate2) {
		return Math.max(startDate1.getTime(), startDate2.getTime()) <= Math.min(endDate1.getTime(), endDate2.getTime());
	}
		
	public String add(Task task, boolean...addToStack){
		
		if (task.getStatus() == Consts.STATUS_TIMED_TASK) { 
			if (task.getName().compareTo("") == 0) {
				return Consts.ERROR_ADD;
			}
			
			for (JSONObject jTask: tasksBuffer) {
				if (Converter.jsonToTask(jTask).getStatus() == Consts.STATUS_BLOCK_TASK) {
					Task temp = Converter.jsonToTask(jTask);
					//intersect
					if (intersectTime(temp, task)) {
						return Consts.ERROR_ADD_BLOCK;
					}
				}
			}
		}
		logicAdd = new Add();
		logicAdd.setFileName(fileName);
		logicAdd.setTask(task);
		if (addToStack.length == 0 || (addToStack.length > 0 && addToStack[0] == true)){
			opStack.add(logicAdd);
		}
		if (logicAdd.executeCommand()) {
			return String.format(Consts.STRING_ADD, Consts.FORMAT_PRINT_DATE.format(task.getStartDate()), Consts.FORMAT_PRINT_DATE.format(task.getEndDate()));
		} else {
			return Consts.ERROR_ADD;
		}
			
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
		if (addToStack.length == 0 || (addToStack.length > 0 && addToStack[0] == true)){
			opStack.add(logicDelete);
		}
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
			Task task = new Task("Blocked " + Consts.FORMAT_PRINT_TIME.format(startDate) + " > " +  Consts.FORMAT_PRINT_TIME.format(endDate), "", startDate, endDate, 0, 0, Consts.STATUS_BLOCK_TASK);
			add(task);
			return "BLOCK " + startDate.toString() + " -> " + endDate.toString();
		} else {
			return "BLOCK FAIL";
		}
	}
	
	public String getUrl(){
		return gCal.getURL();
	}
	
	public boolean sycnWithGoogleExistingToken(){
		return gCal.withExistingToken();
	}

	public boolean generateNewToken(String code) throws IOException{
		return gCal.generateNewToken(code);
	}
	
	public String syncWithGoogle() throws IOException{
		return gCal.syncGCal(getTimedTasksBuffer());
	}

	public boolean undo(){
		if (!opStack.isEmpty()) {
			opStack.pop().undo();
			return true;
		}else{
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	public int getTimeFromDate(Date date) {
		return date.getHours() *  60 * 60 + date.getMinutes() * 60 + date.getSeconds();
	}
	
	public boolean isDefaultTime(Date date) {
		Date dateNow = new Date();
		return (getTimeFromDate(dateNow) - getTimeFromDate(date) <= TIME_EPS);
	}

	public Date getEndOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);
	    return calendar.getTime();
	}

	public Date getStartOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	    return calendar.getTime();
	}
	
	private boolean checkWeekly(Date dateStart1, Date dateEnd1, Date dateStart2, Date dateEnd2) {
		return true;
	}
	
	private boolean checkMonthly(Date dateStart1, Date dateEnd1, Date dateStart2, Date dateEnd2) {
		return true;
	}
	
	public ArrayList<JSONObject> search(String keyword, int statusType) throws IOException {
		//tasksBuffer inside is different from outside
		ArrayList <JSONObject> tasksBuffer;
		
		if (statusType == Consts.STATUS_TIMED_TASK) {
			tasksBuffer = getTimedTasksBuffer();
			if (keyword.trim().toLowerCase().equals("block")) {
				return getBlockTasksBuffer();
			}
		} else {
			tasksBuffer = getFloatingTasksBuffer();
		}
		List<DateGroup> dateGrp = dateParser.parse(keyword);
		Date date1 = null;
		Date date2 = null;
		if(!dateGrp.isEmpty()){
			date1 = dateGrp.get(0).getDates().get(0);
			if (dateGrp.get(0).getDates().size() == 1)
			{
				date2 = date1;
			} else {
				date2 = dateGrp.get(0).getDates().get(1);
			}
			if (isDefaultTime(date1)) {
				date1 = getStartOfDay(date1);
			}
			if (isDefaultTime(date2)) {
				date2 = getEndOfDay(date2);
			}
		}
		ArrayList<JSONObject> foundLine = new ArrayList<JSONObject>();
		for (int i = 0; i < tasksBuffer.size(); i++) {
			Task task = Converter.jsonToTask(tasksBuffer.get(i));
			if (task.getName().contains(keyword)) {
				foundLine.add(tasksBuffer.get(i));
			} else if (task.getDescription().contains(keyword)) {
				foundLine.add(tasksBuffer.get(i));
			} else if(date1 != null){
				//if(dateBefore(task.getStartDate(),date) && dateBefore(date,task.getEndDate())){
				//if (task.getStartDate().getTime() <= date.getTime() && date.getTime() <= task.getEndDate().getTime()) {
				if (intersectTime(task.getStartDate(), task.getEndDate(), date1, date2)) {
					foundLine.add(tasksBuffer.get(i));
				} else if (task.getFrequency() == Consts.FREQUENCY_DAILY_VALUE) {
					foundLine.add(tasksBuffer.get(i));
				} else if (task.getFrequency() == Consts.FREQUENCY_WEEKLY_VALUE && 
						checkWeekly(task.getStartDate(), task.getEndDate(), date1, date2)) {
					foundLine.add(tasksBuffer.get(i));
				} else if (task.getFrequency() == Consts.FREQUENCY_MONTHLY_VALUE && 
						checkMonthly(task.getStartDate(), task.getEndDate(), date1, date2)) {
					foundLine.add(tasksBuffer.get(i));
				} 
			}
		}
		System.out.println(foundLine.toString());
		return foundLine;
	}
	
	public Boolean dateBefore(Date x, Date y){
		return Consts.FORMAT_COMPARE_DATE.format(x).compareTo(Consts.FORMAT_COMPARE_DATE.format(y)) <= 0;
	}
}
