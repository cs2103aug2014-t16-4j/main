package gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import logic.Logic;
import logic.LogicParser;
import model.Task;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.json.simple.JSONObject;
import org.eclipse.swt.widgets.Table;

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
	
	//Json key strings
	private static final String NAME = "Name";
	private static final String DESCRIPTION = "Description";
	private static final String DATE = "Date";
	private static final String PRIORITY = "Priority";
	private static final String FREQUENCY = "Frequency";

	// FEEDBACK STRINGS
	private static final String STRING_WELCOME = "Welcome to TextBuddy. %1$s is ready for use.\n";
	private static final String STRING_HELP = "Usage: TextBuddy <FILENAME.txt>\nSupported commands -> add , display , delete , clear , sort , search , exit";
	private static final String STRING_NOT_SUPPORTED_COMMAND = "Command is not supported";
	private static final String STRING_ENTER_COMMAND = "Command: ";
	private static final String STRING_ADD = "added to %1$s: \"%2$s\"";
	private static final String STRING_CLEAR = "All content deleted from %1$s";
	private static final String STRING_FOUND_LINE = "=== Found line ===";
	private static final String STRING_SORTED = "List Sorted";
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
	private static final int RENDER_STATUS_INDICATOR = 0;
	private static final int RENDER_DAY = 1;
	private static final int RENDER_SOMEDAY = 2;
	private static final int RENDER_BOTH = 3;
	Scanner scanner;
	Logic logic;
	private List dayList;
	private LogicParser parser = new LogicParser();
	private Table somedayTable;

	// possible commands
	public enum Commands {
		ADD, DISPLAY, DELETE, CLEAR, SORT, SEARCH, EXIT
	};

	public TextBuddyUI(String fileName) {
		logic = new Logic(fileName);
	}
	
	public TextBuddyUI(String[] args) {
	}

	public void checkArgs(String[] args) throws IOException {
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
		//needs to change to pop up
		printStatement(STRING_HELP);
	}

	/**
	 * @throws IOException 
	 * @wbp.parser.entryPoint
	 */
	public void init(String fileName) throws IOException {
		logic = new Logic(fileName);
		logic.init();
		display = new Display();
		renderShell();
		createTrayIcon();
		renderInput();
		renderHelpButton();
		
		dayComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		dayComposite.setBounds(10, 35, 280, 405);
		dayComposite.setExpandHorizontal(true);
		dayComposite.setExpandVertical(true);
		
		somedayComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		somedayComposite.setBounds(10, 446, 280, 144);
		somedayComposite.setExpandHorizontal(true);
		somedayComposite.setExpandVertical(true);
		
		somedayTable = new Table(somedayComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		somedayTable.setHeaderVisible(false);
		somedayTable.setLinesVisible(true);
		somedayComposite.setContent(somedayTable);
		somedayComposite.setMinSize(somedayTable.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		TableColumn names = new TableColumn(somedayTable, SWT.LEFT);
		names.setWidth(276);
		
		renderStatusIndicator();
		printStatement(display(), RENDER_BOTH);
		printWelcomeMsg(fileName);
		positionWindow();
		shell.open();
		enableDrag();
		disposeDisplay();
	}

	private void renderInput() {
		input = new Text(shell, SWT.BORDER);
		input.setFocus();
		input.setBounds(10, 10, 245, 19);
		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR){
					delegateTask(input.getText());
					input.setText("");
				}
			}
		});

		FocusListener listener = new FocusListener() {
			public void focusGained(FocusEvent event) {
				input.setFocus();
			}
			public void focusLost(FocusEvent event) {
			}
		};
		input.addFocusListener(listener);
	}

	private void renderStatusIndicator() {
		statusComposite = new Composite(shell, SWT.NONE);
		statusComposite.setBounds(10, 596, 280, 14);
		
		statusInd = new Label(statusComposite, SWT.NONE);
		statusInd.setFont(SWTResourceManager.getFont("Lucida Grande", 8, SWT.NORMAL));
		statusInd.setBounds(0, 0, 280, 14);
		statusInd.setAlignment(SWT.CENTER);
	}

	private void renderHelpButton() {
		Button help = new Button(shell, SWT.NONE);
		help.setBounds(261, 8, 35, 25);
		help.setText("?");
	}

	private void renderShell() {
		shell = new Shell (display, SWT.ON_TOP | SWT.MODELESS);
		shell.setSize(300, 620);
		shell.setLayout(null);
		
		FocusListener listener = new FocusListener() {
			public void focusGained(FocusEvent event) {
				shell.setFocus();
			}
			public void focusLost(FocusEvent event) {
				//shell.setMinimized(true);
				//shell.setVisible(false);
			}
		};
		shell.addFocusListener(listener);
	}

	private void createTrayIcon() {
		Image image = new Image (display, 16, 16);
		Image image2 = new Image (display, 16, 16);
		GC gc = new GC(image2);
		gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		gc.fillRectangle(image2.getBounds());
		gc.dispose();
		final Tray tray = display.getSystemTray ();
		if (tray == null) {
			System.out.println ("The system tray is not available");
		} else {
			final TrayItem item = new TrayItem (tray, SWT.NONE);
			item.setToolTipText("SWT TrayItem");
			item.addListener (SWT.Show, new Listener () {
				@Override
				public void handleEvent (Event event) {
					System.out.println("show");
				}
			});
			item.addListener (SWT.Hide, new Listener () {
				@Override
				public void handleEvent (Event event) {
					System.out.println("hide");
				}
			});
			item.addListener (SWT.Selection, new Listener () {
				@Override
				public void handleEvent (Event event) {
					if(!shell.isVisible()){
						System.out.println("showing window");
						shell.setVisible(true);
						shell.setMinimized(false); 
						input.setFocus();
						shell.forceActive();
					}
					else{
						System.out.println("hiding window");
						shell.setMinimized(true);
						shell.setVisible(false);
					}
				}
			});
			item.addListener (SWT.DefaultSelection, new Listener () {
				@Override
				public void handleEvent (Event event) {
					System.out.println("default selection");
				}
			});
			final Menu menu = new Menu (shell, SWT.POP_UP);
			for (int i = 0; i < 8; i++) {
				MenuItem mi = new MenuItem (menu, SWT.PUSH);
				mi.setText ("Item" + i);
				mi.addListener (SWT.Selection, new Listener () {
					@Override
					public void handleEvent (Event event) {
						System.out.println("selection " + event.widget);
					}
				});
				if (i == 0) menu.setDefaultItem(mi);
			}
			item.addListener (SWT.MenuDetect, new Listener () {
				@Override
				public void handleEvent (Event event) {
					menu.setVisible (true);
				}
			});
			item.setImage (image2);
			item.setHighlightImage (image);
		}
	}

	private void disposeDisplay() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
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
				printStatement(display(), RENDER_BOTH);
				break;
			case CLEAR:
				printStatement(clear());
				break;
			case DELETE:
				printStatement(delete(task));
				break;
			case SORT:
				printStatement(sort(), RENDER_BOTH);
				break;
			case SEARCH:
				printStatement(search(task), RENDER_BOTH);
				return;
			case EXIT:
				systemExit();
			default:
				printStatement(STRING_NOT_SUPPORTED_COMMAND);
				break;
			}
		} else {
			printStatement(STRING_NOT_SUPPORTED_COMMAND);
		}
		printStatement(display(), RENDER_BOTH);
	}

	public ArrayList<JSONObject> search(String keyword) {
		try {
			return logic.search(keyword);
		} catch (IOException e) {
		}
		return null;
	}

	public ArrayList<JSONObject> sort() {
		try {
			logic.sort();
		} catch (Exception e) {
		}
		//printStatement(STRING_SORTED);
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

	public ArrayList<JSONObject> display() {
		try {
			//System.out.println(logic.display().toString());
			return logic.display();
		} catch (IOException e) {
		}
		return null;
	}

	public String add(String task) {
		Task tsk = parser.decompose(task);
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

	private void printStatement(String str){
		if(!shell.isDisposed()){
			updateStatusIndicator(str);
		}
	}
	private void printStatement(ArrayList<JSONObject> str, int mode) {
		/*if(!shell.isDisposed() && mode == RENDER_STATUS_INDICATOR){
			updateStatusIndicator(str.get(0).toJSONString());
		}*/
		if(mode == RENDER_BOTH){
			somedayTable.removeAll();
			updateSomeday(str);
		}
		//System.out.println(str.toString());
	}

	private void updateStatusIndicator(String str) {
		statusInd.setText(str);
		statusComposite.layout();
	}

	private void updateSomeday(ArrayList<JSONObject> str) {
		for(int i=0;i<str.size();i++){
			TableItem item = new TableItem(somedayTable, 0);
            item.setText((i+1)+". "+str.get(i).get(NAME).toString());
            item.setForeground(getColorWithPriority(Integer.parseInt(str.get(i).get(PRIORITY).toString())));
		}
	}
	
	private Color getColorWithPriority(int p){
		if(p==1){
			return display.getSystemColor(SWT.COLOR_RED);
		}
		else if(p==2){
			return display.getSystemColor(SWT.COLOR_BLACK);
		}
		else{
			return new Color(display, 204,204,204);
		}
	}
	
	private void systemExit() {
		printStatement(STRING_EXIT);
		System.exit(0);
	}
}
