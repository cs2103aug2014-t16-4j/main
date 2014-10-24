package gui;

import java.io.IOException;
import java.util.ArrayList;

import logic.CommandEnum;
import logic.Consts;
import logic.GoogleCal;
import logic.LogicController;
import logic.LogicParser;
import model.Task;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.browser.WindowEvent;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
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
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

public class UIController {

	public ArrayList<JSONObject> taskList;

	public static Boolean ISMAC = false;
	private static Boolean BLNMOUSEDOWN=false;
	private static int XPOS=0;
	private static int YPOS=0;
	private List dayList;
	private LogicParser parser = new LogicParser();
	private CommandEnum selectedCommand = CommandEnum.INVALID;

	Display DISPLAY;
	Text input;
	Shell SHELL;
	Shell authShell;
	Browser browser;
	Composite statusComposite;
	Label statusInd;
	Tray tray;
	Table floatingTaskTable;
	ScrolledComposite floatingTaskComposite;
	ScrolledComposite timedTaskComposite;
	LogicController logic;


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
		renderTimedTaskContainer();
		renderAuthPopup();

		printStatement(Consts.RENDER_BOTH);
		printWelcomeMsg(fileName);
		SHELL.open();
		enableDrag();
		disposeDisplay();
	}

	private void renderTimedTaskContainer() {
		timedTaskComposite = new ScrolledComposite(SHELL, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		timedTaskComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		timedTaskComposite.setBounds(10, 35, 280, 405);
		timedTaskComposite.setExpandHorizontal(true);
		timedTaskComposite.setExpandVertical(true);

		ExpandBar expandBar = new ExpandBar(timedTaskComposite, SWT.NONE);

		ExpandItem xpndtmNewExpanditem = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem.setExpanded(false);
		xpndtmNewExpanditem.setText("New ExpandItem");

		ExpandBar expandBar_2 = new ExpandBar(expandBar, SWT.NONE);
		xpndtmNewExpanditem.setControl(expandBar_2);
		xpndtmNewExpanditem.setHeight(xpndtmNewExpanditem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

		ExpandItem xpndtmNewExpanditem_1 = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem_1.setExpanded(false);
		xpndtmNewExpanditem_1.setText("New ExpandItem");

		ExpandBar expandBar_1 = new ExpandBar(expandBar, SWT.NONE);
		xpndtmNewExpanditem_1.setControl(expandBar_1);
		xpndtmNewExpanditem_1.setHeight(xpndtmNewExpanditem_1.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

		timedTaskComposite.setContent(expandBar);
		timedTaskComposite.setMinSize(expandBar.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		//		Composite composite1 = new Composite (expandBar, SWT.NONE);
		//		composite1.setLayout(layout);
		//		Button button1 = new Button (composite1, SWT.PUSH);
		//		button1.setText("SWT.PUSH");
		//		button1 = new Button (composite1, SWT.RADIO);
		//		button1.setText("SWT.RADIO");
		//		button1 = new Button (composite1, SWT.CHECK);
		//		button1.setText("SWT.CHECK");
		//		button1 = new Button (composite1, SWT.TOGGLE);
		//		button1.setText("SWT.TOGGLE");
		//		ExpandItem item1 = new ExpandItem (expandBar, SWT.NONE, 0);
		//		item1.setText("What is your favorite button");
		//		item1.setHeight(composite1.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		//		item1.setControl(composite1);
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
		input.setBounds(10, 10, 258, 19);
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

	private void renderHelp() {
		Label label = new Label(SHELL, SWT.NONE);
		Image small = new Image(SHELL.getDisplay(),"resource/icon_info.gif");
		label.setImage(small);
		label.setBounds(274, 13, 16, 14);

		final Shell helpWindow = new Shell(SHELL, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		helpWindow.setText("Help");
		helpWindow.setSize(250, 150);
		helpWindow.open();
		helpWindow.setVisible(false);

		final Label helpText = new Label(helpWindow, SWT.NONE);
		helpText.setText("Help text here");
		helpText.setBounds(20, 15, 100, 20);

		helpWindow.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				helpWindow.setVisible(false);
			}
		});

		label.addMouseListener(new MouseListener()
		{
			public void mouseDown(MouseEvent e){
				helpWindow.setVisible(true);
			}
			public void mouseUp(MouseEvent e){
			}
			public void mouseDoubleClick(MouseEvent e){
			}

		});
		positionWindow(helpWindow);
	}

	private void renderAuthPopup() {
		System.out.println("rendering auth");
		authShell = new Shell(DISPLAY);
		authShell.setText("Main Window");
		authShell.setLayout(new FillLayout());
		browser = new Browser(authShell, SWT.NONE);
		initialize(DISPLAY, browser);
		authShell.open();
		positionWindow(authShell);
		authShell.setVisible(false);
		browser.setUrl("http://www.google.com");
		authShell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				authShell.setVisible(false);
			}
		});
		browser.addTitleListener(new TitleListener(){
			@Override
			public void changed(TitleEvent event) {
				if (event.title != null && event.title.length() > 0) {
					authShell.setText(event.title);
					if(event.title.contains("Success")){
						System.out.println(event.title.substring(13));
						authShell.close();
					}
				}
			}
		});
	}

	private void showAuthPopup() {
		if(GoogleCal.isOnline()){
			browser.setUrl(logic.getUrl());
			authShell.setVisible(true);
		}else{
			updateStatusIndicator(Consts.STRING_USER_NOT_ONLINE);
		}
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

	static void initialize(final Display display, Browser browser) {
		browser.addOpenWindowListener(new OpenWindowListener() {
			@Override
			public void open(WindowEvent event) {
				if (!event.required) return;	/* only do it if necessary */
				Shell shell = new Shell(display);
				shell.setText("New Window");
				shell.setLayout(new FillLayout());
				Browser browser = new Browser(shell, SWT.NONE);
				initialize(display, browser);
				event.browser = browser;
			}
		});
		browser.addVisibilityWindowListener(new VisibilityWindowListener() {
			@Override
			public void hide(WindowEvent event) {
				Browser browser = (Browser)event.widget;
				Shell shell = browser.getShell();
				shell.setVisible(false);
			}
			@Override
			public void show(WindowEvent event) {
				Browser browser = (Browser)event.widget;
				final Shell shell = browser.getShell();
				if (event.location != null) shell.setLocation(event.location);
				if (event.size != null) {
					Point size = event.size;
					shell.setSize(shell.computeSize(size.x, size.y));
				}
				shell.open();
			}
		});
		browser.addCloseWindowListener(new CloseWindowListener() {
			@Override
			public void close(WindowEvent event) {
				Browser browser = (Browser)event.widget;
				Shell shell = browser.getShell();
				shell.close();
			}
		});
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
		SHELL = new Shell (DISPLAY, SWT.MODELESS);
		//SHELL = new Shell (DISPLAY, SWT.ON_TOP | SWT.MODELESS);
		SHELL.setSize(300, 620);
		SHELL.setLayout(null);

		FocusListener listener = new FocusListener() {
			public void focusGained(FocusEvent event) {
				SHELL.setFocus();
			}
			public void focusLost(FocusEvent event) {
			}
		};
		SHELL.addFocusListener(listener);
		positionWindow(SHELL);
	}

	private void disposeDisplay() {
		while (!SHELL.isDisposed()) {
			if (!DISPLAY.readAndDispatch()) {
				DISPLAY.sleep();
			}
		}
		DISPLAY.dispose();
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

	private void positionWindow(Shell sh) {
		Monitor primary = DISPLAY.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = sh.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		sh.setLocation(x, y);
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
			case SYNC:
				showAuthPopup();
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
