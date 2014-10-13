package logic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public final class Consts {
	//Json key strings
	public static final String NAME = "Name";
    public static final String DESCRIPTION = "Description";
    public static final String PRIORITY = "Priority";
    public static final String FREQUENCY = "Frequency";
    public static final String STARTDATE = "StartDate";
	public static final String ENDDATE = "EndDate";

    // FEEDBACK STRINGS
    public static final String STRING_WELCOME = "Welcome to TaskBox. %1$s is ready for use.\n";
    public static final String STRING_HELP = "Usage: TextBuddy <FILENAME.txt>\nSupported commands -> add , display , delete , update , clear , sort , search , block, undo, exit";
    public static final String STRING_NOT_SUPPORTED_COMMAND = "Command is not supported";
    public static final String STRING_ENTER_COMMAND = "Command: ";
    public static final String STRING_ADD = "added to %1$s: \"%2$s\"";
    public static final String STRING_CLEAR = "All content deleted from %1$s";
    public static final String STRING_FOUND_LINE = "=== Found line ===";
    public static final String STRING_NOT_FOUND = "No item found.";
    public static final String STRING_FOUND = "%d item(s) found.";
    public static final String STRING_SORTED = "List Sorted";
    public static final String STRING_UNDO = "Action Undone";
    public static final String STRING_EXIT = "Bye!";
    public static final String STRING_DELETE = "deleted from %s: \"%s\"";
    public static final String STRING_UPDATE = "%s is updated.\n";
	public static final String STRING_NOT_UPDATE = "%s is not updated.\n";
    public static final DateFormat formatter = new SimpleDateFormat("dd/M/yyyy HH:mm:ss");
	public static final SimpleDateFormat cmpFormatter = new SimpleDateFormat("yyyyMMdd");
    
    // ERRORS
    public static final String ERROR_ADD = "Task cannot be blank.";
    public static final String ERROR_UNKNOWN = "Unknown error occured!";

    // USAGE
    public static final String USAGE_ADD = "Usage: add <todo>";
    public static final String USAGE_DELETE = "Usage: delete <lineno>";
    public static final String USAGE_UPDATE = "Usage: update <lineno> <updated string>";
    public static final String USAGE_BLOCK = "Usage: block <start date/time> <end date/time>";

    // NUMBER CONSTANT
    public static final int INPUT_REQUIREMENT = 1;
    public static final int TASK_POSITION = 1;
    public static final int NO_ARGS_UPDATE = 2;
    public static final int FILE_TYPE_POSITION = 1;
    public static final int FILE_VALID_LENGTH = 2;
    public static final int RENDER_STATUS_INDICATOR = 0;
    public static final int RENDER_DAY = 1;
    public static final int RENDER_SOMEDAY = 2;
    public static final int RENDER_BOTH = 3;
}
