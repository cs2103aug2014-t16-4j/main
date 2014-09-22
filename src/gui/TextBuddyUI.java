package gui;

import java.io.IOException;
import java.util.Scanner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

import logic.TextBuddyLogic;
import model.Task;
import org.eclipse.wb.swt.SWTResourceManager;

public class TextBuddyUI {

	private static Boolean blnMouseDown=false;
	private static int xPos=0;
	private static int yPos=0;
	private Display display;
	private Shell shell;
	private Text input;
	private Label statusInd;
	private Composite statusComposite;
	ScrolledComposite dayComposite;
	ScrolledComposite somedayComposite;

	// FEEDBACK STRINGS
	private static final String STRING_WELCOME = "Welcome to TextBuddy. %1$s is ready for use.\n";
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
//		if (args.length != INPUT_REQUIREMENT) {
//			printHelp();
//			systemExit();
//		}
		//alex-added default file name so one can run on eclipse 
		String fileName = args.length>0?args[0]:"mytext.txt"; 
		fileName = checkFileName(fileName);
		init(fileName);
	}

	private void printHelp() {
		printStatement(STRING_HELP);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void init(String fileName) {
		logic = new TextBuddyLogic(fileName);
		
		display = new Display();
		shell = new Shell (display, SWT.NO_TRIM | SWT.ON_TOP);
		shell.setSize(300, 620);
		shell.setLayout(null);

		input = new Text(shell, SWT.BORDER);
		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR){
					delegateTask(input.getText());
					input.setText("");
				}
			}
		});
		input.setBounds(10, 10, 245, 19);
		input.setFocus();

		Button help = new Button(shell, SWT.NONE);
		help.setBounds(261, 8, 35, 25);
		help.setText("?");
		
		dayComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		dayComposite.setBounds(10, 35, 280, 405);
		dayComposite.setExpandHorizontal(true);
		dayComposite.setExpandVertical(true);
		
		somedayComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		somedayComposite.setBounds(10, 446, 280, 144);
		somedayComposite.setExpandHorizontal(true);
		somedayComposite.setExpandVertical(true);
		
		statusComposite = new Composite(shell, SWT.NONE);
		statusComposite.setBounds(10, 596, 280, 14);
		
		statusInd = new Label(statusComposite, SWT.NONE);
		statusInd.setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.NORMAL));
		statusInd.setBounds(0, 0, 280, 14);
		statusInd.setAlignment(SWT.CENTER);

		positionWindow();
		
		shell.open();
		
		printWelcomeMsg(fileName);

		enableDrag();

		disposeDisplay();

//		logic = new TextBuddyLogic(fileName);
//		printWelcomeMsg(fileName);
//		start(fileName);
	}

	private void disposeDisplay() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
		//alex-temp to make it exit after gui is closed
		systemExit();
	}

	private void enableDrag() {
		shell.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub
				blnMouseDown=false;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				blnMouseDown=true;
				xPos=e.x;
				yPos=e.y;
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		shell.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				// TODO Auto-generated method stub
				if(blnMouseDown){

					shell.setLocation(shell.getLocation().x+(e.x-xPos),shell.getLocation().y+(e.y-yPos));
				}
			}
		});
	}

	private void positionWindow() {
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		shell.setLocation(x, y);
	}

	public void start(String fileName) {
		scanner = new Scanner(System.in);
		for (;;) {
			System.out.print(STRING_ENTER_COMMAND);
			delegateTask(scanner.nextLine());
		}
	}

	private void delegateTask(String userInput) {
		String[] splittedString;
		String task = "";
		Commands command;
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
		printStatement(String.format(STRING_WELCOME, fileName));
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
		if(!shell.isDisposed()){
			statusInd.setText(str);
			statusComposite.layout();
		}
		System.out.println(str);
	}

	private void systemExit() {
		printStatement(STRING_EXIT);
		System.exit(0);
	}
}
