//@author A0097699X

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
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.KeyStroke;

import static java.awt.event.KeyEvent.*;
import logic.CommandEnum;
import logic.Consts;
import logic.LogicController;
import logic.LogicParser;
import logic.google.GoogleCal;
import model.SearchResult;
import model.Task;

import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.wb.swt.SWTResourceManager;
import org.joda.time.Minutes;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;

public class UIController {
	//logic
	LogicController logic = LogicController.getInstance();
	private LogicParser parser = new LogicParser();

	//task storage
	public ArrayList<JSONObject> timedList;
	public ArrayList<JSONObject> floatingList;

	//constants
	private final Provider provider = Provider.getCurrentProvider(false);
	private final static String NON_THIN = "[^iIl1\\.,']";
	private final int REMINDER_TIME_CHECK = 300000;
	private final int MINUTES_TO_REMIND = 60;

	//statics
	private static Boolean BLNMOUSEDOWN = false;
	private static Boolean MAC = false;
	private static int XPOS = 0;
	private static int YPOS = 0;

	//system Preferences
	private String SYSTEM_FONT = "MyriadPro-Regular";
	private int FLOATINGSCROLLSIZE = 5;
	private int TIMEDSCROLLSIZE = 40;
	private char UNDO_HOTKEY = 'z';
	private char REDO_HOTKEY = 'y';
	private char REFRESH_HOTKEY = 'r';
	private char ADD_HOTKEY = 'a';
	private char DELETE_HOTKEY = 'd';
	private char HELP_HOTKEY = '/';
	private char QUIT_HOTKEY = 'q';
	private char SYNC_HOTKEY = 's';
	private char PREFERENCES_HOTKEY = 'p';
	//initialize global variables
	private CommandEnum selectedCommand = CommandEnum.INVALID;
	private int taskNo = 1;

	//declare sub-ui components
	Display DISPLAY;
	Shell SHELL;
	Text input;
	ScrolledComposite floatingTaskComposite;
	ScrolledComposite timedTaskComposite;
	Shell authShell;
	Browser browser;
	Composite statusComposite;
	Label statusInd;
	Tray tray;
	Table floatingTaskTable;
	Composite timedInnerComposite;
	MenuItem mi;
	Timer timer;
	TimerTask tt;

