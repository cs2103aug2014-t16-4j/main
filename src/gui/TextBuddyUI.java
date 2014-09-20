package gui;

import java.io.IOException;
import java.util.Scanner;

import logic.TextBuddyLogic;
import model.Task;

public class TextBuddyUI {

	// FEEDBACK STRINGS
	private static final String STRING_WELCOME = "Welcome to TextBuddy.%1$s is ready for use.\n";
	private static final String STRING_HELP = "Usage: TextBuddy <FILENAME.txt>\nSupported commands -> add , display , delete , clear , sort , search , exit";
	private static final String STRING_NOT_SUPPORTED_COMMAND = "Command is not supported";
	private static final String STRING_ENTER_COMMAND = "Command: ";
	private static final String STRING_ADD = "added to %1$s: \"%2$s\"";
	private static final String STRING_CLEAR = "All content deleted from %1$s";
	private static final String STRING_FOUND_LINE = "=== Found line ===";
	private static final String STRING_SORTED = "== Sorted list ==";
	private static final String STRING_EXIT = "Bye!";

	// ERRORS
	private static final String ERROR_ADD = "Task cannot be blank.";
	private static final String ERROR_UNKNOWN = "Unknown error occured!";

	// USAGE
	private static final String USAGE_ADD = "Usage: add <todo>";
	private static final String USAGE_DELETE = "Usage: delete <lineno>";

	// NUMBER CONSTANT
	private static final int INPUT_REQUIREMENT = 1;
	private static final int TASK_POSITION = 1;
	private static final int FILE_TYPE_POSITION = 1;
	private static final int FILE_VALID_LENGTH = 2;
	Scanner scanner;
	TextBuddyLogic logic;

	// possible commands
	public enum Commands {
		ADD, DISPLAY, DELETE, CLEAR, SORT, SEARCH, EXIT
	};

	public TextBuddyUI(String fileName) {
		logic = new TextBuddyLogic(fileName);
	}

	public TextBuddyUI(String[] args) {

	}

	public void checkArgs(String[] args) {
		if (args.length != INPUT_REQUIREMENT) {
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

	public void init(String fileName) {
		logic = new TextBuddyLogic(fileName);
		printWelcomeMsg(fileName);
		start(fileName);
	}

	public void start(String fileName) {
		scanner = new Scanner(System.in);
		String userInput = "";
		String[] splittedString;
		String task = "";
		Commands command;
		for (;;) {
			System.out.print(STRING_ENTER_COMMAND);
			userInput = scanner.nextLine();
			splittedString = getSplittedString(userInput);
			command = getCommandType(splittedString[0]);
			if (splittedString.length > TASK_POSITION) {
				task = splittedString[TASK_POSITION];
			}
			if (command != null) {
				switch (command) {
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
				case SORT:
					printStatement(sort());
					break;
				case SEARCH:
					printStatement(search(task));
					break;
				case EXIT:
					systemExit();
				default:
					printStatement(STRING_NOT_SUPPORTED_COMMAND);
					break;
				}
			} else {
				printStatement(STRING_NOT_SUPPORTED_COMMAND);
			}
		}
	}

	public String search(String keyword) {
		printStatement(STRING_FOUND_LINE);
		try {
			return logic.search(keyword);
		} catch (IOException e) {
			return ERROR_UNKNOWN;
		}
	}

	public String sort() {
		try {
			logic.sort();
		} catch (Exception e) {
		}
		printStatement(STRING_SORTED);
		return display();
	}

	public String delete(String lineNo) {
		if (lineNo != null && !lineNo.isEmpty()) {
			int lineNumber;
			try {
				lineNumber = Integer.parseInt(lineNo);
				return logic.delete(lineNumber);
			} catch (NumberFormatException | IOException e) {
				return USAGE_DELETE;
			}
		} else {
			return USAGE_DELETE;
		}
	}

	public String clear() {
		boolean isCleared = logic.clear();
		if (isCleared) {
			return String.format(STRING_CLEAR, logic.getFileName());
		} else {
			return ERROR_UNKNOWN;
		}
	}

	public String display() {
		try {
			return logic.display();
		} catch (IOException e) {
			return ERROR_UNKNOWN;
		}
	}

	public String add(String task) {
		Task tsk = new Task(task);
		if (tsk != null && !tsk.isEmpty()) {
			boolean isSuccess = logic.add(tsk);
			if (isSuccess) {
				return String.format(STRING_ADD, logic.getFileName(), task);
			} else {
				return USAGE_ADD;
			}
		} else {
			return ERROR_ADD;
		}
	}

	private String[] getSplittedString(String userInput) {
		String[] splittedString = userInput.split(" ", 2);
		return splittedString;
	}

	private static String checkFileName(String fileName) {
		String[] parts = fileName.split("\\.(?=[^\\.]+$)");
		if (parts.length != FILE_VALID_LENGTH
				|| !parts[FILE_TYPE_POSITION].equalsIgnoreCase("txt")) {
			fileName = fileName + ".txt";
		}
		return fileName;
	}

	private void printWelcomeMsg(String fileName) {
		System.out.printf(STRING_WELCOME, fileName);
	}

	private Commands getCommandType(String command) {
		if (command != null) {
			for (Commands cmd : Commands.values()) {
				if (command.equalsIgnoreCase(cmd.toString())) {
					return cmd;
				}
			}
		}
		return null;
	}

	private void printStatement(String str) {
		System.out.println(str);
	}

	private void systemExit() {
		printStatement(STRING_EXIT);
		System.exit(0);
	}
}
