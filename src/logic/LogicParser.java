package logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import model.Task;

public class LogicParser {

	// PRIORITY CONSTANT
	private static final String PRIORITY_IMPORTANT = "important";
	private static final String PRIORITY_NORMAL = "normal";
	private static final String PRIORITY_CUSTOM = "priority";
	private static final int PRIORITY_IMPORTANT_VALUE = 1;
	private static final int PRIORITY_NORMAL_VALUE = 2;
	
	// FREQUENCY CONSTANT
	private static final String FREQUENCY_DAILY = "daily";
	private static final String FREQUENCY_WEEKLY = "weekly";
	private static final String FREQUENCY_MONTHLY = "monthly";
	private static final int FREQUENCY_DAILY_VALUE = 1;
	private static final int FREQUENCY_WEEKLY_VALUE = 2;
	private static final int FREQUENCY_MONTHLY_VALUE = 3;
	private static final String FREQUENCY_CUSTOM = "frequency";
	
	private static final String DATE_PREPOSITION = "the at in on from to";
	int nameSeparator;
	Parser dateParser = new Parser();
	
	public int decomposePriority(ArrayList<String> words) {
		for (int i = 0; i < words.size(); i++) {
			if (words.get(i).compareTo(PRIORITY_IMPORTANT) == 0) {
				words.remove(i);
				nameSeparator = Math.min(nameSeparator, i - 1);
				return PRIORITY_IMPORTANT_VALUE;
			}
			if (words.get(i).compareTo(PRIORITY_NORMAL) == 0) {
				words.remove(i);
				nameSeparator = Math.min(nameSeparator, i - 1);
				return PRIORITY_IMPORTANT_VALUE;
			}
			if (words.get(i).compareTo(PRIORITY_CUSTOM) == 0) {
				if (i + 1 < words.size())
				{
					//check valid integer
					nameSeparator = Math.min(nameSeparator, i - 1);
					return Integer.parseInt(words.get(i + 1));
				}
			}
		}
		return 0;
	}
	
	public int decomposeFrequency(ArrayList<String> words) {
		for (int i = 0; i < words.size(); i++) {
			if (words.get(i).compareTo(FREQUENCY_DAILY) == 0) {
				words.remove(i);
				nameSeparator = Math.min(nameSeparator, i - 1);
				return FREQUENCY_DAILY_VALUE;
			}
			if (words.get(i).compareTo(FREQUENCY_WEEKLY) == 0) {
				words.remove(i);
				nameSeparator = Math.min(nameSeparator, i - 1);
				return FREQUENCY_WEEKLY_VALUE;
			}
			if (words.get(i).compareTo(FREQUENCY_MONTHLY) == 0) {
				words.remove(i);
				nameSeparator = Math.min(nameSeparator, i - 1);
				return FREQUENCY_MONTHLY_VALUE;
			}
			if (words.get(i).compareTo(FREQUENCY_CUSTOM) == 0) {
				if (i + 1 < words.size())
				{
					//check valid integer
					nameSeparator = Math.min(nameSeparator, i - 1);
					return Integer.parseInt(words.get(i + 1));
				}
			}
		}
		return 0;
	}
	
	public String decomposeDescription(ArrayList<String> words) {
		for (int i = 0; i < words.size(); i++) {
			String tempString = "";
			for (int j = i; j < words.size(); j++) {
				if (i == j)
					tempString = words.get(j);
				else
					tempString = tempString + " " + words.get(j); 
				if (words.get(i).charAt(0) == '(' && words.get(j).charAt(0) == ')') {
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
	
	public Date[] decomposeDate(ArrayList<String> words) {
		Date[] date = new Date[2];
		date[0] = new Date();
		date[1] = new Date();
		
		String fullString = "";
		for (String s : words) {
			fullString = fullString + " " + s;
		}
		List<DateGroup> dateGroupFull = dateParser.parse(fullString);
		if (dateGroupFull.isEmpty()) {
			return date;
		}
		List<Date> fullDate = dateGroupFull.get(0).getDates();

		//determine nameSeparator
		if (!words.isEmpty()) {
			String tempString = "";
			for (int i = (int)words.size() - 1; i >= 0; i--) {
				if (i == words.size() - 1) {
					tempString = words.get(i);
				} else {
					tempString = words.get(i) + " " + tempString;
				}
				List<DateGroup> dateGroup = dateParser.parse(tempString);
				if (dateGroup.size() != 0) {
					List<Date> tempDate = dateGroup.get(0).getDates();
					if (tempDate.size() == fullDate.size())
					{
						Boolean ok = true;
						for (int j = 0; j < tempDate.size(); j++) {
							if (!DateUtils.isSameDay(tempDate.get(j),fullDate.get(j))) {
								ok = false;
								break;
							}
						}
						if (ok) {
							nameSeparator = Math.min(nameSeparator, i - 1);
							break;
						}
					}
				}	
			}
			if (DATE_PREPOSITION.contains(words.get(nameSeparator)))
			{
				nameSeparator--;
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
		return date;	
	}

	public String decomposeName(ArrayList<String> words, int nameSeparator) {
		String result = "";
		for (int i = 0; i <= nameSeparator; i++) {
			result = result + words.get(i) + " ";
		}
		return result.trim();
	}
	
	public Task decompose(String task)
	{		
		Task resultTask = new Task();
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(task.split(" ")));
		nameSeparator = words.size() - 1;
		resultTask.setDescription(decomposeDescription(words));
		resultTask.setPriority(decomposePriority(words));
		resultTask.setFrequency(decomposeFrequency(words));
		Date[] date = decomposeDate(words);
		if (date[0].compareTo(date[1]) > 0)
		{
			Date temp = date[0];
			date[0] = date[1];
			date[1] = temp;
		}
		resultTask.setStartDate(date[0]);
		resultTask.setEndDate(date[1]);
		resultTask.setName(decomposeName(words, nameSeparator));
		return resultTask;
	}

	public LogicParser()
	{
		
	}
}
