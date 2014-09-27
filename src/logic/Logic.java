package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import model.Task;

import org.json.simple.JSONObject;

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

	public Logic(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@SuppressWarnings("unchecked")
	public boolean add(Task task) {
		try {
			FileWriter fstream = new FileWriter(fileName, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			JSONObject JTask=new JSONObject();
			JTask.put(NAME, task.getName());
			JTask.put(DESCRIPTION, task.getDescription());
			JTask.put(DATE, task.getDate());
			JTask.put(PRIORITY, task.getPriority());
			JTask.put(FREQUENCY, task.getFrequency());
			bufferedWriter.write(JTask.toString());
			//bufferedWriter.write(task.getName() + "\r\n");
			bufferedWriter.close();
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public ArrayList<String> display() throws IOException {
		ArrayList<String> todos = new ArrayList<String>();
		String line;
		int lineNo = 1;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			while ((line = in.readLine()) != null) {
				todos.add(String.format(STRING_DISPLAY, lineNo, line));
				lineNo++;
			}
			if (todos.isEmpty()) {
				todos.add(String.format(STRING_FILE_EMPTY, fileName));
			}
			in.close();
		} catch (FileNotFoundException e) {
		}
		return todos;
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
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public void sort() throws Exception {
		Map<String, String> map = new TreeMap<String, String>();
		String line = "";
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		while ((line = in.readLine()) != null) {
			map.put(line, line);
		}
		in.close();
		clear();
		for (String s : map.values()) {
			add(new Task(s));
		}
	}

	public ArrayList<String> search(String keyword) throws IOException {
		keyword = keyword.toLowerCase(); // case insensitive
		ArrayList<String> foundLine = new ArrayList<String>();
		String line;
		int lineNo = 1;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			while ((line = in.readLine()) != null) {
				if (line.contains(keyword)) {
					foundLine.add(String.format(STRING_DISPLAY, lineNo, line));
					lineNo++;
				}
			}
			if (foundLine.isEmpty()) {
				foundLine.add(STRING_NOT_FOUND);
			}
			in.close();
		} catch (FileNotFoundException e) {
		}
		return foundLine;
	}

}
