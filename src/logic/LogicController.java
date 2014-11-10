//@author A0117993R
package logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import logic.command.Add;
import logic.command.Clear;
import logic.command.Command;
import logic.command.Complete;
import logic.command.Delete;
import logic.command.Update;
import logic.google.CacheMap;
import logic.google.GoogleCal;
import logic.google.GoogleCalService;
import logic.google.LoadCache;
import logic.google.SaveCache;
import model.SearchResult;
import model.Task;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import edu.emory.mathcs.backport.java.util.Collections;

public class LogicController {
	private static final int TIME_EPS = 2;

	public static ArrayList <JSONObject> tasksBuffer; // main task buffer
	public static String fileName;
	public static CacheMap cacheMap; // for saving cache purpose
	private static Logger logger = Logger.getLogger("Logic");
	Parser dateParser = new Parser();
	ArrayList <Task> blockBuffer;
	Add logicAdd;
	Clear logicClear;
	Delete logicDelete;
	Update logicUpdate;
	Complete logicComplete;
	SaveCache saveCache;
	LoadCache loadCache;
	GoogleCal gCal;
	GoogleCalService gCalServ;
	//@author A0112069M
	Stack<Command> opStack = new Stack<Command>(); // for undo stuff 
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
		cacheMap = new CacheMap();
		gCal = new GoogleCal();
		gCalServ = new GoogleCalService();
	}
	
	public void setFileName(String fileName) {
		LogicController.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void init(String fileName) throws IOException
	{
		LogicController.fileName = fileName;
		loadBuffer(fileName);
		loadCacheBuffer();	
		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){
			System.err.println(e.getMessage());
		}
		new Thread(gCalServ).start(); // Thread for google sync 
	}
	
	//@author A0117993R
	// Loading the entries from cache file 
	public void loadCacheBuffer(){
		File f = new File(Consts.CACHE);
		if (f.exists()) {
			loadCache = new LoadCache();
			if(loadCache.executeCommand()){
				assert(!cacheMap.isEmpty());
				if(GoogleCal.isOnline() && gCal.withExistingToken()){
					try {
						if(initSync()){
							f.delete();
						}
					} catch (IOException e) {
						System.err.println(e.getMessage());
					}
				}
				logger.log(Level.INFO,"Cache entries are loaded.");
			}else{ 
				logger.log(Level.INFO,"Cache entries are not loaded.");
			}
		}else{
			logger.log(Level.INFO,"File doesnt exist.");
		}
	}
	
	//@author A0117993R
	// Trying to sync with google server when the app is open
	@SuppressWarnings("unchecked")
	public boolean initSync() throws IOException{
		Iterator<Entry<String, List<JSONObject>>> it = cacheMap.entrySet().iterator();
		ArrayList<JSONObject> temp = new ArrayList<JSONObject>(); // To store the values from cacheMap
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			logger.log(Level.INFO,pairs.getKey() + " = " + pairs.getValue());
			String key = (String) pairs.getKey();
			for (JSONObject obj : (List<JSONObject>) pairs.getValue()) {
				temp.add(obj);
			}
			if (key.equals(Consts.ADD)) {
				logger.log(Level.INFO,"Adding to google cal - cache file.");
				gCal.syncGCal(temp);
			} else if (key.equals(Consts.DELETE)) {
				logger.log(Level.INFO,"Deleting from google cal - cache file");
				for (JSONObject obj : temp) {
					gCal.deleteEvent((String)obj.get(Consts.NAME));
				}
			}
			it.remove();
		}
		return temp.isEmpty() ? false : true;
	}

	//@author A0117993R
	// Fetching all tasks from file in the beginning.	
	public void loadBuffer(String fileName) throws IOException {
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
			System.err.println(e.getMessage());
		}
	}
	
	//@author A0112069M
	//TasksBuffer without Block task (Timed)
	public ArrayList<JSONObject> getTimedTasksBuffer() {
		ArrayList<JSONObject> displayTasksBuffer = new ArrayList<JSONObject>();
		Date curDate = new Date();
		for (JSONObject jTask: tasksBuffer) {
			
			if ((Converter.jsonToTask(jTask).getStatus() % 10 == Consts.STATUS_TIMED_TASK || Converter
					.jsonToTask(jTask).getStatus() == Consts.STATUS_COMPLETED_TIMED_TASK)
					&& Consts.FORMAT_COMPARE_DATE.format(
							Converter.jsonToTask(jTask).getStartDate())
							.compareTo(
									Consts.FORMAT_COMPARE_DATE.format(curDate)) >= 0) {
				//System.out.println(jTask);// For Debuging
				displayTasksBuffer.add(jTask);
			}
		}
		//System.out.println(); // For Debuging
		return displayTasksBuffer;
	}
	
	//TasksBuffer without Block task (Floating)
	public ArrayList<JSONObject> getFloatingTasksBuffer() {
		ArrayList<JSONObject> displayTasksBuffer = new ArrayList<JSONObject>();
		for (JSONObject jTask: tasksBuffer) {
			if (Converter.jsonToTask(jTask).getStatus() % 10 == Consts.STATUS_FLOATING_TASK || Converter.jsonToTask(jTask).getStatus() == Consts.STATUS_COMPLETED_FLOATING_TASK) {
				//System.out.println(jTask);// For Debuging
				displayTasksBuffer.add(jTask);
			}
		} 
		
		//System.out.println(); // For Debuging
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
		//System.out.println(); // For Debuging
		return displayTasksBuffer;
	}
	
	private boolean intersectTime(Task taskA, Task taskB) {
		return Math.max(taskA.getStartDate().getTime(), taskB.getStartDate().getTime()) <= Math.min(taskA.getEndDate().getTime(), taskB.getEndDate().getTime());
	}
		
	private boolean intersectTime(Date startDate1, Date endDate1, Date startDate2, Date endDate2) {
		return Math.max(startDate1.getTime(), startDate2.getTime()) <= Math.min(endDate1.getTime(), endDate2.getTime());
	}
		
	// Adding task
	public String add(Task task, boolean...addToStack){
		if (task.getStatus() == Consts.STATUS_TIMED_TASK) { 
			if (task.getName().compareTo("") == 0) {
				return Consts.ERROR_ADD;
			}
			assert(!task.getName().isEmpty());
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
			//@author A0117993R
			if(GoogleCal.isOnline()){
				try {
					if(gCal.withExistingToken()){
						gCal.createEvent(task, "primary");
						logger.log(Level.INFO,"Adding - sync with google.");
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}else{
				logger.log(Level.INFO,"Adding - offline [saving to file].");
				cacheMap.put(Consts.ADD,Converter.taskToJSON(task));
			}
			return String.format(Consts.STRING_ADD, task.getName());
		} else {
			return Consts.ERROR_ADD;
		}
			
	}
	
	//@author A0112069M
	public boolean clear(boolean...addToStack){
		logicClear = new Clear();
		logicClear.setFileName(fileName);
		logicClear.setOldTaskBuffer(tasksBuffer);
		opStack.add(logicClear);
		//@author A0117993R
		if(GoogleCal.isOnline()){
			try {
				if(gCal.withExistingToken()){
					logger.log(Level.INFO,"Deleting all events - sync with google.");
					gCal.deleteAllEntries(); // Clearing all events from google calendar
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		return logicClear.executeCommand();
	}
	
	//@author A0112069M
	public String update(JSONObject oldTask, String commandString, String restString, boolean...addToStack){
	//public String update(JSONObject oldTask, Task newTask, boolean...addToStack){
		
		Task newTask = new Task();
		newTask = Converter.jsonToTask(oldTask);
		
		switch (commandString.toLowerCase()) {
			case Consts.UPDATE_NAME:
				newTask.setName(restString);
				break;
			case Consts.UPDATE_DESC:
				newTask.setDescription(restString);
				break;
			case Consts.UPDATE_DATE:
				List<DateGroup> dateGrp = dateParser.parse(restString);
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
					newTask.setStartDate(date1);
					newTask.setEndDate(date2);
				} else {
					return Consts.UPDATE_ERROR_DATE;
				}
				break;
			case Consts.PRIORITY_IMPORTANT:
				newTask.setPriority(Consts.PRIORITY_IMPORTANT_VALUE);
				break;
			case Consts.PRIORITY_NORMAL:
				newTask.setPriority(Consts.PRIORITY_NORMAL_VALUE);
				break;			
			default:
				return Consts.STRING_UPDATE_CORRECT;
		}
		
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
		try {
			Collections.sort(tasksBuffer, new NameComparator());
		} catch (Exception e) {
			System.err.println(e.getMessage());
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
			//@author A0117993R
			if(GoogleCal.isOnline()) {
				try {
					if(gCal.withExistingToken()){
						logger.log(Level.INFO,"Deleting- sync with google.");
						gCal.deleteEvent(Converter.jsonToTask(task).getName());
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}else{
				logger.log(Level.INFO,"Deleting- offline [saving to file].");
				cacheMap.put("DELETE", task);
			}
			return String.format(Consts.STRING_DELETE, fileName,task.get(Consts.NAME));
		}else{
			return Consts.USAGE_DELETE;
		}
	}
	
	//@author A0112069M
	public String block(String dateString) {
		List<DateGroup> dateGrp = dateParser.parse(dateString);
		if(!dateGrp.isEmpty() && dateGrp.get(0).getDates().size() == 2) {
			Date startDate = dateGrp.get(0).getDates().get(0);
			Date endDate = dateGrp.get(0).getDates().get(1);			
			Task task = new Task("Blocked " + Consts.FORMAT_PRINT_TIME.format(startDate) + " > " +  Consts.FORMAT_PRINT_TIME.format(endDate), "", startDate, endDate, 0, 0, Consts.STATUS_BLOCK_TASK);
			add(task);
			return String.format(Consts.STRING_BLOCK, Consts.FORMAT_PRINT_DATE.format(startDate), Consts.FORMAT_PRINT_DATE.format(endDate));
		} else {
			return Consts.STRING_BLOCK_FAIL;
		}
	}
	
	//@author A0117993R
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
		loadCacheBuffer();
		return gCal.syncGCal(getTimedTasksBuffer());
	}
	
	public void saveCache(){
		saveCache = new SaveCache();
		saveCache.executeCommand();
	}
	
	public boolean undo(){
		if (!opStack.isEmpty()) {
			opStack.pop().undo();
			return true;
		}else{
			return false;
		}
	}

	//@author A0112069M
	@SuppressWarnings("deprecation")
	private int getTimeFromDate(Date date) {
		return date.getHours() *  60 * 60 + date.getMinutes() * 60 + date.getSeconds();
	}
	
	private boolean isDefaultTime(Date date) {
		Date dateNow = new Date();
		return (getTimeFromDate(dateNow) - getTimeFromDate(date) <= TIME_EPS);
	}

	private Date getEndOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);
	    return calendar.getTime();
	}

	private Date getStartOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	    return calendar.getTime();
	}
	
	private Date checkWeekly(Date dateStart1, Date dateEnd1, Date dateStart2, Date dateEnd2) {
		DateTime dS1 = new DateTime(dateStart1);
		DateTime dE1 = new DateTime(dateEnd1);
		if (dE1.isAfter(dS1.plusDays(6))) {
			dE1 = dS1.plusDays(6);
		}
		DateTime dS2 = new DateTime(dateStart2);
		DateTime dE2 = new DateTime(dateEnd2);
		if (dE2.isAfter(dS2.plusDays(6))) {
			dE2 = dS2.plusDays(6);
		}
		for (DateTime curDate1 = dS1; curDate1.isBefore(dE1); curDate1 = curDate1.plusDays(1)) {
			for (DateTime curDate2 = dS2; curDate2.isBefore(dE2); curDate2 = curDate2.plusDays(1)) {
				if (Days.daysBetween(curDate1, curDate2).getDays() % 7 == 0) { 
					return curDate2.toDate();
				}
			}
		}
		return null;
	}
	
	private Date checkMonthly(Date dateStart1, Date dateEnd1, Date dateStart2, Date dateEnd2) {
		int dayStart1 = (new DateTime(dateStart1)).getDayOfMonth();
		int dayEnd1 = (new DateTime(dateEnd1)).getDayOfMonth();
		int dayStart2 = (new DateTime(dateStart2)).getDayOfMonth();
		int dayEnd2 = (new DateTime(dateEnd2)).getDayOfMonth();
		if (Math.max(dayStart1, dayStart2) <= Math.min(dayEnd1, dayEnd2)) {
			return dateStart1;
		}
		else {
			return null;
		}
		
	}
	
	public String complete(JSONObject jTargetTask, int newStatus) {
		logicComplete = new Complete();
		logicComplete.setFileName(fileName);
		logicComplete.setStatus(newStatus);
		logicComplete.setTask(jTargetTask);
		opStack.add(logicComplete);
		if(logicComplete.executeCommand()){
			return String.format(Consts.STRING_COMPLETE, jTargetTask.get(Consts.NAME));
		}else{
			return Consts.STRING_COMPLETE_FAIL;
		}
	}
	
	private Date maxDate(Date date1, Date date2) {
		if (date1.after(date2)) {
			return date1;
		} else {
			return date2;
		}
	}
	
	public SearchResult search(String keyword, int statusType) throws IOException {
		//tasksBuffer inside is different from outside
		ArrayList <JSONObject> tasksBuffer;
		ArrayList <Date> date = new ArrayList<Date>();
		
		if (statusType == Consts.STATUS_TIMED_TASK) {
			tasksBuffer = getTimedTasksBuffer();
			if (keyword.trim().toLowerCase().equals("block")) {
				return new SearchResult(getBlockTasksBuffer(), date);
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
					date.add(maxDate(task.getStartDate(), date1));
				} else if (task.getFrequency() == Consts.FREQUENCY_DAILY_VALUE) {
					foundLine.add(tasksBuffer.get(i));
					date.add(maxDate(task.getStartDate(), date1));
				} else if (task.getFrequency() == Consts.FREQUENCY_WEEKLY_VALUE && 
						checkWeekly(task.getStartDate(), task.getEndDate(), date1, date2) != null) {
					foundLine.add(tasksBuffer.get(i));
					date.add(checkWeekly(task.getStartDate(), task.getEndDate(), date1, date2));
				} else if (task.getFrequency() == Consts.FREQUENCY_MONTHLY_VALUE && 
						checkMonthly(task.getStartDate(), task.getEndDate(), date1, date2) != null) {
					foundLine.add(tasksBuffer.get(i));
					date.add(checkMonthly(task.getStartDate(), task.getEndDate(), date1, date2));
				} 
			}
		}
		logger.log(Level.INFO,foundLine.toString());
		return new SearchResult(foundLine, date);
	}
	
	public Boolean dateBefore(Date x, Date y){
		return Consts.FORMAT_COMPARE_DATE.format(x).compareTo(Consts.FORMAT_COMPARE_DATE.format(y)) <= 0;
	}
}