	public UIController(String[] args) {
		String fileName = args.length > 0 ? args[0] : "mytext.txt";
		fileName = checkFileName(fileName);
		try {
			init(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// for testing
	public UIController(String fileName) {
		try {
			MAC = SystemUtils.IS_OS_MAC;
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
		MAC = SystemUtils.IS_OS_MAC;
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
		renderPreferencesPopup();

		renderTasks("");
		updateStatusIndicator(String.format(Consts.STRING_WELCOME, fileName));
		SHELL.open();
		startReminder();
		enableDrag();
		disposeDisplay();
	}

	private void renderPreferencesPopup() {
		final Shell PreferencesWindow = new Shell(SHELL, SWT.APPLICATION_MODAL
				| SWT.DIALOG_TRIM);
		PreferencesWindow.setText("TaskBox Preferences");
		PreferencesWindow.setSize(400, 300);

		GridLayout gridLayout = new GridLayout(5, false);
		gridLayout.verticalSpacing = 8;
		gridLayout.makeColumnsEqualWidth = true;

		PreferencesWindow.setLayout(gridLayout);
		Label label;
		GridData gridData;

		label = new Label(PreferencesWindow, SWT.CENTER);
		label.setText("Scroll Size");
		label.setBackground(DISPLAY.getSystemColor(SWT.COLOR_DARK_GRAY));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 5;
		label.setLayoutData(gridData);

		label = new Label(PreferencesWindow, SWT.NULL);
		label.setText("Floating task:");

		final Text fScroll = new Text(PreferencesWindow, SWT.SINGLE | SWT.BORDER);
		fScroll.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		fScroll.setText(Integer.toString(FLOATINGSCROLLSIZE));

		label = new Label(PreferencesWindow, SWT.NULL);

		label = new Label(PreferencesWindow, SWT.NULL);
		label.setText("Timed task:");

		final Text tScroll = new Text(PreferencesWindow, SWT.SINGLE | SWT.BORDER);
		tScroll.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		tScroll.setText(Integer.toString(TIMEDSCROLLSIZE));

		label = new Label(PreferencesWindow, SWT.CENTER);
		label.setBackground(DISPLAY.getSystemColor(SWT.COLOR_DARK_GRAY));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 5;
		label.setLayoutData(gridData);
		label.setText("Sync Priority");

		final Combo rating = new Combo(PreferencesWindow, SWT.READ_ONLY | SWT.CENTER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 5;
		rating.setLayoutData(gridData);
		rating.add("Google Calendar");
		rating.add("TaskBox");
		rating.select(0);

		// Save
		Button save = new Button(PreferencesWindow, SWT.CENTER);
		save.setText("Save");

		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.CENTER;
		save.setLayoutData(gridData);

		save.addMouseListener(new MouseListener() {
			public void mouseDown(MouseEvent e) {
				System.out.println(tScroll.getText());
				System.out.println(fScroll.getText());
				System.out.println(rating.getSelectionIndex());
			}

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		PreferencesWindow.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				PreferencesWindow.setVisible(false);
			}
		});

		mi.addListener (SWT.Selection, new Listener () {
			@Override
			public void handleEvent (Event event) {
				PreferencesWindow.setVisible(true);
				PreferencesWindow.setFocus();
			}
		});

		// add ctrl + p hotkey
		DISPLAY.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == PREFERENCES_HOTKEY)) {
					if (PreferencesWindow.isVisible()) {
						PreferencesWindow.setVisible(false);
					} else {
						PreferencesWindow.setVisible(true);
						PreferencesWindow.setFocus();
					}
				}
			}
		});
		positionWindow(PreferencesWindow);
		PreferencesWindow.open();
		PreferencesWindow.pack();
		PreferencesWindow.setVisible(false);
	}

	private void startReminder() {
		timer = new Timer();
		tt = new TimerTask() {
			@Override
			public void run() {
				if(timedList.size()>0){
					DateTime d;
					DateTimeFormatter formatter;
					DateTime now = new DateTime();
					String toCheck;
					int i = 0;
					do{
						toCheck = timedList.get(i++).get(Consts.STARTDATE).toString();
						formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
						d = formatter.parseDateTime(toCheck);
					}while(d.isBeforeNow());
					Minutes min = Minutes.minutesBetween(now, d);
					System.out.println(min.getMinutes());
					final JSONObject o = timedList.get(--i);
					if(min.getMinutes() == MINUTES_TO_REMIND){
						DISPLAY.asyncExec(new Runnable() {
							public void run() {
								showNotification(o.get(Consts.NAME).toString()+" is starting in 1 hour!","Ends: "+o.get(Consts.ENDDATE).toString()+"\n"+o.get(Consts.DESCRIPTION).toString());
							}
						});
					}
				}
			}
		};
		timer.scheduleAtFixedRate(tt, REMINDER_TIME_CHECK, REMINDER_TIME_CHECK);
	}

	private void renderDisplay() {
		DISPLAY = new Display();
		if (!MAC) {
			final HotKeyListener listener = new HotKeyListener() {
				public void onHotKey(final HotKey hotKey) {

					new Thread(new Runnable() {
						public void run() {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									if (!SHELL.isVisible()
											|| DISPLAY.getFocusControl() == null) {
										System.out.println("showing window");
										SHELL.setVisible(true);
										SHELL.setMinimized(false);
										input.setFocus();
										SHELL.forceActive();
									} else {
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
			provider.register(
					KeyStroke.getKeyStroke(VK_H, InputEvent.ALT_DOWN_MASK),
					listener);
		}
		DISPLAY.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				// undo
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == UNDO_HOTKEY)) {
					undo();
					updateTaskList();
					renderTasks("");
				}
				// quit
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == QUIT_HOTKEY)) {
					systemExit();
				}
				// sync
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == SYNC_HOTKEY)) {
					showAuthPopup();
				}
				// prepare input to add
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == ADD_HOTKEY)) {
					e.doit = false; // disable select all
					input.setText("add ");
					input.setSelection(input.getText().length());
				}
				// prepare input to delete
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == DELETE_HOTKEY)) {
					input.setText("delete ");
					input.setSelection(input.getText().length());
				}
				// notification
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == 'n')) {
					showNotification(
							"Hi There! I'm a notification widget!",
							"Today we are creating a widget that allows us to show notifications that fade in and out!");
				}
				//refresh list
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == REFRESH_HOTKEY)) {
					updateTaskList();
					renderTasks("");
				}
			}
		});

	}

	private void showNotification(String title, String text) {
		NotifierDialog.notify(title, text);
	}

	private void renderTimedTaskContainer() {
		timedTaskComposite = new ScrolledComposite(SHELL, SWT.BORDER
				| SWT.V_SCROLL);
		timedTaskComposite.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		timedTaskComposite.setBounds(10, 35, 280, 405);
		timedTaskComposite.setExpandHorizontal(true);
		timedTaskComposite.setExpandVertical(true);

		DISPLAY.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == SWT.ARROW_DOWN)) {
					Point p = timedTaskComposite.getOrigin();
					timedTaskComposite.setOrigin(0, p.y+=TIMEDSCROLLSIZE);
				}
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == SWT.ARROW_UP)) {
					Point p = timedTaskComposite.getOrigin();
					timedTaskComposite.setOrigin(0, p.y-=TIMEDSCROLLSIZE);
				}
			}
		});
	}

	private void renderStatusIndicator() {
		statusComposite = new Composite(SHELL, SWT.NONE);
		statusComposite.setBounds(10, 596, 280, 14);

		statusInd = new Label(statusComposite, SWT.NONE);
		statusInd.setFont(SWTResourceManager.getFont(SYSTEM_FONT,
				MAC ? 11 : 9, SWT.NORMAL));
		statusInd.setBounds(0, 0, 280, 14);
		statusInd.setAlignment(SWT.CENTER);
	}

	private void renderInputBox() {
		input = new Text(SHELL, SWT.BORDER);
		input.setBounds(10, 10, 258, 19);

		// delegate task and reset input
		input.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					delegateTask(input.getText());
					input.setText("");
				}
			}
		});
	}

	private void renderHelp() {
		Label helpButton = new Label(SHELL, SWT.NONE);
		helpButton.setImage(new Image(SHELL.getDisplay(),
				"resource/icon_info.gif"));
		helpButton.setBounds(274, 13, 16, 14);

		final Shell helpWindow = new Shell(SHELL, SWT.APPLICATION_MODAL
				| SWT.DIALOG_TRIM);
		helpWindow.setText("Help");
		helpWindow.setSize(600, 480);
		helpWindow.open();
		helpWindow.setVisible(false);
		helpWindow.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		final StyledText helpText = new StyledText(helpWindow, SWT.NONE);
		helpText.setText(Consts.HELP_TEXT);
		helpText.setStyleRange(new StyleRange(0, 19, null, null, SWT.BOLD));
		helpText.setStyleRange(new StyleRange(248, 8, null, null, SWT.BOLD));
		helpText.setBounds(20, 10, 560, 460);
		helpText.setEditable(false);

		helpWindow.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				helpWindow.setVisible(false);
			}
		});

		helpButton.addMouseListener(new MouseListener() {
			public void mouseDown(MouseEvent e) {
				helpWindow.setVisible(true);
			}

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		// add ctrl + ? hotkey
		DISPLAY.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == HELP_HOTKEY)) {
					if (helpWindow.isVisible()) {
						helpWindow.setVisible(false);
					} else {
						helpWindow.setVisible(true);
						helpWindow.setFocus();
					}
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
		initializeBrowser(DISPLAY, browser);
		authShell.open();
		positionWindow(authShell);
		authShell.setVisible(false);
		authShell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				authShell.setVisible(false);
			}
		});
		browser.addTitleListener(new TitleListener() {
			@Override
			public void changed(TitleEvent event) {
				if (event.title != null && event.title.length() > 0) {
					authShell.setText(event.title);
					if (event.title.contains("Success")) {
						try {
							logic.generateNewToken(event.title.substring(13));
							updateStatusIndicator(logic.syncWithGoogle());
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
		if (GoogleCal.isOnline()) {
			boolean isOkWithExistingToken = false;
			if (logic.sycnWithGoogleExistingToken()) {
				try {
					updateStatusIndicator(logic.syncWithGoogle());
					isOkWithExistingToken = true;
				} catch (IOException e) {
					System.err.println(e.getMessage());
					isOkWithExistingToken = false;
				}
			}
			if (!isOkWithExistingToken) {
				browser.setUrl(logic.getUrl());
				authShell.setVisible(true);
				authShell.setFocus();
			}
		} else {
			// logic.saveCache();
			updateStatusIndicator(Consts.STRING_USER_NOT_ONLINE);
		}
	}

	private void renderFloatingTaskContainer() {
		floatingTaskComposite = new ScrolledComposite(SHELL, SWT.BORDER
				| SWT.V_SCROLL);
		floatingTaskComposite.setBounds(10, 446, 280, 144);
		floatingTaskComposite.setExpandHorizontal(true);
		floatingTaskComposite.setExpandVertical(true);

		floatingTaskTable = new Table(floatingTaskComposite, SWT.BORDER
				| SWT.V_SCROLL);
		floatingTaskTable.setHeaderVisible(false);
		floatingTaskTable.setLinesVisible(true);

		floatingTaskComposite.setContent(floatingTaskTable);
		floatingTaskComposite.setMinSize(floatingTaskTable.computeSize(
				SWT.DEFAULT, SWT.DEFAULT));
		TableColumn taskNames = new TableColumn(floatingTaskTable, SWT.LEFT);
		taskNames.setWidth(MAC ? 276 : 255);

		DISPLAY.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (((e.stateMask & SWT.ALT) == SWT.ALT) && (e.keyCode == SWT.ARROW_DOWN)) {
					floatingTaskTable.setTopIndex(floatingTaskTable.getTopIndex() + FLOATINGSCROLLSIZE);
				}
				if (((e.stateMask & SWT.ALT) == SWT.ALT) && (e.keyCode == SWT.ARROW_UP)) {
					floatingTaskTable.setTopIndex(floatingTaskTable.getTopIndex() - FLOATINGSCROLLSIZE);
				}
			}
		});
	}

	static void initializeBrowser(final Display display, Browser browser) {
		browser.addOpenWindowListener(new OpenWindowListener() {
			@Override
			public void open(WindowEvent event) {
				if (!event.required)
					return; /* only do it if necessary */
				Shell shell = new Shell(display);
				shell.setText("Request for Permission");
				shell.setLayout(new FillLayout());
				Browser browser = new Browser(shell, SWT.NONE);
				initializeBrowser(display, browser);
				event.browser = browser;
			}
		});
		browser.addVisibilityWindowListener(new VisibilityWindowListener() {
			@Override
			public void hide(WindowEvent event) {
				Browser browser = (Browser) event.widget;
				Shell shell = browser.getShell();
				shell.setVisible(false);
			}

			@Override
			public void show(WindowEvent event) {
				Browser browser = (Browser) event.widget;
				final Shell shell = browser.getShell();
				if (event.location != null)
					shell.setLocation(event.location);
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
				Browser browser = (Browser) event.widget;
				Shell shell = browser.getShell();
				shell.close();
			}
		});
	}

	private void renderTrayIcon() {
		Image image = new Image(SHELL.getDisplay(), "resource/1box.png");
		tray = DISPLAY.getSystemTray();
		if (tray == null) {
			System.out.println("The system tray is not available");
		} else {
			final TrayItem item = new TrayItem(tray, SWT.NONE);
			item.setToolTipText("SWT TrayItem");
			item.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					SHELL.forceFocus();
					if (!SHELL.isVisible()) {
						System.out.println("showing window");
						SHELL.setVisible(true);
						SHELL.setMinimized(false);
						input.setFocus();
						SHELL.forceActive();
					} else {
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
			mi = new MenuItem (menu, SWT.PUSH);
			mi.setText ("TaskBox Preferences");
			item.addListener (SWT.MenuDetect, new Listener () {
				@Override
				public void handleEvent (Event event) {
					menu.setVisible (true);
				}
			});
			item.setImage(image);
			item.setHighlightImage(image);
		}
	}

	private void renderShell() {
		SHELL = new Shell(DISPLAY, SWT.MODELESS);
		SHELL.setSize(300, 620);
		SHELL.setLayout(null);
		// allow user to input once shell gets focus
		SHELL.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {
				input.setFocus();
			}

			public void focusLost(FocusEvent event) {
			}
		});
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
			public void mouseUp(MouseEvent arg0) {
				BLNMOUSEDOWN = false;
			}
			public void mouseDown(MouseEvent e) {
				BLNMOUSEDOWN = true;
				XPOS = e.x;
				YPOS = e.y;
			}
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});

		SHELL.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (BLNMOUSEDOWN) {
					SHELL.setLocation(SHELL.getLocation().x + (e.x - XPOS),
							SHELL.getLocation().y + (e.y - YPOS));
				}
			}
		});
	}

	// sets shell position to middle of screen
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
			String resultsDate = "";
			switch (selectedCommand) {
			case ADD:
				statusString = add(task);
				updateTaskList();
				break;
			case DISPLAY:
				updateTaskList();
				break;
			case COMPLETE:
				statusString = complete(task);
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
				resultsDate = searchTimed(task);
				searchFloating(task);
				break;
			case UPDATE:
				statusString = update(task);
				updateTaskList();
				break;
			case BLOCK:
				statusString = block(task);
				break;
			case UNDO:
				if(splittedString.length==1){
					undo();
				}
				else{
					statusString = undo(task);
				}
				updateTaskList();
				break;
			case SYNC:
				updateStatusIndicator(Consts.STRING_SYNC);
				showAuthPopup();
				break;
			case SHOW:
				statusString = complete(task);
				break;
			case EXIT:
				systemExit();
			default:
				updateStatusIndicator(Consts.STRING_NOT_SUPPORTED_COMMAND);
				break;
			}
			if (!statusString.isEmpty()) {
				updateStatusIndicator(statusString);
			}
			renderTasks(resultsDate);
		} else {
			updateStatusIndicator(Consts.STRING_NOT_SUPPORTED_COMMAND);
		}
	}

	public String block(String userInput) {
		if (userInput != null && !userInput.isEmpty()) {
			try {
				return logic.block(userInput);
			} catch (NumberFormatException e) {
				return Consts.USAGE_BLOCK;
			}
		} else {
			return Consts.USAGE_BLOCK;
		}
	}

	public String searchTimed(String keyword) {
		if (keyword != null && !keyword.isEmpty()) {
			try {
				SearchResult searchResult = logic.search(keyword, Consts.STATUS_TIMED_TASK);
				timedList = searchResult.getTasksBuffer();

				//Modify date range by searchResult.getStartDate(), searchResult.getEndDate()
				//for search command without date specification (like search with desc)
				//startDate and endDate = Consts.DATE_DEFAULT
				if(searchResult.getStartDate() != Consts.DATE_DEFAULT){
					updateStatusIndicator(Consts.STRING_SEARCH_COMPLETE);
					SimpleDateFormat osdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
					SimpleDateFormat nsdf = new SimpleDateFormat("dd/MM/yyyy");
					Date d = null;
					try {
						d = osdf.parse(searchResult.getStartDate().toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					String temp = nsdf.format(d);
					return temp;
				}
				else{
					return "";
				}
			} catch (IOException e) {
			}
		} else {
			updateStatusIndicator(Consts.STRING_NOT_FOUND);
		}
		return "";
	}

	public void searchFloating(String keyword) {
		if (keyword != null && !keyword.isEmpty()) {
			try {
				SearchResult searchResult = logic.search(keyword,
						Consts.STATUS_FLOATING_TASK);
				floatingList = searchResult.getTasksBuffer();
				updateStatusIndicator(Consts.STRING_SEARCH_COMPLETE);
			} catch (IOException e) {
			}
		} else {
			updateStatusIndicator(Consts.STRING_NOT_FOUND);
		}
	}

	public String update(String userInput) {
		if (userInput != null && !userInput.isEmpty()) {
			String[] splittedString = getSplittedString(userInput);
			if (splittedString.length != Consts.NO_ARGS_UPDATE) {
				return Consts.USAGE_UPDATE;
			}
			int lineNumber = Integer.parseInt(splittedString[0]);
			if (lineNumber > taskNo + floatingList.size() - 1 || lineNumber < 1) {
				return Consts.USAGE_UPDATE;
			}
			try {
				Task newTask = parser.decompose(splittedString[1]);
				// calculate whether task is in timed or floating
				return logic.update(lineNumber >= taskNo ? floatingList.get(lineNumber - taskNo) : timedList.get(lineNumber - 1),newTask);
			} catch (NumberFormatException e) {
				return Consts.USAGE_UPDATE;
			}
		} else {
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
			int lineNumber = Integer.parseInt(lineNo);

			if (lineNumber > taskNo + floatingList.size() - 1 || lineNumber < 1) {
				return Consts.USAGE_DELETE;
			}
			try {
				// calculate whether task is in timed or floating
				return logic.delete(lineNumber >= taskNo ? floatingList.get(lineNumber - taskNo) : timedList.get(lineNumber - 1));
			} catch (NumberFormatException e) {
				return Consts.USAGE_DELETE;
			}
		} else {
			return Consts.USAGE_DELETE;
		}
	}



	private String undo(String lineNo) {
		if (lineNo != null && !lineNo.isEmpty()) {
			int lineNumber = Integer.parseInt(lineNo);

			if (lineNumber > taskNo + floatingList.size() - 1 || lineNumber < 1) {
				return Consts.USAGE_UNDO;
			}
			try {
				// calculate whether task is in timed or floating
				return logic.complete(lineNumber >= taskNo ? floatingList.get(lineNumber - taskNo) : timedList.get(lineNumber - 1),lineNumber >= taskNo? Consts.STATUS_FLOATING_TASK : Consts.STATUS_TIMED_TASK);
			} catch (NumberFormatException e) {
				return Consts.USAGE_UNDO;
			}
		} else {
			return Consts.USAGE_UNDO;
		}
	}

	private String complete(String lineNo) {
		if (lineNo != null && !lineNo.isEmpty()) {
			int lineNumber = Integer.parseInt(lineNo);

			if (lineNumber > taskNo + floatingList.size() - 1 || lineNumber < 1) {
				return Consts.USAGE_COMPLETE;
			}
			try {
				// calculate whether task is in timed or floating
				return logic.complete(lineNumber >= taskNo ? floatingList.get(lineNumber - taskNo) : timedList.get(lineNumber - 1),lineNumber >= taskNo? Consts.STATUS_COMPLETED_FLOATING_TASK : Consts.STATUS_COMPLETED_TIMED_TASK);
			} catch (NumberFormatException e) {
				return Consts.USAGE_COMPLETE;
			}
		} else {
			return Consts.USAGE_COMPLETE;
		}
	}

	public String clear() {
		if (logic.clear()) {
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
			public int compare(JSONObject t1, JSONObject t2) {
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
			return logic.add(tsk);
		} else {
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

	private void renderTasks(String resultsDate) {
		taskNo = 1;
		if (timedInnerComposite != null) {
			timedInnerComposite.dispose();
		}
		floatingTaskTable.removeAll();
		updateTimedTask(-1,resultsDate);
		updatefloatingTask();
	}

	private void updateStatusIndicator(String str) {
		if (!SHELL.isDisposed()) {
			statusInd.setText(str);
			statusComposite.layout();
		}
	}

	private void updateTimedTask(int line, String resultsDate) {
		int noOfDays = 0;
		String currentDateString = "";
		timedInnerComposite = new Composite(timedTaskComposite, SWT.NONE);
		timedInnerComposite.setLayout(new GridLayout(1, true));
		FormToolkit toolkit = null;
		Form form = null;

		for (; taskNo < timedList.size() + 1; taskNo++) {
			JSONObject o = timedList.get(taskNo - 1);
			String start = o.get(Consts.STARTDATE).toString();
			String startTime = start.substring(11, start.length() - 3) + "hr";
			String startDate = !resultsDate.isEmpty()?resultsDate:start.substring(0, 10);
			//startDate = startDate.substring(3,6)+startDate.substring(0,3)+startDate.substring(6);
			String end = o.get(Consts.ENDDATE).toString();
			String endTime = end.substring(11, end.length() - 3) + "hr";
			String desc = o.get(Consts.DESCRIPTION).toString();
			String taskName = o.get(Consts.NAME).toString();
			int status = Integer.parseInt(o.get(Consts.STATUS).toString());
			int priority = Integer.parseInt(o.get(Consts.PRIORITY).toString());
			int frequency = Integer
					.parseInt(o.get(Consts.FREQUENCY).toString());
			String shortenedTaskName = ellipsize(taskName, 24);

			if (currentDateString.compareTo(startDate) != 0) {
				noOfDays++;
				toolkit = new FormToolkit(timedInnerComposite.getDisplay());
				currentDateString = startDate;
				form = toolkit.createForm(timedInnerComposite);
				form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				form.setText(currentDateString);
				if(!MAC){
					form.setFont(SWTResourceManager.getFont(SYSTEM_FONT,13, SWT.BOLD, false, true));
				}
				else{
					form.setFont(SWTResourceManager.getFont(SYSTEM_FONT,13, SWT.BOLD));
				}
				ColumnLayout cl = new ColumnLayout();
				cl.maxNumColumns = 1;
				form.getBody().setLayout(cl);
			}

			Section section = toolkit.createSection(form.getBody(),
					Section.COMPACT | Section.TITLE_BAR | Section.TWISTIE
					| Section.EXPANDED);
			section.setText(taskNo + ". " + shortenedTaskName);

			if(!MAC && status == Consts.STATUS_COMPLETED_TIMED_TASK){
				section.setFont(SWTResourceManager.getFont(SYSTEM_FONT,10, SWT.BOLD, true, false));
			}
			else{
				section.setFont(SWTResourceManager.getFont(SYSTEM_FONT,10, SWT.BOLD));
			}
			//section.setTitleBarBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			//section.setTitleBarBorderColor(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			if (priority == 1) {
				section.setTitleBarBorderColor(SWTResourceManager
						.getColor(SWT.COLOR_RED));
			}

			final Composite sectionClient = toolkit.createComposite(section);
			TableWrapLayout twl = new TableWrapLayout();
			twl.numColumns = 1;
			sectionClient.setLayout(twl);
			FormText text;

			// full name
			if (taskName.compareTo(shortenedTaskName) != 0) {
				text = toolkit.createFormText(sectionClient, false);
				text.setText(taskName, false, false);
			}

			// time
			text = toolkit.createFormText(sectionClient, false);
			if(start.compareTo(end) == 0){
				text.setText("Due by: " + startTime, false, false);
			}
			else if(startTime.compareTo("00:00hr") == 0
					&& endTime.compareTo("23:59hr") == 0){
				text.setText("Full Day Event", false, false);
			}
			else{
				text.setText("Start: " + startTime, false, false);
				text = toolkit.createFormText(sectionClient, false);
				text.setText("End: " + endTime, false, false);
			}

			// description
			if (!desc.isEmpty()) {
				text = toolkit.createFormText(sectionClient, false);
				text.setText(desc, false, false);
			}

			// repeats
			if (frequency > 0 && frequency < 4) {
				String freString = frequency == 1 ? "daily"
						: (frequency == 2 ? "weekly"
								: (frequency == 3 ? "monthly" : "-"));
				text = toolkit.createFormText(sectionClient, false);
				text.setText("Repeats " + freString, false, false);
			}

			section.setClient(sectionClient);
		}
		timedTaskComposite.setContent(timedInnerComposite);
		timedTaskComposite.setMinHeight(timedList.size() * 70 + noOfDays * 40);
	}

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
			return text.substring(0, max - 3) + "...";

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
		for (int i = 0; i < floatingList.size(); i++) {
			JSONObject o = floatingList.get(i);
			int status = Integer.parseInt(o.get(Consts.STATUS).toString());

			TableItem item = new TableItem(floatingTaskTable, 0);
			item.setText((i + taskNo) + ". "
					+ o.get(Consts.NAME).toString());
			item.setForeground(getColorWithPriority(Integer
					.parseInt(o.get(Consts.PRIORITY)
							.toString())));

			if(!MAC && status == Consts.STATUS_COMPLETED_FLOATING_TASK){
				item.setFont(SWTResourceManager.getFont(SYSTEM_FONT,10, SWT.NORMAL, true, false));
			}
			else{
				item.setFont(SWTResourceManager.getFont(SYSTEM_FONT,10, SWT.NORMAL));
			}
		}
	}

	public Color getColorWithPriority(int p) {
		if (p == Consts.TASK_IMPORTANT) {
			return DISPLAY.getSystemColor(SWT.COLOR_RED);
		}
		else {
			return DISPLAY.getSystemColor(SWT.COLOR_BLACK);
		}
	}

	private void systemExit() {
		logic.saveCache();
		updateStatusIndicator(Consts.STRING_EXIT);
		SHELL.dispose();
		input.dispose();
		floatingTaskComposite.dispose();
		timedTaskComposite.dispose();
		authShell.dispose();
		browser.dispose();
		statusComposite.dispose();
		statusInd.dispose();
		floatingTaskTable.dispose();
		timedInnerComposite.dispose();
		tray.dispose();
		provider.reset();
		provider.stop();
		System.exit(0);
	}
}