/**
 * @author Kaung Htet Aung (A0117993R)
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *	This program is used to store to-dos.User is able to display, delete and clear the content.
 */

public class TextBuddy {

	// Possible commands
	public enum Commands{
		ADD,DISPLAY,DELETE,CLEAR,EXIT
	};

	public static void main(String[] args) {
		if(args.length != 1){
			printHelp();
			System.exit(0);
		}
		String fileName = args[0];
		fileName = checkFileName(fileName);
		start(fileName);
	}
	
	// Show help screen if user forgets to include second argument 
	public static void printHelp(){
		String helpString = "Usage: TextBuddy <FILENAME.txt>\nSupported commands -> add , display , delete , clear , exit";
		printStatement(helpString);
	}
	
	/**
	 * This method is to check whether the user entered the correct filename
	 * If fileName has no extension or not 'txt' , it will append 'txt' as the file extension
	 * @param fileName
	 * 			is the full string that user entered
	 * @return fileName
	 */
	public static String checkFileName(String fileName) {
		String[] parts = fileName.split("\\.(?=[^\\.]+$)");
		if(parts.length!=2 || !parts[1].equalsIgnoreCase("txt")){
			fileName = fileName+".txt";
		}
		return fileName;
	}

	/**
	 * @param fileName
	 */
	public static void start(String fileName) {
		printStatement("Welcome to TextBuddy. " + fileName + " is ready for use");
		Scanner s = new Scanner(System.in);
		String command = "";
		String[] filter = null;
		for(;;){
			System.out.print("command: ");
			command = s.nextLine();
			// Filter the first word and the rest of the command
			filter = command.split(" ",2);
			command = filter[0];
			Commands cmd = checkCommand(command);
			if (cmd!=null) {
				switch (cmd) {
				case ADD:
					if(filter.length>1){
						checkAddCommand(fileName, filter);
					}else{
						printStatement("Usage: add <todo>");
					}
					break;
				case DISPLAY:
					try {
						printStatement(display(fileName));
					} catch (IOException e) {
					}
					break;
				case DELETE:
					if(filter.length == 2){
						int lineNumber;
						try{
							lineNumber = Integer.parseInt(filter[1]);
							printStatement(delete(fileName,lineNumber));
						}catch(NumberFormatException | IOException e){
							printStatement("Usage: delete <lineNo>");
						}
					}else{
						printStatement("Usage: delete <lineNo>");
					}
					break;
				case CLEAR:
					if(clear(fileName)){
						printStatement("all content deleted from "+ fileName);
					}else{
						printStatement("Error");
					}
					break;
				case EXIT:
					System.exit(0);
					break;
				default:
					printStatement("Command is not supported.");
					break;
				}
			}else{
				printStatement("Command is not supported.");
			}
		}
	}
	
	/**
	 * Checking which command user wants to perform 
	 * @param command
	 * 
	 */
	public static Commands checkCommand(String command){
		if(command != null){
			for(Commands cmd:Commands.values()){
				if(command.equalsIgnoreCase(cmd.toString())){
					return cmd;
				}
			}
		}
		return null;
	}
	public static void printStatement(String str){
		System.out.println(str);
	}
	
	/**
	 * This method validates the add command and perform the operation
	 * @param fileName
	 * @param filter
	 */
	public static void checkAddCommand(String fileName, String[] filter) {
		String todo = filter[1];
		if (!todo.isEmpty()) {
			if (add(todo, fileName)) {
				printStatement("added to " + fileName
						+ ": \"" + todo + "\"");
			} else {
				printStatement("Error..");
			}
		}else{
			printStatement("Todo cannnot be blank.");
		}
	}

	/**
	 * This method does the add operation 
	 * @param todo
	 * @param fileName
	 * @return true
	 * 			if successfully added
	 * @return false
	 */
	public static boolean add(String todo,String fileName){
		try {
			FileWriter fstream = new FileWriter(fileName,true);
			BufferedWriter bw = new BufferedWriter(fstream);
			bw.write(todo + "\r\n");
			bw.close();
			return true;
		} catch (IOException e) {
		}
		return false;
	}
	
	public static String display(String fileName) throws IOException{
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
	
	/**
	 * This method reads the content from file, delete appropriate index and write them back into the file
	 *   
	 * @param fileName
	 * @param lineNo
	 * @return deletedLine
	 * @throws IOException
	 */
	public static String delete(String fileName,int lineNo) throws IOException{
		String deletedLine = "";
		String line;
		ArrayList<String> existingToDos = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		while((line = in.readLine()) != null){
			existingToDos.add(line);
		}
		if (!existingToDos.isEmpty()) {
			if(lineNo > 0 && lineNo <= existingToDos.size()){
				clear(fileName);
				deletedLine = "deleted from " + fileName + ": \"" + existingToDos.get(lineNo - 1) + "\"";
				existingToDos.remove(lineNo - 1);
				for(int i=0;i<existingToDos.size();i++){
					add(existingToDos.get(i),fileName);
				}
			}else{
				deletedLine = "Line number not found! None was deleted.";
			}
		}else{
			clear(fileName);
		}
		in.close();
		return deletedLine;
	}

	public static boolean clear(String fileName){
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
