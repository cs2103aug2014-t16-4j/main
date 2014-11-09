package logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Task;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.services.calendar.model.Event;

import edu.emory.mathcs.backport.java.util.Arrays;

// Converter class for converting different formats 
public class Converter {

	@SuppressWarnings("unchecked")
	public static JSONObject taskToJSON(Task task) {
		JSONObject jTask = new JSONObject();
		jTask.put(Consts.NAME, task.getName());
		jTask.put(Consts.DESCRIPTION, task.getDescription());
		jTask.put(Consts.STARTDATE,task.getStartDate() != null ? Consts.FORMAT_DATE.format(task.getStartDate()) : "");
		jTask.put(Consts.ENDDATE,task.getEndDate() != null ? Consts.FORMAT_DATE.format(task.getEndDate()) : "");
		jTask.put(Consts.PRIORITY, task.getPriority());
		jTask.put(Consts.FREQUENCY, task.getFrequency());
		jTask.put(Consts.STATUS, task.getStatus());
		return jTask;
	}

	public static Task jsonToTask(JSONObject obj) {
		Task temp = null;
		try {
			temp = new Task(obj.get(Consts.NAME).toString());
			temp.setDescription(obj.get(Consts.DESCRIPTION).toString());
			temp.setStartDate(Consts.FORMAT_DATE.parse(obj.get(Consts.STARTDATE).toString()));
			temp.setEndDate(Consts.FORMAT_DATE.parse(obj.get(Consts.ENDDATE).toString()));
			temp.setFrequency((int) Integer.parseInt(obj.get(Consts.FREQUENCY).toString()));
			temp.setPriority((int) Integer.parseInt(obj.get(Consts.PRIORITY).toString()));
			temp.setStatus((int) Integer.parseInt(obj.get(Consts.STATUS).toString()));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return temp;
	}

	public static String jsonListToString(List<JSONObject> objList) {
		String res = "";
		for (JSONObject obj : objList) {
			res = res + obj.toString() + '~';
		}
		return res;
	}
	
	public static List<JSONObject> stringToJSONList(String strListRaw) throws org.json.simple.parser.ParseException{
		JSONParser jsonParser = new JSONParser();
		List<String> strList = Arrays.asList(strListRaw.split("~"));
		List<JSONObject> res = new ArrayList<JSONObject>(strList.size());
		for (String objStr : strList) {
			res.add((JSONObject) jsonParser.parse(objStr));
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject eventToJSON(Event event) throws ParseException {
		JSONObject temp = new JSONObject();
		temp.put(Consts.NAME, event.getSummary());
		temp.put(Consts.DESCRIPTION, event.getDescription()!=null?event.getDescription():"");
		temp.put(Consts.STARTDATE, event.getStart()!=null?convertDate(event.getStart().getDateTime().toString()):"");
		//temp.put(Consts.STARTDATE, convertDate(event.getStart().getDateTime().toString()));
		//temp.put(Consts.ENDDATE, convertDate(event.getEnd().getDateTime().toString()));
		temp.put(Consts.ENDDATE, event.getEnd()!=null?convertDate(event.getEnd().getDateTime().toString()):"");
		temp.put(Consts.PRIORITY, event.getSequence());
		if(event.getRecurrence() == null){
			temp.put(Consts.FREQUENCY,0);
		}else{
			temp.put(Consts.FREQUENCY, parseRecurrence(event.getRecurrence().toString()));
		}
		temp.put(Consts.STATUS, 1);
		return temp;
	}

	private static String convertDate(String str) throws ParseException {
		String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";
		String datedate = str;
		Date newDate = new SimpleDateFormat(pattern).parse(datedate.split("\\+")[0]);
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/M/yyyy HH:mm:ss");
		return DATE_FORMAT.format(newDate);
	}

	private static int parseRecurrence(String str) {
		if (str.isEmpty() || str == null) {
			return 0;
		} else {
			String delims = "[:=;]+";
			String[] splittedStr = str.split(delims);
			if (splittedStr[2].equalsIgnoreCase(Consts.FREQUENCY_DAILY)) {
				return Consts.FREQUENCY_DAILY_VALUE;
			} else if (splittedStr[2].equalsIgnoreCase(Consts.FREQUENCY_WEEKLY)) {
				return Consts.FREQUENCY_WEEKLY_VALUE;
			} else {
				return Consts.FREQUENCY_MONTHLY_VALUE;
			}
		}
	}
}
