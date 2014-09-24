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

public class TextBuddyLogic {

	private static final String STRING_DISPLAY = "%d. %s\n";
	private static final String STRING_DELETE = "deleted from %s: \"%s\"";
	private static final String STRING_NOT_FOUND = "None was found\n";
	private static final String STRING_DELETE_NOT_FOUND = "Line number not found! None was deleted.";
	private static final String STRING_FILE_EMPTY = "%s is empty\n";

	String fileName;

	public TextBuddyLogic(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean add(Task task) {
		try {
			FileWriter fstream = new FileWriter(fileName, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			bufferedWriter.write(task.getName() + "\r\n");
			bufferedWriter.close();
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public String display() throws IOException {
		String todos = "";
		String line;
		int lineNo = 1;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			while ((line = in.readLine()) != null) {
				todos += String.format(STRING_DISPLAY, lineNo, line);
				lineNo++;
			}
			if (todos.isEmpty()) {
				todos = String.format(STRING_FILE_EMPTY, fileName);
			}
			in.close();
		} catch (FileNotFoundException e) {
		}
		// To escape the last next line character ('\n')
		return todos.length()>0 ?todos.substring(0, todos.length() - 1) : "";
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

	public String search(String keyword) throws IOException {
		keyword = keyword.toLowerCase(); // case insensitive
		String foundLine = "";
		String line;
		int lineNo = 1;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			while ((line = in.readLine()) != null) {
				if (line.contains(keyword)) {
					foundLine += String.format(STRING_DISPLAY, lineNo, line);
					lineNo++;
				}
			}
			if (foundLine.isEmpty()) {
				foundLine = STRING_NOT_FOUND;
			}
			in.close();
		} catch (FileNotFoundException e) {
		}
		// To escape the last next line character ('\n')
		return foundLine.substring(0, foundLine.length() - 1);
	}

}
