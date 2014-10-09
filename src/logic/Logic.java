package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

public class Logic {
	//Json key strings
	private static final String NAME = "Name";
	private static final String DESCRIPTION = "Description";
	private static final String STARTDATE = "StartDate";
	private static final String ENDDATE = "EndDate";
	private static final String PRIORITY = "Priority";
	private static final String FREQUENCY = "Frequency";
	
	//Feedback strings
	private static final String STRING_DISPLAY = "%d. %s";
	private static final String STRING_DELETE = "deleted from %s: \"%s\"";
	private static final String STRING_NOT_FOUND = "None was found\n";
	private static final String STRING_UPDATE = "%s is updated.\n";
	private static final String STRING_NOT_UPDATE = "%s is not updated.\n";
	private static final String STRING_DELETE_NOT_FOUND = "Line number not found! None was deleted.";
	private static final String STRING_FILE_EMPTY = "%s is empty.\n";
	private static final DateFormat formatter = new SimpleDateFormat("dd/M/yyyy HH:mm:ss");
	private static final SimpleDateFormat cmpFormatter = new SimpleDateFormat("yyyyMMdd");


	String fileName;
	ArrayList <JSONObject> tasksBuffer;
	Parser dateParser = new Parser();
	
	public Logic(String fileName) {
		this.fileName = fileName;
		tasksBuffer = new ArrayList<JSONObject>();
	}
	
	//Fetch all tasks from file in the beginning.
	public void init() throws IOException
	{
		ArrayList<JSONObject> tasks = new ArrayList<JSONObject>();
		JSONParser jsonParser = new JSONParser();
		String line;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			while ((line = in.readLine()) != null) {
				//System.out.println(line);
				JSONObject obj = (JSONObject) jsonParser.parse(line);
				tasks.add(obj);
			}
			in.close();
			setTasksBuffer(tasks);
		} catch (FileNotFoundException | ParseException e) {
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return fileName;
	}

	public ArrayList<JSONObject> getTasksBuffer() {
		return tasksBuffer;
	}

	public void setTasksBuffer(ArrayList<JSONObject> tasksBuffer) {
		this.tasksBuffer = tasksBuffer;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	//public Task toTask(JSONObject jTask);

	//@SuppressWarnings("unchecked")
	public boolean add(Task task) {
		try {
			FileWriter fstream = new FileWriter(fileName, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			JSONObject jTask = taskToJSON(task);
			bufferedWriter.write(jTask.toString()+"\r\n");
			bufferedWriter.close();
			tasksBuffer.add(jTask);
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public ArrayList<JSONObject> display() throws IOException {
		return tasksBuffer;
	}
	
	public String update(JSONObject oldTask,Task newTask){
		try {
			delete(oldTask);
			add(newTask);
			return STRING_UPDATE;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return STRING_NOT_UPDATE;
	}

	public String delete(JSONObject task) throws IOException {

		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter bufferedWriter = new BufferedWriter(fstream);
		tasksBuffer.remove(task);
		//write back tasksBuffer to file

		for (JSONObject jTask: tasksBuffer) {
			bufferedWriter.write(jTask.toString()+"\r\n");
		}
		
		bufferedWriter.close();						
		return String.format(STRING_DELETE, fileName,
				task);
	}

	/*public String delete(int lineNo) throws IOException {
		String deletedLine = "";
		String line;
		ArrayList<String> existingToDos = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		while ((line = in.readLine()) != null) {
			existingToDos.add(line);
		}
		if (!existingToDos.isEmpty()) {
			if (lineNo > 0 && lineNo <= existingToDos.size()) {
				clear();
				deletedLine = String.format(STRING_DELETE, fileName,
						existingToDos.get(lineNo - 1));
				existingToDos.remove(lineNo - 1);
				for (int i = 0; i < existingToDos.size(); i++) {
					add(new Task(existingToDos.get(i)));
				}
			} else {
				deletedLine = STRING_DELETE_NOT_FOUND;
			}
		} else {
			clear();
		}
		in.close();
		return deletedLine;
	}*/

	public boolean clear() {
		try {
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fstream);
			bw.write("");
			bw.close();
			tasksBuffer.clear();
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public void sort() throws Exception {
		
		Map<String, JSONObject> map = new TreeMap<String, JSONObject>();
		String sortKey = "";
		for(int i=0;i<tasksBuffer.size();i++){
			JSONObject obj = tasksBuffer.get(i);
			sortKey = obj.get(NAME).toString() + " " + obj.get(STARTDATE).toString();
			map.put(sortKey, obj);
		}
		clear();
		tasksBuffer.clear();
		Task tempTask;
		for(JSONObject taskObj : map.values()){
			tempTask = jsonToTask(taskObj);
			add(tempTask);
		}
	}

	public Boolean dateBefore(Date x, Date y)
	{
		return cmpFormatter.format(x).compareTo(cmpFormatter.format(y)) <= 0;
	}
	
	public ArrayList<JSONObject> search(String keyword) throws IOException {
		/*keyword = keyword.toLowerCase(); // case insensitive
		ArrayList<JSONObject> foundLine = new ArrayList<JSONObject>();
		JSONParser jsonParser = new JSONParser();
		String line;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			while ((line = in.readLine()) != null) {
				if (line.contains(keyword)) {
					JSONObject obj = (JSONObject) jsonParser.parse(line);
					foundLine.add(obj);
				}
			}
			in.close();
		} catch (FileNotFoundException | ParseException e) {
		}
		return foundLine;*/
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
	
	private Task jsonToTask(JSONObject obj){
		Task temp = null;
		try{
			temp = new Task(obj.get(NAME).toString());
			temp.setDescription(obj.get(DESCRIPTION).toString());
			temp.setStartDate(formatter.parse(obj.get(STARTDATE).toString()));
			temp.setEndDate(formatter.parse(obj.get(ENDDATE).toString()));
			temp.setFrequency((int) obj.get(FREQUENCY));
			temp.setPriority((int) obj.get(PRIORITY));
		}catch(Exception e){
			//e.printStackTrace();
		}
		return temp;	
	}

	@SuppressWarnings("unchecked")
	public JSONObject taskToJSON(Task task)
	{
		JSONObject jTask=new JSONObject();
		jTask.put(NAME, task.getName());
		jTask.put(DESCRIPTION, task.getDescription());
		jTask.put(STARTDATE, formatter.format(task.getStartDate()));
		jTask.put(ENDDATE, formatter.format(task.getEndDate()));
		jTask.put(PRIORITY, task.getPriority());
		jTask.put(FREQUENCY, task.getFrequency());
		return jTask;
	}
}
