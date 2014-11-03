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

import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.apache.commons.lang.SystemUtils;
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
import org.eclipse.wb.swt.SWTResourceManager;
import org.json.simple.JSONObject;

public class UIController {

	public ArrayList<JSONObject> timedList;
	public ArrayList<JSONObject> floatingList;
	public static Boolean ISMAC = false;

	private final static String NON_THIN = "[^iIl1\\.,']";
	private static Boolean BLNMOUSEDOWN = false;
	private static int XPOS = 0;
	private static int YPOS = 0;
	private LogicParser parser = new LogicParser();
	private CommandEnum selectedCommand = CommandEnum.INVALID;
	private int taskNo = 1;

	final Provider provider = Provider.getCurrentProvider(false);

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
		updateStatusIndicator(String.format(Consts.STRING_WELCOME, fileName));
		SHELL.open();
		enableDrag();
		disposeDisplay();
	}

	private void renderDisplay() {
		DISPLAY = new Display();
		if (!ISMAC) {
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
						&& (e.keyCode == 'z')) {
					undo();
					updateTaskList();
					renderTasks();
				}
				// quit
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == 'q')) {
					systemExit();
				}
				// sync
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == 's')) {
					showAuthPopup();
				}
				// prepare input to add
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == 'a')) {
					e.doit = false; // disable select all
					input.setText("add ");
					input.setSelection(input.getText().length());
				}
				// prepare input to delete
				else if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (e.keyCode == 'd')) {
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
			}
		});

	}

	private void showNotification(String title, String text) {
		NotifierDialog.notify(title, text);
	}

	private void renderTimedTaskContainer() {
		timedTaskComposite = new ScrolledComposite(SHELL, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		timedTaskComposite.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		timedTaskComposite.setBounds(10, 35, 280, 405);
		timedTaskComposite.setExpandHorizontal(true);
		timedTaskComposite.setExpandVertical(true);
	}

	private void renderStatusIndicator() {
		statusComposite = new Composite(SHELL, SWT.NONE);
		statusComposite.setBounds(10, 596, 280, 14);

		statusInd = new Label(statusComposite, SWT.NONE);
		statusInd.setFont(SWTResourceManager.getFont("Lucida Grande",
				ISMAC ? 10 : 8, SWT.NORMAL));
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
		helpWindow.setSize(600, 410);
		helpWindow.open();
		helpWindow.setVisible(false);
		helpWindow.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		final StyledText helpText = new StyledText(helpWindow, SWT.NONE);
		helpText.setText("TaskBox Commands:\n\nadd [task title] ([task description]) [task date && time] [task priority] [repeat frequency]"
				+ "\ndelete [line #]"
				+ "\nupdate [line #]"
				+ "\nclear"
				+ "\nsort"
				+ "\nsearch [task date && time]/[keyword]"
				+ "\nblock [task start and end date && time]"
				+ "\nundo"
				+ "\nsync"
				+ "\nexit"
				+ "\n\nHotkeys:"
				+ "\n\nShift + h: Hide/Show TaskBox"
				+ "\nCtrl + /: Help"
				+ "\nCtrl + z: undo"
				+ "\nCtrl + y: redo"
				+ "\nCtrl + a: quick add"
				+ "\nCtrl + d: quick delete"
				+ "\nCtrl + s: sync" + "\nCtrl + q: quit");
		helpText.setStyleRange(new StyleRange(0, 19, null, null, SWT.BOLD));
		helpText.setStyleRange(new StyleRange(248, 8, null, null, SWT.BOLD));
		helpText.setBounds(20, 10, 560, 380);
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
						&& (e.keyCode == '/')) {
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
					e.printStackTrace();
					isOkWithExistingToken = false;
				}
			}
			if (!isOkWithExistingToken) {
				browser.setUrl(logic.getUrl());
				authShell.setVisible(true);
			}
		} else {
			updateStatusIndicator(Consts.STRING_USER_NOT_ONLINE);
		}
	}

	private void renderFloatingTaskContainer() {
		floatingTaskComposite = new ScrolledComposite(SHELL, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		floatingTaskComposite.setBounds(10, 446, 280, 144);
		floatingTaskComposite.setExpandHorizontal(true);
		floatingTaskComposite.setExpandVertical(true);

		floatingTaskTable = new Table(floatingTaskComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		floatingTaskTable.setHeaderVisible(false);
		floatingTaskTable.setLinesVisible(true);

		floatingTaskComposite.setContent(floatingTaskTable);
		floatingTaskComposite.setMinSize(floatingTaskTable.computeSize(
				SWT.DEFAULT, SWT.DEFAULT));
		TableColumn taskNames = new TableColumn(floatingTaskTable, SWT.LEFT);
		taskNames.setWidth(ISMAC ? 276 : 271);
	}

	static void initialize(final Display display, Browser browser) {
		browser.addOpenWindowListener(new OpenWindowListener() {
			@Override
			public void open(WindowEvent event) {
				if (!event.required)
					return; /* only do it if necessary */
				Shell shell = new Shell(display);
				shell.setText("Request for Permission");
				shell.setLayout(new FillLayout());
				Browser browser = new Browser(shell, SWT.NONE);
				initialize(display, browser);
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
			// item.addListener (SWT.DefaultSelection, new Listener () {
			// @Override
			// public void handleEvent (Event event) {
			// System.out.println("default selection");
			// }
			// });
			// final Menu menu = new Menu (SHELL, SWT.POP_UP);
			// MenuItem mi = new MenuItem (menu, SWT.PUSH);
			// mi.setText ("Item");
			// mi.addListener (SWT.Selection, new Listener () {
			// @Override
			// public void handleEvent (Event event) {
			// System.out.println("selection " + event.widget);
			// }
			// });
			// item.addListener (SWT.MenuDetect, new Listener () {
			// @Override
			// public void handleEvent (Event event) {
			// menu.setVisible (true);
			// }
			// });
			item.setImage(image);
			item.setHighlightImage(image);
		}
	}

	private void renderShell() {
		SHELL = new Shell(DISPLAY, SWT.MODELESS);
		// SHELL = new Shell (DISPLAY, SWT.ON_TOP | SWT.MODELESS);
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
				searchTimed(task);
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
			if (!statusString.isEmpty()) {
				updateStatusIndicator(statusString);
			}
			renderTasks();
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

	public void searchTimed(String keyword) {
		if (keyword != null && !keyword.isEmpty()) {
			try {
				timedList = logic.search(keyword, Consts.STATUS_TIMED_TASK);
				if (timedList.isEmpty()) {
					updateStatusIndicator(Consts.STRING_NOT_FOUND);
				} else {
					updateStatusIndicator(String.format(Consts.STRING_FOUND,
							timedList.size()));
				}
			} catch (IOException e) {
			}
		} else {
			updateStatusIndicator(Consts.STRING_NOT_FOUND);
		}
	}

	public void searchFloating(String keyword) {
		if (keyword != null && !keyword.isEmpty()) {
			try {
				floatingList = logic.search(keyword,
						Consts.STATUS_FLOATING_TASK);
				if (floatingList.isEmpty()) {
					updateStatusIndicator(Consts.STRING_NOT_FOUND);
				} else {
					updateStatusIndicator(String.format(Consts.STRING_FOUND,
							floatingList.size()));
				}
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
			if (lineNumber > taskNo + floatingList.size()) {
				return Consts.USAGE_UPDATE;
			}
			try {
				Task newTask = parser.decompose(splittedString[1]);
				// calculate whether task is in timed or floating
				return logic.update(
						lineNumber >= taskNo ? floatingList.get(lineNumber
								- taskNo) : timedList.get(lineNumber - 1),
						newTask);
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
			;
			if (lineNumber > taskNo + floatingList.size()) {
				return Consts.USAGE_DELETE;
			}
			try {
				// calculate whether task is in timed or floating
				return logic.delete(lineNumber >= taskNo ? floatingList
						.get(lineNumber - taskNo) : timedList
						.get(lineNumber - 1));
			} catch (NumberFormatException e) {
				return Consts.USAGE_DELETE;
			}
		} else {
			return Consts.USAGE_DELETE;
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
		// need to check through
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
			/*
			 * boolean isSuccess = logic.add(tsk); if (isSuccess) { return
			 * String.format(Consts.STRING_ADD, logic.getFileName(), task); }
			 * else { return Consts.USAGE_ADD; }
			 */
			return logic.add(tsk);
		} else {
			// return Consts.ERROR_ADD;
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

	private void renderTasks() {
		taskNo = 1;
		if (timedInnerComposite != null) {
			timedInnerComposite.dispose();
		}
		floatingTaskTable.removeAll();
		updateTimedTask();
		updatefloatingTask();
	}

	private void updateStatusIndicator(String str) {
		if (!SHELL.isDisposed()) {
			statusInd.setText(str);
			statusComposite.layout();
		}
	}

	private void updateTimedTask() {
		int noOfDays = 0;
		String currentDateString = "";
		timedInnerComposite = new Composite(timedTaskComposite, SWT.NONE);
		timedInnerComposite.setLayout(new GridLayout(1, true));
		FormToolkit toolkit = null;
		Form form = null;

		for (; taskNo < timedList.size() + 1; taskNo++) {
			JSONObject o = timedList.get(taskNo - 1);
			String start = o.get(Consts.STARTDATE).toString();
			String startTime = start.substring(11, start.length() - 3) + " hr";
			String startDate = start.substring(0, 10);
			String end = o.get(Consts.ENDDATE).toString();
			String endTime = end.substring(11, end.length() - 3) + " hr";
			String desc = o.get(Consts.DESCRIPTION).toString();
			String taskName = o.get(Consts.NAME).toString();
			int priority = Integer.parseInt(o.get(Consts.PRIORITY).toString());
			int frequency = Integer
					.parseInt(o.get(Consts.FREQUENCY).toString());
			String shortenedTaskName = ellipsize(taskName, 28);
			String dateString = start.compareTo(end) == 0 ? startTime
					: (startTime.compareTo("00:00 hr") == 0
							&& endTime.compareTo("23:59 hr") == 0 ? "Full Day Event"
							: startTime + " to " + endTime);

			if (currentDateString.compareTo(startDate) != 0) {
				noOfDays++;
				toolkit = new FormToolkit(timedInnerComposite.getDisplay());
				currentDateString = startDate;
				form = toolkit.createForm(timedInnerComposite);
				form.setLayoutData(new GridData(GridData.FILL_BOTH));
				form.setText(currentDateString);
				ColumnLayout cl = new ColumnLayout();
				cl.maxNumColumns = 1;
				TableWrapLayout twl = new TableWrapLayout();
				twl.numColumns = 1;
				form.getBody().setLayout(cl);
			}

			final Section section = toolkit.createSection(form.getBody(),
					Section.TREE_NODE | Section.COMPACT | Section.TITLE_BAR);

			section.setText(taskNo + ". " + shortenedTaskName);
			// section.setTitleBarBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			// section.setTitleBarBorderColor(SWTResourceManager.getColor(SWT.COLOR_WHITE));

			if (priority == 1) {
				// section.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				// section.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
				// section.setBackground(new Color(DISPLAY, 255,165,0));
				section.setTitleBarBorderColor(SWTResourceManager
						.getColor(SWT.COLOR_RED));
			}

			final Composite sectionClient = toolkit.createComposite(section);
			TableWrapLayout twl = new TableWrapLayout();
			twl.numColumns = 1;
			sectionClient.setLayout(twl);
			TableWrapData td = new TableWrapData();
			td.colspan = 1;
			sectionClient.setLayoutData(td);
			FormText text;

			// full name
			if (taskName.compareTo(shortenedTaskName) != 0) {
				text = toolkit.createFormText(sectionClient, false);
				text.setText(taskName, false, false);
			}

			// time
			text = toolkit.createFormText(sectionClient, false);
			text.setText(dateString, false, false);
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
			section.addControlListener(new ControlAdapter() {
				public void controlMoved(ControlEvent event) {
					section.setExpanded(false);
				}
			});
			section.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanged(ExpansionEvent e) {
					section.layout();
					sectionClient.layout();
				}
			});
		}
		timedTaskComposite.setContent(timedInnerComposite);
		timedTaskComposite.setMinHeight(timedList.size() * 35 + noOfDays * 40);
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
			TableItem item = new TableItem(floatingTaskTable, 0);
			item.setText((i + taskNo) + ". "
					+ floatingList.get(i).get(Consts.NAME).toString());
			item.setForeground(getColorWithPriority(Integer
					.parseInt(floatingList.get(i).get(Consts.PRIORITY)
							.toString())));
		}
	}

	public Color getColorWithPriority(int p) {
		if (p == Consts.TASK_IMPORTANT) {
			return DISPLAY.getSystemColor(SWT.COLOR_RED);
		}
		// else if(p==Consts.TASK_NORMAL) {
		// return new Color(DISPLAY, 255,165,0);
		// }
		else {
			// return new Color(display, 204,204,204);
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