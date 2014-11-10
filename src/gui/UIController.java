//@author A0097699X

package gui;

import logic.*;
import model.*;
import gui.common.*;
import logic.google.GoogleCal;

import javax.swing.KeyStroke;

import java.awt.event.InputEvent;

import static java.awt.event.KeyEvent.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.wb.swt.SWTResourceManager;
import org.joda.time.Minutes;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.apache.commons.lang.SystemUtils;

// renders everything UI related
public class UIController {
	// logic
	LogicController logic = LogicController.getInstance();
	private LogicParser parser = new LogicParser();

	// task storage
	private ArrayList<JSONObject> timedList;
	private ArrayList<JSONObject> floatingList;
	private ArrayList<Date> resultsDate;

	// constants
	private final Provider PROVIDER = Provider.getCurrentProvider(false);
	private final static String NON_THIN = "[^iIl1\\.,']";
	private final ArrayList<Date> DEFAULT_SEARCH_DATE = null;
	private final char UNDO_HOTKEY = 'z';
	private final char REFRESH_HOTKEY = 'r';
	private final char ADD_HOTKEY = 'a';
	private final char DELETE_HOTKEY = 'd';
	private final char HELP_HOTKEY = '/';
	private final char QUIT_HOTKEY = 'q';
	private final char SYNC_HOTKEY = 's';
	private final int REMINDER_TIME_CHECK = 60000;
	private final int MINUTES_TO_REMIND = 60;
	private final int DEFAULT_JUMP_LINE = 0;
	private final int DEFAULT_EXPAND_LINE = -1;

	// statics
	private static Boolean blnMouseDown = false;
	private static Boolean isMac = false;
	private static int xPos = 0;
	private static int yPos = 0;

	// system Preferences
	private String system_font = "MyriadPro-Regular";
	private int floating_scroll_size = 5;
	private int timed_scroll_size = 40;

	// initialize global variables
	private CommandEnum selectedCommand = CommandEnum.INVALID;
	private int taskNo = 1;

	// declare sub-ui components
	Display display;
	Shell shell;
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

