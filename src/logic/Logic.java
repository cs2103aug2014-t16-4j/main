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
import java.util.Map;
import java.util.TreeMap;

import model.Task;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Logic {
	//Json key strings
	private static final String NAME = "Name";
	private static final String DESCRIPTION = "Description";
	private static final String DATE = "Date";
	private static final String PRIORITY = "Priority";
	private static final String FREQUENCY = "Frequency";
	
	//Feedback strings
	private static final String STRING_DISPLAY = "%d. %s";
	private static final String STRING_DELETE = "deleted from %s: \"%s\"";
	private static final String STRING_NOT_FOUND = "None was found\n";
	private static final String STRING_DELETE_NOT_FOUND = "Line number not found! None was deleted.";
	private static final String STRING_FILE_EMPTY = "%s is empty\n";

	String fileName;
	ArrayList <JSONObject> tasksBuffer;

	public Logic(String fileName) {
		this.fileName = fileName;
		tasksBuffer = new ArrayList<JSONObject>();
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

	@SuppressWarnings("unchecked")
	public boolean add(Task task) {
		try {
			FileWriter fstream = new FileWriter(fileName, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			JSONObject jTask=new JSONObject();
			jTask.put(NAME, task.getName());
			jTask.put(DESCRIPTION, task.getDescription());
			jTask.put(DATE, task.getStartDate().toString());
			jTask.put(PRIORITY, task.getPriority());
			jTask.put(FREQUENCY, task.getFrequency());
			bufferedWriter.write(jTask.toString()+"\r\n");
			bufferedWriter.close();
			tasksBuffer.add(jTask);
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public ArrayList<JSONObject> display() throws IOException {
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
		return tasks;
	}

	public String delete(int lineNo) throws IOException {
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
	}

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
			sortKey = obj.get(NAME).toString() + " " + obj.get(DATE).toString();
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

	public ArrayList<JSONObject> search(String keyword) throws IOException {
		keyword = keyword.toLowerCase(); // case insensitive
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
		return foundLine;
	}
	
	private Task jsonToTask(JSONObject obj){
		Task temp = null;
		try{
			temp = new Task(obj.get(NAME).toString());
			temp.setDescription(obj.get(DESCRIPTION).toString());
			DateFormat formatter = new SimpleDateFormat("dd/M/yyyy");
			temp.setStartDate(formatter.parse(obj.get(DATE).toString()));
			System.out.println(obj.get(FREQUENCY));
			temp.setFrequency((int) obj.get(FREQUENCY));
			temp.setPriority((int) obj.get(PRIORITY));
		}catch(Exception e){
			//e.printStackTrace();
		}
		return temp;	
	}

}
