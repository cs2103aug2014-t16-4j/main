//@author A0112069M
package logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
//import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import model.Task;

public class LogicParser {
	private static final int TIME_EPS = 2;	
	private static final String IGNORE_LIST = "important normal the at in on from to";

	int nameSeparator;
	int taskType;
	Parser dateParser = new Parser();
	
	@SuppressWarnings("deprecation")
	private int getTimeFromDate(Date date) {
		return date.getHours() *  60 * 60 + date.getMinutes() * 60 + date.getSeconds();
	}
	
	private boolean isDefaultTime(Date date) {
		Date dateNow = new Date();
		return (Math.abs(getTimeFromDate(dateNow) - getTimeFromDate(date)) <= TIME_EPS);
	}
	
	private Date getEndOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);
	    return calendar.getTime();
	}

	private Date getStartOfDay(Date date) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	    return calendar.getTime();
	}
	
	private int decomposePriority(ArrayList<String> words) {
		for (int i = 0; i < words.size(); i++) {
			if (words.get(i).compareTo(Consts.PRIORITY_IMPORTANT) == 0) {
				//words.remove(i);
				//nameSeparator = Math.min(nameSeparator, i - 1);
				return Consts.PRIORITY_IMPORTANT_VALUE;
			}
		}
		return 0;
	}
	
	private int decomposeFrequency(ArrayList<String> words) {
		for (int i = 0; i < words.size(); i++) {
			if (words.get(i).compareTo(Consts.FREQUENCY_DAILY) == 0) {
				words.remove(i);
				nameSeparator = Math.min(nameSeparator, i - 1);
				return Consts.FREQUENCY_DAILY_VALUE;
			}
			if (words.get(i).compareTo(Consts.FREQUENCY_WEEKLY) == 0) {
				words.remove(i);
				nameSeparator = Math.min(nameSeparator, i - 1);
				return Consts.FREQUENCY_WEEKLY_VALUE;
			}
			if (words.get(i).compareTo(Consts.FREQUENCY_MONTHLY) == 0) {
				words.remove(i);
				nameSeparator = Math.min(nameSeparator, i - 1);
				return Consts.FREQUENCY_MONTHLY_VALUE;
			}
		}
		return 0;
	}
	
	private String decomposeDescription(ArrayList<String> words) {
		for (int i = 0; i < words.size(); i++) {
			String tempString = "";
			for (int j = i; j < words.size(); j++) {
				if (i == j)
					tempString = words.get(j);
				else
					tempString = tempString + " " + words.get(j); 
				if (words.get(i).length() >= 2 && words.get(i).charAt(0) == '(' && words.get(j).charAt(words.get(j).length() - 1) == ')') {
					tempString = tempString.replace("(", "");
					tempString = tempString.replace(")", "");
					for (int k = i; k <= j; k++) {
						words.remove(i);						
					}
					nameSeparator = Math.min(nameSeparator, i - 1);
					return tempString.trim();
				}
			}
		}
		return "";
	}
	
	private String decomposeOptionalName(ArrayList<String> words) {
		for (int i = 0; i < words.size(); i++) {
			String tempString = "";
			for (int j = i; j < words.size(); j++) {
				if (i == j)
					tempString = words.get(j);
				else
					tempString = tempString + " " + words.get(j); 
				if (words.get(i).length() >= 2 && words.get(i).charAt(0) == '"' && words.get(j).charAt(words.get(j).length() - 1) == '"') {
					tempString = tempString.replace("\"", "");
					tempString = tempString.replace("\"", "");
					for (int k = i; k <= j; k++) {
						words.remove(i);						
					}
					nameSeparator = Math.min(nameSeparator, i - 1);
					return tempString.trim();
				}
			}
		}
		return "";
	}
	
	private Date[] decomposeDate(ArrayList<String> words) {
		Date[] date = new Date[2];
		date[0] = new Date();
		date[1] = new Date();
		
		List<Integer> valid = new ArrayList<Integer>();
		String fullString = "";
		for (String s : words) {
			valid.add(fullString.length() + 1);
			fullString = fullString + " " + s;
		}
		List<DateGroup> dateGroupFull;
		int l = 0;
		while (true) {
			dateGroupFull = dateParser.parse(fullString.substring(l, fullString.length()));
			if (dateGroupFull.isEmpty()) {
				taskType = Consts.STATUS_FLOATING_TASK;
				return date;
			}
			l = l + dateGroupFull.get(0).getPosition() + 1;
			if (valid.contains(l)) break;
			l++;
		}
		List<Date> fullDate = dateGroupFull.get(0).getDates();

		//determine nameSeparator
		if (!words.isEmpty()) {
			//int l = dateGroupFull.get(0).getPosition();
			
			for (int i = 0; i < words.size(); i++) {
				l -= words.get(i).length() + 1;
				if (l < 0) {
					nameSeparator = Math.min(nameSeparator, i - 1);
					break;
				}
			}			
			
			if (fullDate.size() == 0) {
			} else if (fullDate.size() == 1) {
				date[0] = fullDate.get(0);
				date[1] = fullDate.get(0);
			} else if (fullDate.size() == 2) {
				date[0] = fullDate.get(0);
				date[1] = fullDate.get(1);				
			}
		}

		if (isDefaultTime(date[0])) {
			date[0] = getStartOfDay(date[0]);
		}

		if (isDefaultTime(date[1])) {
			date[1] = getEndOfDay(date[1]);
		}
		return date;	
	}

	private String decomposeName(ArrayList<String> words, int nameSeparator) {
		String result = "";
		for (int i = 0; i <= Math.min(words.size() - 1, nameSeparator); i++) {
			result = result + words.get(i) + " ";
		}
		return result.trim();
	}
	
	public Task decompose(String task)
	{		
		taskType = Consts.STATUS_TIMED_TASK;
		Task resultTask = new Task();
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(task.split(" ")));
		nameSeparator = words.size() - 1;
		String sName = decomposeOptionalName(words);
		resultTask.setDescription(decomposeDescription(words));
		resultTask.setPriority(decomposePriority(words));
		resultTask.setFrequency(decomposeFrequency(words));
		Date[] date = decomposeDate(words);
		if (date[0]!=null && date[1]!=null && date[0].compareTo(date[1]) > 0)
		{
			Date temp = date[0];
			date[0] = date[1];
			date[1] = temp;
		}
		resultTask.setStartDate(date[0]);
		resultTask.setEndDate(date[1]);
		if (sName.isEmpty()) {
			nameSeparator = Math.min(nameSeparator, words.size() - 1);
			while (nameSeparator > 0 && IGNORE_LIST.contains(words.get(nameSeparator)))
			{
				nameSeparator--;
			}
			resultTask.setName(decomposeName(words, nameSeparator));
		} else {
			resultTask.setName(sName);
		}
		resultTask.setStatus(taskType);
		return resultTask;
	}

	public LogicParser()
	{
		
	}
}
