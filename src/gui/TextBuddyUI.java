package gui;

import java.io.IOException;
import java.util.Scanner;

import logic.TextBuddyLogic;
import model.Task;

public class TextBuddyUI {
	
	private static final String STRING_WELCOME = "Welcome to TextBuddy.%1$s is ready for use.\n";
	private static final String STRING_HELP = "Usage: TextBuddy <FILENAME.txt>\nSupported commands -> add , display , delete , clear , sort , search , exit";
	private static final String STRING_NOT_SUPPORTED_COMMAND = "Command is not supported";
	private static final String STRING_ADD = "added to %1$s: \"%2$s\"";
	private static final String STRING_CLEAR = "All content deleted from %1$s";
	
	//ERRORS
	private static final String ERROR_ADD = "Task cannot be blank.";
	private static final String ERROR_UNKNOWN = "Unknown error occured!";
	
	//USAGE
	private static final String USAGE_ADD = "Usage: add <todo>";
	private static final String USAGE_DELETE = "Usage: delete <lineno>";
	
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
		start(fileName);
	}

	private void start(String fileName) {
		scanner = new Scanner(System.in);
		String userInput = "";
		String[] splittedString;
		String task = "";
		Commands command;
		for(;;){
			System.out.print("Command: ");
			userInput = scanner.nextLine();
			splittedString = getSplittedString(userInput);
			command = getCommandType(splittedString[0]);
			if(splittedString.length>1){
				task = splittedString[1];
			}
			if(command != null){
				switch(command){
				case ADD:
					printStatement(add(task));
					break;
				case DISPLAY:
					printStatement(display());
					break;
				case CLEAR:
					printStatement(clear());
					break;
				case DELETE:
					printStatement(delete(task));
					break;
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
	private String delete(String lineNo) {
		if(lineNo != null && !lineNo.isEmpty()){
			int lineNumber;
			try{
				lineNumber = Integer.parseInt(lineNo);
				return logic.delete(lineNumber);
			}catch(NumberFormatException | IOException e){
				return USAGE_DELETE;
			}
		}else{
			return USAGE_DELETE;
		}
	}

	private String clear() {
		boolean isCleared = logic.clear();
		if(isCleared){
			return String.format(STRING_CLEAR, logic.getFileName());
		}else{
			return ERROR_UNKNOWN;
		}
	}

	private String display() {
		try {
			return logic.display();
		} catch (IOException e) {
			return ERROR_UNKNOWN;
		}
	}

	private String add(String task) {
		Task tsk = new Task(task);
		if(tsk != null && !tsk.isEmpty()){
			boolean isSuccess = logic.add(tsk);
			if(isSuccess){
				return String.format(STRING_ADD,logic.getFileName(),task);
			}else{
				return USAGE_ADD;
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
		printStatement("Bye!");
		System.exit(0);
	}
}
