package gui;

import java.io.IOException;
import java.util.ArrayList;

import logic.CommandEnum;
import logic.Consts;
import logic.LogicController;
import logic.LogicParser;
import model.Task;

import org.apache.commons.lang.SystemUtils;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.json.simple.JSONObject;

public class UIController {

	public ArrayList<JSONObject> taskList;
	
	public static Boolean ISMAC = false;
	private static Boolean BLNMOUSEDOWN=false;
	private static int XPOS=0;
	private static int YPOS=0;
	public static Display DISPLAY;
	public static Text input;
	ScrolledComposite timedTaskComposite;
	
	public Shell SHELL;
	public Composite statusComposite;
	public Label statusInd;
	public Button help;
	public Tray tray;
	public Table floatingTaskTable;
	public ScrolledComposite floatingTaskComposite;
	LogicController logic;
	private List dayList;
	private LogicParser parser = new LogicParser();
	private CommandEnum selectedCommand = CommandEnum.INVALID;
	
	public UIController(String[] args) {
		String fileName = args.length>0?args[0]:"mytext.txt"; 
		fileName = checkFileName(fileName);
		try {
			init(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//for testing
	public UIController(String fileName) {
		try {
			ISMAC = SystemUtils.IS_OS_MAC;
			logic = LogicController.getInstance();
			logic.init(fileName);
			taskList = logic.getDisplayTasksBuffer();
			DISPLAY = new Display();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printHelp() {
		//needs to change to pop up
		updateStatusIndicator(Consts.STRING_HELP);
	}

	/**
	 * @throws IOException 
	 * @wbp.parser.entryPoint
	 */
	public void init(String fileName) throws IOException {
		ISMAC = SystemUtils.IS_OS_MAC;
		logic = LogicController.getInstance();
		logic.init(fileName);
		taskList = logic.getDisplayTasksBuffer();
		
		DISPLAY = new Display();
		renderShell();
		renderTrayIcon();
		renderInputBox();
		renderHelp();
		renderFloatingTaskContainer();
		renderStatusIndicator();
		timedTaskComposite = new ScrolledComposite(SHELL, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		timedTaskComposite.setBounds(10, 35, 280, 405);
		timedTaskComposite.setExpandHorizontal(true);
		timedTaskComposite.setExpandVertical(true);
		
		
		
		printStatement(Consts.RENDER_BOTH);
		printWelcomeMsg(fileName);
		positionWindow();
		SHELL.open();
		enableDrag();
		disposeDisplay();
	}

	private void renderStatusIndicator() {
		statusComposite = new Composite(SHELL, SWT.NONE);
		statusComposite.setBounds(10, 596, 280, 14);
		
		int fontSize = 10;
		if(!ISMAC) {
			fontSize = 8;
		}
		
		statusInd = new Label(statusComposite, SWT.NONE);
		statusInd.setFont(SWTResourceManager.getFont("Lucida Grande", fontSize, SWT.NORMAL));
		statusInd.setBounds(0, 0, 280, 14);
		statusInd.setAlignment(SWT.CENTER);
	}

	private void renderInputBox() {
		input = new Text(SHELL, SWT.BORDER);
		input.setFocus();
		input.setBounds(10, 10, 245, 19);
		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR){
					//TaskBoxUI.delegateTask(input.getText());
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

	private void renderHelp() {
		help = new Button(SHELL, SWT.NONE);
		if(ISMAC) {
			help.setBounds(261, 8, 35, 25);
		}
		else {
			help.setBounds(261, 9, 30, 21);
		}
		help.setText("?");
	}

	private void renderFloatingTaskContainer() {
		floatingTaskComposite = new ScrolledComposite(SHELL, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		floatingTaskComposite.setBounds(10, 446, 280, 144);
		floatingTaskComposite.setExpandHorizontal(true);
		floatingTaskComposite.setExpandVertical(true);
		
		floatingTaskTable = new Table(floatingTaskComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		floatingTaskTable.setHeaderVisible(false);
		floatingTaskTable.setLinesVisible(true);
		floatingTaskComposite.setContent(floatingTaskTable);
		floatingTaskComposite.setMinSize(floatingTaskTable.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		TableColumn names = new TableColumn(floatingTaskTable, SWT.LEFT);
		names.setWidth(276);
	}

	private void renderTrayIcon() {
		Image image = new Image (DISPLAY, 16, 16);
		Image image2 = new Image (DISPLAY, 16, 16);
		GC gc = new GC(image2);
		gc.setBackground(DISPLAY.getSystemColor(SWT.COLOR_BLACK));
		gc.fillRectangle(image2.getBounds());
		gc.dispose();
		tray = DISPLAY.getSystemTray ();
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
					if(!SHELL.isVisible()){
						System.out.println("showing window");
						SHELL.setVisible(true);
						SHELL.setMinimized(false); 
						input.setFocus();
						SHELL.forceActive();
					}
					else{
						System.out.println("hiding window");
						SHELL.setMinimized(true);
						SHELL.setVisible(false);
					}
				}
			});
			item.addListener (SWT.DefaultSelection, new Listener () {
				@Override
				public void handleEvent (Event event) {
					System.out.println("default selection");
				}
			});
			final Menu menu = new Menu (SHELL, SWT.POP_UP);
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

	private void renderShell() {
		SHELL = new Shell (DISPLAY, SWT.ON_TOP | SWT.MODELESS);
		SHELL.setSize(300, 620);
		SHELL.setLayout(null);
		
		FocusListener listener = new FocusListener() {
			public void focusGained(FocusEvent event) {
				SHELL.setFocus();
			}
			public void focusLost(FocusEvent event) {
				//shell.setMinimized(true);
				//shell.setVisible(false);
			}
		};
		SHELL.addFocusListener(listener);
	}

	private void addInputListener() {
		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR){
					delegateTask(input.getText());
					input.setText("");
				}
			}
		});
	}

	private void disposeDisplay() {
		while (!SHELL.isDisposed()) {
			if (!DISPLAY.readAndDispatch()) {
				DISPLAY.sleep();
			}
		}
		DISPLAY.dispose();
		//alex-temp to make it exit after gui is closed
		systemExit();
	}

	private void enableDrag() {
		SHELL.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				BLNMOUSEDOWN=false;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				BLNMOUSEDOWN=true;
				XPOS=e.x;
				YPOS=e.y;
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {

			}
		});
		SHELL.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if(BLNMOUSEDOWN){

					SHELL.setLocation(SHELL.getLocation().x+(e.x-XPOS),SHELL.getLocation().y+(e.y-YPOS));
				}
			}
		});
	}

	private void positionWindow() {
		Monitor primary = DISPLAY.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = SHELL.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		SHELL.setLocation(x, y);
	}

	public void delegateTask(String userInput) {
		String[] splittedString;
		String task = "";
		String statusString = "";
		splittedString = getSplittedString(userInput);
		selectedCommand = getCommandType(splittedString[0]);
		if (splittedString.length > Consts.TASK_POSITION) {
			task = splittedString[Consts.TASK_POSITION];
		}
		if (selectedCommand != CommandEnum.INVALID) {
			switch (selectedCommand) {
			case ADD:
				statusString = add(task);
				taskList = getTaskList();
				break;
			case DISPLAY:
				break;
			case CLEAR:
				statusString = clear();
				taskList = getTaskList();
				break;
			case DELETE:
				statusString = delete(task);
				taskList = getTaskList();
				break;
			case SORT:
				statusString = sort();
				taskList = getTaskList();
				break;
			case SEARCH:
				search(task);
				break;
			case UPDATE:
				statusString = update(task);
				taskList = getTaskList();
				break;
			case BLOCK:
				statusString = block(task);
				break;
			case UNDO:
				undo();
				taskList = getTaskList();
				break;
			case EXIT:
				systemExit();
			default:
				updateStatusIndicator(Consts.STRING_NOT_SUPPORTED_COMMAND);
				break;
			}
			if(!statusString.isEmpty()){
				updateStatusIndicator(statusString);
			}
			printStatement(Consts.RENDER_BOTH);
		} else {
			updateStatusIndicator(Consts.STRING_NOT_SUPPORTED_COMMAND);
		}
	}

	public ArrayList<JSONObject> getDisplayList() {
		return taskList;
	}
	
	public String block(String userInput){
		if(userInput != null && !userInput.isEmpty()){
			try{
				return logic.block(userInput);
			}catch(NumberFormatException e){
				return Consts.USAGE_BLOCK;
			}
		}else{
			return Consts.USAGE_BLOCK;
		}
	}
	
	public void search(String keyword) {
		try {
			taskList = logic.search(keyword);
			if(taskList.isEmpty()){
				updateStatusIndicator(Consts.STRING_NOT_FOUND);
			}else{
				updateStatusIndicator(String.format(Consts.STRING_FOUND,taskList.size()));
			}
		} catch (IOException e) {
		}
	}

	public String update(String userInput){
		if(userInput != null && !userInput.isEmpty()){
			String[] splittedString = getSplittedString(userInput);
			if(splittedString.length != Consts.NO_ARGS_UPDATE){
				return Consts.USAGE_UPDATE;
			}
			int lineNumber;
			try{
				lineNumber = Integer.parseInt(splittedString[0]);
				Task newTask = parser.decompose(splittedString[1]);
				return logic.update(logic.getDisplayTasksBuffer().get(lineNumber-1),newTask);
			}catch(NumberFormatException e){
				return Consts.USAGE_UPDATE;
			}
		}else{
			return Consts.USAGE_UPDATE;
		}
	}

	public String sort() {
		try {
			logic.sort();
			return Consts.STRING_SORTED;
		} catch (Exception e) {
		}
		return null;
	}
	
	public String undo() {
		try {
			logic.undo();
			return Consts.STRING_UNDO;
		} catch (Exception e) {
		}
		return null;
	}

	public String delete(String lineNo) {
		if (lineNo != null && !lineNo.isEmpty()) {
			int lineNumber;
			try {
				lineNumber = Integer.parseInt(lineNo);
				return logic.delete(logic.getDisplayTasksBuffer().get(lineNumber-1));
			} catch (NumberFormatException e) {
				return Consts.USAGE_DELETE;
			}
		} else {
			return Consts.USAGE_DELETE;
		}
	}

	public String clear() {
		boolean isCleared = logic.clear();
		if (isCleared) {
			return String.format(Consts.STRING_CLEAR, logic.getFileName());
		} else {
			return Consts.ERROR_UNKNOWN;
		}
	}

	public ArrayList<JSONObject> getTaskList() {
		return logic.getDisplayTasksBuffer();
	}

	public String add(String task) {
		Task tsk = parser.decompose(task);
		if (tsk != null && !tsk.isEmpty()) {
			boolean isSuccess = logic.add(tsk);
			if (isSuccess) {
				return String.format(Consts.STRING_ADD, logic.getFileName(), task);
			} else {
				return Consts.USAGE_ADD;
			}
		} else {
			return Consts.ERROR_ADD;
		}
	}

	private String[] getSplittedString(String userInput) {
		String[] splittedString = userInput.split(" ", 2);
		return splittedString;
	}

	private static String checkFileName(String fileName) {
		String[] parts = fileName.split("\\.(?=[^\\.]+$)");
		if (parts.length != Consts.FILE_VALID_LENGTH
				|| !parts[Consts.FILE_TYPE_POSITION].equalsIgnoreCase("txt")) {
			fileName = fileName + ".txt";
		}
		return fileName;
	}

	private void printWelcomeMsg(String fileName) {
		System.out.printf(Consts.STRING_WELCOME, fileName);
		updateStatusIndicator(String.format(Consts.STRING_WELCOME, fileName));
	}

	private CommandEnum getCommandType(String firstWord) {
		if (firstWord != null) {
			for (CommandEnum cmd : CommandEnum.values()) {
				if (firstWord.equalsIgnoreCase(cmd.toString())) {
					return cmd;
				}
			}
		}
		return CommandEnum.INVALID;
	}

	private void printStatement(int mode) {
		if(mode == Consts.RENDER_BOTH){
			floatingTaskTable.removeAll();
			updatefloatingTask(taskList);
		}
	}

	private void updateStatusIndicator(String str) {
		if(!SHELL.isDisposed()){
			statusInd.setText(str);
			statusComposite.layout();
		}
	}

	private void updatefloatingTask(ArrayList<JSONObject> str) {
		for(int i=0;i<str.size();i++){
			TableItem item = new TableItem(floatingTaskTable, 0);
            item.setText((i+1)+". "+str.get(i).get(Consts.NAME).toString());
            item.setForeground(getColorWithPriority(Integer.parseInt(str.get(i).get(Consts.PRIORITY).toString())));
		}
	}
	
	public Color getColorWithPriority(int p){
		if(p==Consts.TASK_IMPORTANT){
			return DISPLAY.getSystemColor(SWT.COLOR_RED);
		}
//		else if(p==Consts.TASK_NORMAL){
//			return new Color(DISPLAY, 255,165,0);
//		}
		else{
			//return new Color(display, 204,204,204);
			return DISPLAY.getSystemColor(SWT.COLOR_BLACK);
		}
	}
	
	private void systemExit() {
		updateStatusIndicator(Consts.STRING_EXIT);
		System.exit(0);
	}
}
