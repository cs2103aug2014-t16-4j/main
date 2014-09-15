package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import model.Task;

public class TextBuddyLogic {
	
	String fileName;

	public TextBuddyLogic(String fileName){
		this.fileName=fileName;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean add(Task task){
		try {
			FileWriter fstream = new FileWriter(fileName,true);
			BufferedWriter bw = new BufferedWriter(fstream);
			bw.write(task.getName() + "\r\n");
			bw.close();
			return true;
		} catch (IOException e) {
		}
		return false;
	}
	
	public String display() throws IOException{
		String todos = "";
		String line;
		int lineNo = 1;
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			while((line = in.readLine())!=null){
				todos += lineNo+ ". "+ line + "\n";
				lineNo++;
			}
			if(todos.isEmpty()){
				todos = fileName + " is empty\n";
			}
			in.close();
		} catch (FileNotFoundException e) {
		}
		// To escape the last next line character ('\n')
		return todos.substring(0,todos.length()-1);
	}
	public String delete(int lineNo) throws IOException{
		String deletedLine = "";
		String line;
		ArrayList<String> existingToDos = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		while((line = in.readLine()) != null){
			existingToDos.add(line);
		}
		if (!existingToDos.isEmpty()) {
			if(lineNo > 0 && lineNo <= existingToDos.size()){
				clear();
				deletedLine = "deleted from " + fileName + ": \"" + existingToDos.get(lineNo - 1) + "\"";
				existingToDos.remove(lineNo - 1);
				for(int i=0;i<existingToDos.size();i++){
					add(new Task(existingToDos.get(i)));
				}
			}else{
				deletedLine = "Line number not found! None was deleted.";
			}
		}else{
			clear();
		}
		in.close();
		return deletedLine;
	}
	public boolean clear(){
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
}