	public UIController() {
		String fileName = checkFileName("taskbox.txt");
		try {
			init(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// for testing
	public UIController(String fileName) {
		logic = LogicController.getInstance();
		try {
			logic.init(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// UI initializer
	private void init(String fileName) throws IOException {
		isMac = SystemUtils.IS_OS_MAC;
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

		renderTasks(DEFAULT_EXPAND_LINE, DEFAULT_SEARCH_DATE, DEFAULT_JUMP_LINE);
		updateStatusIndicator(String.format(Consts.STRING_WELCOME, fileName));
		shell.open();
		startReminder();
		enableDrag();
		disposeDisplay();
	}

	private void startReminder() {
		timer = new Timer();
		tt = new TimerTask() {
			@Override
			public void run() {
				if (timedList.size() > 0) {
					DateTime d;
					DateTimeFormatter formatter = DateTimeFormat
							.forPattern("dd/MM/yyyy HH:mm:ss");
					DateTime now = new DateTime();
					String toCheck;
					int i = 0, status = 0;

					do {
						try {
							status = Integer.parseInt(timedList.get(i)
									.get(Consts.STATUS).toString());
						} catch (Exception e) {
							status = Consts.STATUS_TIMED_TASK;
						}
						toCheck = timedList.get(i++).get(Consts.STARTDATE)
								.toString();
						d = formatter.parseDateTime(toCheck);
					} while (d.isBeforeNow() || status != 1);

					final JSONObject o = timedList.get(--i);
					Minutes min = Minutes.minutesBetween(now, d);

					if (min.getMinutes() + 1 == MINUTES_TO_REMIND) {
						display.asyncExec(new Runnable() {
							public void run() {
								showNotification(o.get(Consts.NAME).toString()
										+ " is starting in 1 hour!", "Ends: "
										+ o.get(Consts.ENDDATE).toString()
										+ "\n"
										+ o.get(Consts.DESCRIPTION).toString());
							}
						});
					}
				}
			}
		};
		timer.scheduleAtFixedRate(tt, REMINDER_TIME_CHECK, REMINDER_TIME_CHECK);
	}

	private void renderDisplay() {
		display = new Display();
		if (!isMac) {
			final HotKeyListener listener = new HotKeyListener() {
				public void onHotKey(final HotKey hotKey) {

					new Thread(new Runnable() {
						public void run() {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									if (!shell.isVisible()
											|| display.getFocusControl() == null) {
										System.out.println("showing window");
										shell.setVisible(true);
										shell.setMinimized(false);
										input.setFocus();
										shell.forceActive();
									} else {
										System.out.println("hiding window");
										shell.setMinimized(true);
										shell.setVisible(false);
									}
								}
							});
						}
					}).start();
				}
			};

			PROVIDER.reset();
			PROVIDER.register(
					KeyStroke.getKeyStroke(VK_H, InputEvent.ALT_DOWN_MASK),
					listener);
		}
		display.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				// undo
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == UNDO_HOTKEY)) {
					e.doit = false;
					undo();
					updateTaskList();
					renderTasks(DEFAULT_EXPAND_LINE, DEFAULT_SEARCH_DATE,
							DEFAULT_JUMP_LINE);
				}
				// quit
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == QUIT_HOTKEY)) {
					e.doit = false;
					systemExit();
				}
				// sync
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == SYNC_HOTKEY)) {
					e.doit = false;
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
					e.doit = false;
					input.setText("delete ");
					input.setSelection(input.getText().length());
				}
				// refresh list
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == REFRESH_HOTKEY)) {
					e.doit = false;
					updateTaskList();
					renderTasks(DEFAULT_EXPAND_LINE, DEFAULT_SEARCH_DATE,
							DEFAULT_JUMP_LINE);
				}
			}
		});

	}

	private void showNotification(String title, String text) {
		NotifierDialog.notify(title, text);
	}

	private void renderTimedTaskContainer() {
		timedTaskComposite = new ScrolledComposite(shell, SWT.BORDER
				| SWT.V_SCROLL);
		timedTaskComposite.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		timedTaskComposite.setBounds(10, 35, 280, 405);
		timedTaskComposite.setExpandHorizontal(true);
		timedTaskComposite.setExpandVertical(true);

		display.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == SWT.ARROW_DOWN)) {
					Point p = timedTaskComposite.getOrigin();
					timedTaskComposite.setOrigin(0, p.y += timed_scroll_size);
				}
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == SWT.ARROW_UP)) {
					Point p = timedTaskComposite.getOrigin();
					timedTaskComposite.setOrigin(0, p.y -= timed_scroll_size);
				}
			}
		});
	}

	private void renderStatusIndicator() {
		statusComposite = new Composite(shell, SWT.NONE);
		statusComposite.setBounds(10, 596, 280, 14);

		statusInd = new Label(statusComposite, SWT.NONE);
		statusInd.setFont(SWTResourceManager.getFont(system_font, isMac ? 11
				: 9, SWT.NORMAL));
		statusInd.setBounds(0, 0, 280, 14);
		statusInd.setAlignment(SWT.CENTER);
	}

	private void renderInputBox() {
		input = new Text(shell, SWT.BORDER);
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
		Label helpButton = new Label(shell, SWT.NONE);
		helpButton.setImage(new Image(shell.getDisplay(),
				"resource/icon_info.gif"));
		helpButton.setBounds(274, 13, 16, 14);

		final Shell helpWindow = new Shell(shell, SWT.APPLICATION_MODAL
				| SWT.DIALOG_TRIM);
		helpWindow.setText("Help");
		helpWindow.setSize(600, 480);
		helpWindow.open();
		helpWindow.setVisible(false);
		helpWindow.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		final StyledText helpText = new StyledText(helpWindow, SWT.NONE);
		helpText.setText(Consts.HELP_TEXT);
		helpText.setStyleRange(new StyleRange(0, 19, null, null, SWT.BOLD));
		helpText.setStyleRange(new StyleRange(322, 10, null, null, SWT.BOLD));
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
		display.addFilter(SWT.KeyDown, new Listener() {
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
		authShell = new Shell(display);
		authShell.setText("Request for Permission");
		authShell.setLayout(new FillLayout());
		browser = new Browser(authShell, SWT.NONE);
		initializeBrowser(display, browser);
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
		floatingTaskComposite = new ScrolledComposite(shell, SWT.BORDER
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
		taskNames.setWidth(isMac ? 276 : 255);

		display.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (((e.stateMask & SWT.ALT) == SWT.ALT)
						&& (e.keyCode == SWT.ARROW_DOWN)) {
					floatingTaskTable.setTopIndex(floatingTaskTable
							.getTopIndex() + floating_scroll_size);
				}
				if (((e.stateMask & SWT.ALT) == SWT.ALT)
						&& (e.keyCode == SWT.ARROW_UP)) {
					floatingTaskTable.setTopIndex(floatingTaskTable
							.getTopIndex() - floating_scroll_size);
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
		Image image = new Image(shell.getDisplay(), "resource/1box.png");
		tray = display.getSystemTray();
		
		if (tray == null) {
			System.out.println("The system tray is not available");
		} else {
			final TrayItem item = new TrayItem(tray, SWT.NONE);
			item.setToolTipText("SWT TrayItem");
			item.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					shell.forceFocus();
					if (!shell.isVisible()) {
						System.out.println("showing window");
						shell.setVisible(true);
						shell.setMinimized(false);
						input.setFocus();
						shell.forceActive();
					} else {
						System.out.println("hiding window");
						shell.setMinimized(true);
						shell.setVisible(false);
					}
				}
			});
			item.setImage(image);
			item.setHighlightImage(image);
		}
	}

	private void renderShell() {
		shell = new Shell(display, SWT.MODELESS);
		shell.setSize(300, 620);
		shell.setLayout(null);
		Image image = new Image(shell.getDisplay(), "resource/1box.png");
		shell.setImage(image);
		
		// allow user to input once shell gets focus
		shell.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {
				input.setFocus();
			}

			public void focusLost(FocusEvent event) {
			}
		});
		positionWindow(shell);
	}

	private void disposeDisplay() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
		systemExit();
	}

	private void enableDrag() {
		shell.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent arg0) {
				blnMouseDown = false;
			}

			public void mouseDown(MouseEvent e) {
				blnMouseDown = true;
				xPos = e.x;
				yPos = e.y;
			}

			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});

		shell.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (blnMouseDown) {
					shell.setLocation(shell.getLocation().x + (e.x - xPos),
							shell.getLocation().y + (e.y - yPos));
				}
			}
		});
	}

	// sets shell position to middle of screen
	private void positionWindow(Shell sh) {
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = sh.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		sh.setLocation(x, y);
	}

	// delegate tasks from input
	private void delegateTask(String userInput) {
		String[] splittedString;
		String task = "";
		String statusString = "";
		splittedString = getSplittedString(userInput);
		selectedCommand = getCommandType(splittedString[0]);
		
		if (splittedString.length > Consts.TASK_POSITION) {
			task = splittedString[Consts.TASK_POSITION];
		}
		if (selectedCommand != CommandEnum.INVALID) {
			int expandLine = -1, jumpLine = 0;
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
				statusString = searchTimed(task);
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
				if (splittedString.length == 1) {
					undo();
				} else {
					statusString = undo(task);
				}
				updateTaskList();
				break;
			case SYNC:
				updateStatusIndicator(Consts.STRING_SYNC);
				showAuthPopup();
				break;
			case EXPAND:
				expandLine = expand(task);
				jumpLine = timedTaskComposite.getOrigin().y;
				break;
			case QUIT:
				systemExit();
			default:
				updateStatusIndicator(Consts.STRING_NOT_SUPPORTED_COMMAND);
				break;
			}
			if (!statusString.isEmpty()) {
				updateStatusIndicator(statusString);
			}
			renderTasks(expandLine, resultsDate, jumpLine);
		} else {
			updateStatusIndicator(Consts.STRING_NOT_SUPPORTED_COMMAND);
		}
	}

	// calls and returns string value from logic's block function
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
				// Modify date range by searchResult.getStartDate(),
				// searchResult.getEndDate()
				// for search command without date specification (like search
				// with desc)
				// startDate and endDate = Consts.DATE_DEFAULT
				SearchResult searchResult = logic.search(keyword,
						Consts.STATUS_TIMED_TASK);
				timedList = searchResult.getTasksBuffer();
				resultsDate = searchResult.getDate();
				return Consts.STRING_SEARCH_COMPLETE;
			} catch (IOException e) {
			}
		} else {
			updateStatusIndicator(Consts.STRING_NOT_FOUND);
		}
		return Consts.USAGE_SEARCH;
	}

	private void searchFloating(String keyword) {
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
			if (splittedString.length == Consts.NO_ARGS_UPDATE) {
				int lineNumber;
				try {
					lineNumber = Integer.parseInt(splittedString[0]);
				} catch (Exception e) {
					return Consts.USAGE_UPDATE;
				}
				if (lineNumber < taskNo + floatingList.size() && lineNumber > 0) {
					try {
						String[] newSplitted = getSplittedString(splittedString[1]);
						// calculate whether task is in timed or floating
						return logic
								.update(lineNumber >= taskNo ? floatingList
										.get(lineNumber - taskNo) : timedList
										.get(lineNumber - 1),
										newSplitted[0],
										newSplitted.length == Consts.NO_ARGS_UPDATE ? newSplitted[1]
												: "");
					} catch (NumberFormatException e) {
						return Consts.USAGE_UPDATE;
					}
				}
			}
		}
		return Consts.USAGE_UPDATE;
	}

	private String sort() {
		try {
			logic.sort();
			return Consts.STRING_SORTED;
		} catch (Exception e) {
		}
		return null;
	}

	private String undo() {
		try {
			logic.undo();
			return Consts.STRING_UNDO;
		} catch (Exception e) {
		}
		return null;
	}

	public int expand(String lineNo) {
		if (lineNo.toLowerCase().compareTo("all") == 0) {
			return Consts.EXPAND_ALL;
		}
		if (lineNo.toLowerCase().compareTo("none") == 0) {
			return Consts.EXPAND_NONE;
		}
		if (lineNo != null && !lineNo.isEmpty()) {
			int lineNumber;
			try {
				lineNumber = Integer.parseInt(lineNo);
			} catch (Exception e) {
				return Consts.EXPAND_NONE;
			}

			if (lineNumber < taskNo + floatingList.size() && lineNumber > 0) {
				try {
					// calculate whether task is in timed or floating
					return lineNumber;
				} catch (NumberFormatException e) {
					return Consts.EXPAND_NONE;
				}
			}
		}
		return Consts.EXPAND_NONE;
	}

	public String delete(String lineNo) {
		if (lineNo != null && !lineNo.isEmpty()) {
			int lineNumber;
			try {
				lineNumber = Integer.parseInt(lineNo);
			} catch (Exception e) {
				return Consts.USAGE_DELETE;
			}

			if (lineNumber < taskNo + floatingList.size() && lineNumber > 0) {
				try {
					// calculate whether task is in timed or floating
					return logic.delete(lineNumber >= taskNo ? floatingList
							.get(lineNumber - taskNo) : timedList
							.get(lineNumber - 1));
				} catch (NumberFormatException e) {
					return Consts.USAGE_DELETE;
				}
			}
		}
		return Consts.USAGE_DELETE;
	}

	public String undo(String lineNo) {
		if (lineNo != null && !lineNo.isEmpty()) {
			int lineNumber;
			try {
				lineNumber = Integer.parseInt(lineNo);
			} catch (Exception e) {
				return Consts.USAGE_UNDO;
			}

			if (lineNumber < taskNo + floatingList.size() && lineNumber > 0) {
				try {
					// calculate whether task is in timed or floating
					return logic.complete(
							lineNumber >= taskNo ? floatingList.get(lineNumber
									- taskNo) : timedList.get(lineNumber - 1),
							lineNumber >= taskNo ? Consts.STATUS_FLOATING_TASK
									: Consts.STATUS_TIMED_TASK);
				} catch (NumberFormatException e) {
					return Consts.USAGE_UNDO;
				}
			}
		}
		return Consts.USAGE_UNDO;
	}

	public String complete(String lineNo) {
		if (lineNo != null && !lineNo.isEmpty()) {
			int lineNumber;
			try {
				lineNumber = Integer.parseInt(lineNo);
			} catch (Exception e) {
				return Consts.USAGE_COMPLETE;
			}

			if (lineNumber < taskNo + floatingList.size() && lineNumber > 0) {
				try {
					// calculate whether task is in timed or floating
					return logic
							.complete(
									lineNumber >= taskNo ? floatingList
											.get(lineNumber - taskNo)
											: timedList.get(lineNumber - 1),
									lineNumber >= taskNo ? Consts.STATUS_COMPLETED_FLOATING_TASK
											: Consts.STATUS_COMPLETED_TIMED_TASK);
				} catch (NumberFormatException e) {
					return Consts.USAGE_COMPLETE;
				}
			}
		}
		return Consts.USAGE_COMPLETE;

	}

	private String clear() {
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

	private String add(String task) {
		Task tsk = parser.decompose(task);
		if (tsk != null && !tsk.isEmpty()) {
			return logic.add(tsk);
		} else {
			return Consts.USAGE_ADD;
		}
	}

	public String[] getSplittedString(String userInput) {
		String[] splittedString = userInput.split(" ", 2);
		return splittedString;
	}

	public static String checkFileName(String fileName) {
		String[] parts = fileName.split("\\.(?=[^\\.]+$)");
		if (parts.length != Consts.FILE_VALID_LENGTH
				|| !parts[Consts.FILE_TYPE_POSITION].equalsIgnoreCase("txt")) {
			fileName = fileName + ".txt";
		}
		return fileName;
	}

	public CommandEnum getCommandType(String firstWord) {
		if (firstWord != null) {
			for (CommandEnum cmd : CommandEnum.values()) {
				if (firstWord.equalsIgnoreCase(cmd.toString())) {
					return cmd;
				}
			}
		}
		return CommandEnum.INVALID;
	}

	private void renderTasks(int expandLine, ArrayList<Date> resultsDate,
			int jumpLine) {
		taskNo = 1;
		if (timedInnerComposite != null) {
			timedInnerComposite.dispose();
		}
		floatingTaskTable.removeAll();
		updateTimedTask(expandLine, resultsDate, jumpLine);
		updatefloatingTask();
	}

	private void updateStatusIndicator(String str) {
		if (!shell.isDisposed()) {
			statusInd.setText(str);
			statusComposite.layout();
		}
	}

	private void updateTimedTask(int line, ArrayList<Date> resultsDate,
			int jumpLine) {
		int noOfDays = 0;
		String currentDateString = "";
		timedInnerComposite = new Composite(timedTaskComposite, SWT.NONE);
		timedInnerComposite.setLayout(new GridLayout(1, true));
		FormToolkit toolkit = null;
		Form form = null;

		for (; taskNo < timedList.size() + 1; taskNo++) {
			JSONObject o = timedList.get(taskNo - 1);
	
			String start;
			if (resultsDate != null && resultsDate.size() > 0) {
				start = Consts.FORMAT_PRINT_DATE.format(resultsDate
						.get(taskNo - 1));
			} else {
				start = o.get(Consts.STARTDATE).toString();
			}
			
			String startTime = start.substring(11, start.length() - 3) + "hr";
			String startDate = start.substring(0, 10);
			String end = o.get(Consts.ENDDATE).toString();
			String endTime = end.substring(11, end.length() - 3) + "hr";
			String endDate = end.substring(0, 10);
			String desc = o.get(Consts.DESCRIPTION).toString();
			String taskName = o.get(Consts.NAME).toString();
			int status;
			int priority;
			int frequency;
			try {
				status = Integer.parseInt(o.get(Consts.STATUS).toString());
				priority = Integer.parseInt(o.get(Consts.PRIORITY).toString());
				frequency = Integer
						.parseInt(o.get(Consts.FREQUENCY).toString());
			} catch (Exception e) {
				status = 0;
				priority = 0;
				frequency = 0;
			}
			String shortenedTaskName = ellipsize(taskName, 35);

			if (currentDateString.compareTo(startDate) != 0) {
				noOfDays++;
				toolkit = new FormToolkit(timedInnerComposite.getDisplay());
				currentDateString = startDate;
				form = toolkit.createForm(timedInnerComposite);
				form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				form.setText(currentDateString);
				
				if (!isMac) {
					form.setFont(SWTResourceManager.getFont(system_font, 13,
							SWT.BOLD, false, true));
				} else {
					form.setFont(SWTResourceManager.getFont(system_font, 13,
							SWT.BOLD));
				}
				
				ColumnLayout cl = new ColumnLayout();
				cl.maxNumColumns = 1;
				form.getBody().setLayout(cl);
			}
			Section section;
			
			if (taskNo == line || line == -9) {
				section = toolkit.createSection(form.getBody(), Section.COMPACT
						| Section.TITLE_BAR | Section.TWISTIE
						| Section.EXPANDED);
			} else {
				section = toolkit.createSection(form.getBody(), Section.COMPACT
						| Section.TITLE_BAR | Section.TWISTIE);
			}

			section.setText(taskNo + ". " + shortenedTaskName);

			if (!isMac && status == Consts.STATUS_COMPLETED_TIMED_TASK) {
				section.setFont(SWTResourceManager.getFont(system_font, 10,
						SWT.BOLD, true, false));
			} else {
				section.setFont(SWTResourceManager.getFont(system_font, 10,
						SWT.BOLD));
			}
			// section.setTitleBarBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			// section.setTitleBarBorderColor(SWTResourceManager.getColor(SWT.COLOR_WHITE));

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
			if (start.compareTo(end) == 0) {
				text.setText("Due by: " + startDate + " " + startTime, false,
						false);
			} else if ((startTime.compareTo("00:00hr") == 0 && endTime
					.compareTo("23:59hr") == 0)
					&& (startDate.compareTo(endDate) == 0)) {
				text.setText("Full Day Event", false, false);
			} else {
				text.setText("Start: " + startDate + " " + startTime, false,
						false);
				text = toolkit.createFormText(sectionClient, false);
				text.setText("End: " + endDate + " " + endTime, false, false);
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
		timedTaskComposite.setMinHeight(timedList.size()
				* (line == -9 ? 70 : 40) + noOfDays * (line == -9 ? 40 : 30));
		timedTaskComposite.setOrigin(0, jumpLine);
		
		if (resultsDate != null) {
			resultsDate.clear();
		}
	}

	private int textWidth(String str) {
		return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
	}

	public String ellipsize(String text, int max) {
		if (textWidth(text) <= max)
			return text;
		if(max < 3){
			return "";
		}
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
			int status;
			
			try {
				status = Integer.parseInt(o.get(Consts.STATUS).toString());
			} catch (Exception e) {
				status = Consts.STATUS_FLOATING_TASK;
			}

			TableItem item = new TableItem(floatingTaskTable, 0);
			item.setText((i + taskNo) + ". " + o.get(Consts.NAME).toString());
			
			try {
				item.setForeground(getColorWithPriority(Integer.parseInt(o.get(
						Consts.PRIORITY).toString())));
			} catch (Exception e) {
				item.setForeground(getColorWithPriority(0));
			}
			
			if (!isMac && status == Consts.STATUS_COMPLETED_FLOATING_TASK) {
				item.setFont(SWTResourceManager.getFont(system_font, 10,
						SWT.NORMAL, true, false));
			} else {
				item.setFont(SWTResourceManager.getFont(system_font, 10,
						SWT.NORMAL));
			}
		}
	}

	public Color getColorWithPriority(int p) {
		if (p == Consts.TASK_IMPORTANT) {
			return display.getSystemColor(SWT.COLOR_RED);
		} else {
			return display.getSystemColor(SWT.COLOR_BLACK);
		}
	}

	private void systemExit() {
		logic.saveCache();
		updateStatusIndicator(Consts.STRING_EXIT);
		shell.dispose();
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
		PROVIDER.reset();
		PROVIDER.stop();
		System.exit(0);
	}
}