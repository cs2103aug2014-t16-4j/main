package gui;

import java.util.Scanner;

import logic.TextBuddyLogic;
import model.Task;

public class TextBuddyUI {
	
	private static final String STRING_WELCOME = "Welcome to TextBuddy.%1$s is ready for use.\n";
	private static final String STRING_HELP = "Usage: TextBuddy <FILENAME.txt>\nSupported commands -> add , display , delete , clear , sort , search , exit";
	private static final String STRING_NOT_SUPPORTED_COMMAND = "Command is not supported";
	private static final String STRING_ADD = "added to %1$s: \"%2$s\"";
	private static final String ERROR_ADD = "Task cannot be blank.";
	private static final String ERROR_UNKNOWN = "Unknown error occured!";
	
	Scanner scanner;
	TextBuddyLogic logic;
	//possible commands
	public enum Commands{
		ADD,DISPLAY,DELETE,CLEAR,EXIT
	};
	
	public TextBuddyUI(String[] args){
		
	}
	public void checkArgs(String[] args) {
		if(args.length != 1){
			printHelp();
			systemExit();
		}
		String fileName = args[0];
		fileName = checkFileName(fileName);
		init(fileName);
	}
	private void printHelp() {
		printStatement(STRING_HELP);
	}
	public void init(String fileName){
		logic = new TextBuddyLogic(fileName);
		printWelcomeMsg(fileName);
		printStatement(start(fileName));
	}

	private String start(String fileName) {
		scanner = new Scanner(System.in);
		String userInput = "";
		String[] splittedString;
		String task = "";
		Commands command;
		for(;;){
			userInput = scanner.nextLine();
			splittedString = getSplittedString(userInput);
			command = getCommandType(splittedString[0]);
			task = splittedString[1];
			if(command != null){
				switch(command){
				case ADD:
					return add(task);
				case DISPLAY:
					return display();
				case CLEAR:
					return clear();
				case DELETE:
					return delete(task);
				case EXIT:
					systemExit();
				default:
					printStatement(STRING_NOT_SUPPORTED_COMMAND);
					break;
				}
			}else{
				printStatement(STRING_NOT_SUPPORTED_COMMAND);
			}
		}
	}
	private String delete(String task) {
		// TODO Auto-generated method stub
		return null;
	}
	private String clear() {
		// TODO Auto-generated method stub
		return null;
	}
	private String display() {
		
		return null;
	}
	private String add(String task) {
		Task tsk = new Task(task);
		if(tsk != null && !tsk.isEmpty()){
			boolean isSuccess = logic.add(tsk);
			if(isSuccess){
				return STRING_ADD;
			}else{
				return ERROR_UNKNOWN;
			}
		}else{
			return ERROR_ADD;
		}
	}
	private String[] getSplittedString(String userInput){
		String[] splittedString = userInput.split(" ",2);
		return splittedString;
	}

	private static String checkFileName(String fileName) {
		String[] parts = fileName.split("\\.(?=[^\\.]+$)");
		if(parts.length!=2 || !parts[1].equalsIgnoreCase("txt")){
			fileName = fileName+".txt";
		}
		return fileName;
	}
	
	private void printWelcomeMsg(String fileName){
		System.out.printf(STRING_WELCOME,fileName);
	}

	private Commands getCommandType(String command) {
		if(command != null){
			for(Commands cmd:Commands.values()){
				if(command.equalsIgnoreCase(cmd.toString())){
					return cmd;
				}
			}
		}
		return null;
	}
	
	private void printStatement(String str){
		System.out.println(str);
	}
	private void systemExit(){
		System.exit(0);
	}
}
