package logic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// Constant Class 
public final class Consts {
	//JSON KEY STRINGS
	public static final String NAME = "Name";
	public static final String DESCRIPTION = "Description";
	public static final String PRIORITY = "Priority";
	public static final String FREQUENCY = "Frequency";
	public static final String STARTDATE = "StartDate";
	public static final String ENDDATE = "EndDate";
	public static final String STATUS = "Status";

	// GOOGLE STATUS CONSTANTS
	public static final String CONFIRMED = "confirmed";

	// CACHE FILE KEY CONSTANTS
	public static final String ADD = "ADD";
	public static final String DELETE = "DELETE";

	// FILENAME CONSTANTS
	public static final String GOOGLETOKEN = "GoogleToken";
	public static final String CACHE = "Cache";

	// FEEDBACK STRINGS
	public static final String STRING_WELCOME = "Welcome to TaskBox. %1$s is ready for use.\n";
	public static final String STRING_HELP = "Usage: TextBuddy <FILENAME.txt>\nSupported commands -> add , display , delete , update , clear , sort , search , block, undo, exit, sync, show";
	public static final String STRING_NOT_SUPPORTED_COMMAND = "Command is not supported";
	public static final String STRING_ENTER_COMMAND = "Command: ";
	public static final String STRING_ADD = "Added %s";
	public static final String STRING_COMPLETE = "Completed %s";
	public static final String STRING_COMPLETE_FAIL = "Complete fails";
	public static final String STRING_CLEAR = "All content deleted from %1$s";
	public static final String STRING_SEARCH_COMPLETE = "Search completed";
	public static final String STRING_NOT_FOUND = "No item found.";
	public static final String STRING_FOUND = "%d item(s) found.";
	public static final String STRING_SORTED = "List Sorted";
	public static final String STRING_UNDO = "Action Undone";
	public static final String STRING_EXIT = "Bye!";
	public static final String STRING_DELETE = "Deleted from %s: \"%s\"";
	public static final String STRING_UPDATE = "%s is updated.\n";
	public static final String STRING_UPDATE_CORRECT = "Use \"name\" or \"desc\" or \"date\" or \"important\" or \"not important\"";
	public static final String STRING_NOT_UPDATE = "%s is not updated.\n";
	public static final String STRING_CRE_CORRECT = "The credentials are correct.";
	public static final String STRING_CRE_NOT_CORRECT = "The credentials are not correct.";
	public static final String STRING_SYNC_COMPLETE = "Synchronization is complete";
	public static final String STRING_SYNC_NOT_COMPLETE = "Synchronization is not complete";
	public static final String STRING_SYNC = "TaskBox is synchronizing";
	public static final String STRING_USER_NOT_ONLINE = "Please check your internet connection.";
	public static final String STRING_BLOCK = "Blocked %s -> %s";
	public static final String STRING_BLOCK_FAIL = "Block fail";
	

	// DATE FORMAT
	public static final DateFormat FORMAT_DATE = new SimpleDateFormat("dd/M/yyyy HH:mm:ss");
	public static final DateFormat FORMAT_COMPARE_DATE = new SimpleDateFormat("yyyyMMdd");
	public static final DateFormat FORMAT_PRINT_DATE = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	public static final DateFormat FORMAT_PRINT_TIME = new SimpleDateFormat("HH:mm:ss");


	// ERRORS
	public static final String ERROR_ADD = "Task cannot be blank.";
	public static final String ERROR_ADD_BLOCK = "The time frame is blocked.";
	public static final String ERROR_UNKNOWN = "Unknown error occured!";

	// USAGE
	public static final String USAGE_ADD = "Usage: add <todo>";
	public static final String USAGE_UNDO = "Usage: undo <task #>";
	public static final String USAGE_COMPLETE = "Usage: complete <task #>";
	public static final String USAGE_DELETE = "Usage: delete <task #>";
	public static final String USAGE_UPDATE = "Usage: update <task #> <updated string>";
	public static final String USAGE_BLOCK = "Usage: block <start date/time> <end date/time>";

	// NUMBER CONSTANT
	public static final int INPUT_REQUIREMENT = 1;
	public static final int TASK_POSITION = 1;
	public static final int NO_ARGS_UPDATE = 2;
	public static final int FILE_TYPE_POSITION = 1;
	public static final int FILE_VALID_LENGTH = 2;
	public static final int RENDER_STATUS_INDICATOR = 0;
	public static final int TASK_IMPORTANT = 1;
	public static final int TASK_NORMAL = 0;

	// FREQUENCY CONSTANT
	public static final String FREQUENCY_DAILY = "daily";
	public static final String FREQUENCY_WEEKLY = "weekly";
	public static final String FREQUENCY_MONTHLY = "monthly";
	public static final int FREQUENCY_DAILY_VALUE = 1;
	public static final int FREQUENCY_WEEKLY_VALUE = 2;
	public static final int FREQUENCY_MONTHLY_VALUE = 3;
	
	// UPDATE CONSTANT
	public static final String UPDATE_NAME = "name";
	public static final String UPDATE_DATE = "date";
	public static final String UPDATE_ERROR_DATE = "Please type a correct date";
	public static final String UPDATE_DESC = "desc";

	// PRIORITY CONSTANT
	public static final String PRIORITY_IMPORTANT = "important";
	public static final String PRIORITY_NOT_IMPORTANT = "not important";
	public static final int PRIORITY_IMPORTANT_VALUE = 1;
	public static final int PRIORITY_NOT_IMPORTANT_VALUE = 0;

	// STATUS CONSTANT (Task type)
	public static final int STATUS_TIMED_TASK = 1;
	public static final int STATUS_FLOATING_TASK = 2;
	public static final int STATUS_BLOCK_TASK = 3;
	public static final int STATUS_COMPLETED_TIMED_TASK = 11;
	public static final int STATUS_COMPLETED_FLOATING_TASK = 12;

	// HELP TEXT
	public static final String HELP_TEXT = 
			"TaskBox Commands:\n"
			+ "\nadd [task title] ([task description]) [task date & time] [task priority] [repeat frequency]"
			+ "\nupdate [line #][name/start/end/description] [task name/start/end/description]"
			+ "\nblock [task start and end date & time]"
			+ "\nsearch [task date & time]/[keyword]"
			+ "\ndelete [line #]"
			+ "\nexpand [line #]"
			+ "\nclear"
			+ "\nsort"
			+ "\nundo"
			+ "\nsync"
			+ "\nquit"
			+ "\n\nHotkeys:\n"
			+ "\nAlt + up/down arrow: Scroll floating tasks"
			+ "\nCtrl + up/down arrow: Scroll timed tasks"
			+ "\nAlt + h: Hide/Show TaskBox"
			+ "\nCtrl + /: Help"
			+ "\nCtrl + z: Undo"
			+ "\nCtrl + a: Quick Add"
			+ "\nCtrl + d: Quick Delete"
			+ "\nCtrl + r: Refresh Tasks"
			+ "\nCtrl + p: TaskBox Preferences"
			+ "\nCtrl + s: Sync" 
			+ "\nCtrl + q: Quit";
	
	public static final Date DATE_DEFAULT = new Date(1,1,1);
}
