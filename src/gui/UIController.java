package gui;

import gui.common.HotKey;
import gui.common.HotKeyListener;
import gui.common.Provider;

import java.awt.event.InputEvent;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import javax.swing.KeyStroke;

import static java.awt.event.KeyEvent.*;
import logic.CommandEnum;
import logic.Consts;
import logic.GoogleCal;
import logic.LogicController;
import logic.LogicParser;
import model.Task;

import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.wb.swt.SWTResourceManager;
import org.json.simple.JSONObject;

public class UIController {

	public ArrayList<JSONObject> timedList;
	public ArrayList<JSONObject> floatingList;

	public static Boolean ISMAC = false;
	private static Boolean BLNMOUSEDOWN=false;
	private static int XPOS=0;
	private static int YPOS=0;
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
	Composite timedInnerComposite;

	final Provider provider = Provider.getCurrentProvider(false);

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
			timedList = logic.getTimedTasksBuffer();
			floatingList = logic.getFloatingTasksBuffer();
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

		updateTaskList();
		renderDisplay();
		renderShell();
		renderTrayIcon();
		renderInputBox();
		renderHelp();
		renderFloatingTaskContainer();
		renderStatusIndicator();
		renderTimedTaskContainer();
		renderAuthPopup();

		renderTasks();
		printWelcomeMsg(fileName);
		SHELL.open();
		enableDrag();
		disposeDisplay();
	}

	private void renderDisplay() {
		DISPLAY = new Display();
		if(!ISMAC){
			final HotKeyListener listener = new HotKeyListener() {
				public void onHotKey(final HotKey hotKey) {
					System.out.println("hotkey");

					new Thread(new Runnable() {
						public void run() {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									if(!SHELL.isVisible() || DISPLAY.getFocusControl()==null){
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
						}
					}).start();
				}
			};

			provider.reset();
			provider.register(KeyStroke.getKeyStroke(VK_H,InputEvent.CTRL_DOWN_MASK), listener);
		}
		DISPLAY.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'z')){
					undo();
					updateTaskList();
					renderTasks();
				}
				else if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'q')){
					systemExit();
				}
				else if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 's')){
					showAuthPopup();
				}
				else if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'a')){
					e.doit = false;
					input.setText("add ");
					input.setSelection(input.getText().length());
				}
				else if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'd')){
					input.setText("delete ");
					input.setSelection(input.getText().length());
				}
				else if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'n')){
					NotifierDialog.notify("Hi There! I'm a notification widget!", "Today we are creating a widget that allows us to show notifications that fade in and out!");
				}
			}
		});

	}

	private void renderTimedTaskContainer() {
		timedTaskComposite = new ScrolledComposite(SHELL, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		timedTaskComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		timedTaskComposite.setBounds(10, 35, 280, 405);
		timedTaskComposite.setExpandHorizontal(true);
		timedTaskComposite.setExpandVertical(true);
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
		helpWindow.setSize(250, 300);
		helpWindow.open();
		helpWindow.setVisible(false);

		final Label helpText = new Label(helpWindow, SWT.NONE);
		helpText.setText("Supported commands:\nadd\ndisplay\ndelete\nupdate\nclear\nsort\nsearch\nblock\nundo\nexit\nsync");
		helpText.setBounds(20, 15, 200, 250);

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
		DISPLAY.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == '/')){
					helpWindow.setVisible(true);
				}
			}
		});
		positionWindow(helpWindow);
	}

	private void renderAuthPopup() {
		authShell = new Shell(DISPLAY);
		authShell.setText("Request for Permission");
		authShell.setLayout(new FillLayout());
		browser = new Browser(authShell, SWT.NONE);
		initialize(DISPLAY, browser);
		authShell.open();
		positionWindow(authShell);
		authShell.setVisible(false);
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
						try {
							updateStatusIndicator(syncWithGoogle(event.title.substring(13)));
						} catch (IOException e) {
							e.printStackTrace();
						}
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

	private String syncWithGoogle(String code) throws IOException{
		return logic.syncWithGoogle(code);
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
		if(ISMAC){
			names.setWidth(261);
		}
		else{
			names.setWidth(271);
		}

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
			MenuItem mi = new MenuItem (menu, SWT.PUSH);
			mi.setText ("Item");
			mi.addListener (SWT.Selection, new Listener () {
				@Override
				public void handleEvent (Event event) {
					System.out.println("selection " + event.widget);
				}
			});
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
				updateTaskList();
				break;
			case DISPLAY:
				updateTaskList();
				break;
			case CLEAR:
				statusString = clear();
				updateTaskList();
				break;
			case DELETE:
				statusString = delete(task);
				updateTaskList();
				break;
			case SORT:
				statusString = sort();
				updateTaskList();
				break;
			case SEARCH:
				search(task);
				break;
			case UPDATE:
				statusString = update(task);
				updateTaskList();
				break;
			case BLOCK:
				statusString = block(task);
				break;
			case UNDO:
				undo();
				updateTaskList();
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
			renderTasks();
		} else {
			updateStatusIndicator(Consts.STRING_NOT_SUPPORTED_COMMAND);
		}
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
			timedList = logic.search(keyword);
			if(timedList.isEmpty()){
				updateStatusIndicator(Consts.STRING_NOT_FOUND);
			}else{
				updateStatusIndicator(String.format(Consts.STRING_FOUND,timedList.size()));
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
				return logic.update(logic.getFloatingTasksBuffer().get(lineNumber-1),newTask);
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
				return logic.delete(logic.getFloatingTasksBuffer().get(lineNumber-1));
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

	public void updateTaskList() {
		timedList = logic.getTimedTasksBuffer();
		floatingList = logic.getFloatingTasksBuffer();
		sortTimedList();
	}

	private void sortTimedList() {
		Collections.sort(timedList, new Comparator<JSONObject>() {
			@Override public int compare(JSONObject t1, JSONObject t2) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				Date date1 = null;
				Date date2 = null;
				try {
					date1 = sdf.parse(t1.get(Consts.STARTDATE).toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				try {
					date2 = sdf.parse(t2.get(Consts.STARTDATE).toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return date1.compareTo(date2);
			}
		});
	}

	public String add(String task) {
		Task tsk = parser.decompose(task);
		if (tsk != null && !tsk.isEmpty()) {
			/*			boolean isSuccess = logic.add(tsk);
			if (isSuccess) {
				return String.format(Consts.STRING_ADD, logic.getFileName(), task);
			} else {
				return Consts.USAGE_ADD;				
			}*/
			return logic.add(tsk);
		} else {
			//return Consts.ERROR_ADD;
			return Consts.USAGE_ADD;				
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

	private void renderTasks() {
		if(timedInnerComposite != null){
			timedInnerComposite.dispose();
		}
		floatingTaskTable.removeAll();
		updatefloatingTask();
		updateTimedTask();
	}

	private void updateStatusIndicator(String str) {
		if(!SHELL.isDisposed()){
			statusInd.setText(str);
			statusComposite.layout();
		}
	}

	private void updateTimedTask(){
		String currentDateString = "";
		timedInnerComposite = new Composite(timedTaskComposite, SWT.NONE);
		timedInnerComposite.setLayout(new GridLayout(1, true));
		FormToolkit toolkit = null;
		Form form = null;

		for(int i=0;i<timedList.size();i++){
			JSONObject o = timedList.get(i);
			String start = o.get(Consts.STARTDATE).toString();
			String startTime = start.substring(10, start.length()-3)+" hr";
			String startDate = start.substring(0,10);
			String end = o.get(Consts.ENDDATE).toString();
			String endTime = end.substring(11, end.length()-3)+" hr";
			String desc = o.get(Consts.DESCRIPTION).toString();
			String taskName = o.get(Consts.NAME).toString();
			String shortenedTaskName = ellipsize(taskName, 30);
			String dateString = start.compareTo(end)==0?startTime:startTime+" to "+endTime;

			if(currentDateString.compareTo(startDate)!=0){
				toolkit = new FormToolkit(timedInnerComposite.getDisplay());
				currentDateString = startDate;
				form = toolkit.createForm(timedInnerComposite);
				form.setLayoutData(new GridData(GridData.FILL_BOTH));
				form.setText(currentDateString);
				ColumnLayout cl = new ColumnLayout();
				cl.maxNumColumns = 1;
				form.getBody().setLayout(cl);
			}
			Section section = null;
			desc = desc.isEmpty()?(taskName.compareTo(shortenedTaskName)==0?"":taskName):desc;

			if(desc.isEmpty()){
				section = toolkit.createSection(form.getBody(), Section.TREE_NODE | Section.COMPACT);
			}
			else{
				section = toolkit.createSection(form.getBody(), Section.DESCRIPTION | Section.TREE_NODE | Section.COMPACT);
				section.setDescription(desc);
			}

			section.setText((i+1)+". "+shortenedTaskName);

			FormText text = toolkit.createFormText(section, false);
			text.setText(dateString, false, false);
			text.setVisible(false);
			section.setClient(text);
		}
		timedTaskComposite.setContent(timedInnerComposite);
		timedTaskComposite.setMinHeight(timedList.size()*44);

		//		timedTaskComposite.setExpandHorizontal(false);
		//		timedTaskComposite.setExpandVertical(true);
	}

	private final static String NON_THIN = "[^iIl1\\.,']";

	private static int textWidth(String str) {
		return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
	}

	public static String ellipsize(String text, int max) {

		if (textWidth(text) <= max)
			return text;

		// Start by chopping off at the word before max
		// This is an over-approximation due to thin-characters...
		int end = text.lastIndexOf(' ', max - 3);

		// Just one long word. Chop it off.
		if (end == -1)
			return text.substring(0, max-3) + "...";

		// Step forward as long as textWidth allows.
		int newEnd = end;
		do {
			end = newEnd;
			newEnd = text.indexOf(' ', end + 1);

			// No more spaces.
			if (newEnd == -1)
				newEnd = text.length();

		} while (textWidth(text.substring(0, newEnd) + "...") < max);

		return text.substring(0, end) + "...";
	}

	private void updatefloatingTask() {
		for(int i=0;i<floatingList.size();i++){
			TableItem item = new TableItem(floatingTaskTable, 0);
			item.setText((i+1)+". "+floatingList.get(i).get(Consts.NAME).toString());
			item.setForeground(getColorWithPriority(Integer.parseInt(floatingList.get(i).get(Consts.PRIORITY).toString())));
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
		provider.reset();
		provider.stop();
		System.exit(0);
	}
}
