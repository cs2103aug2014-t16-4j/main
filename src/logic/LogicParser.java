package logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
	
	int nameSeparate;
	Parser dateParser = new Parser();
	
	public int decomposePriority(ArrayList<String> words) {
		for (int i = 0; i < words.size(); i++) {
			if (words.get(i).compareTo(PRIORITY_IMPORTANT) == 0) {
				words.remove(i);
				nameSeparate = Math.min(nameSeparate, i - 1);
				return PRIORITY_IMPORTANT_VALUE;
			}
			if (words.get(i).compareTo(PRIORITY_NORMAL) == 0) {
				words.remove(i);
				nameSeparate = Math.min(nameSeparate, i - 1);
				return PRIORITY_IMPORTANT_VALUE;
			}
			if (words.get(i).compareTo(PRIORITY_CUSTOM) == 0) {
				if (i + 1 < words.size())
				{
					//check valid integer
					nameSeparate = Math.min(nameSeparate, i - 1);
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
				nameSeparate = Math.min(nameSeparate, i - 1);
				return FREQUENCY_DAILY_VALUE;
			}
			if (words.get(i).compareTo(FREQUENCY_WEEKLY) == 0) {
				words.remove(i);
				nameSeparate = Math.min(nameSeparate, i - 1);
				return FREQUENCY_WEEKLY_VALUE;
			}
			if (words.get(i).compareTo(FREQUENCY_MONTHLY) == 0) {
				words.remove(i);
				nameSeparate = Math.min(nameSeparate, i - 1);
				return FREQUENCY_MONTHLY_VALUE;
			}
			if (words.get(i).compareTo(FREQUENCY_CUSTOM) == 0) {
				if (i + 1 < words.size())
				{
					//check valid integer
					nameSeparate = Math.min(nameSeparate, i - 1);
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
					nameSeparate = Math.min(nameSeparate, i - 1);
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
		int numberOfDate = 0;
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
					if (tempDate.size() == 1 && numberOfDate == 0)
					{
						numberOfDate = 1;
						nameSeparate = Math.min(nameSeparate, i - 1);
						date[0] = tempDate.get(0);
						date[1] = date[0];
					}
					if (tempDate.size() == 2 && numberOfDate == 1)
					{
						numberOfDate = 2;
						nameSeparate = Math.min(nameSeparate, i - 1);
						date[0] = tempDate.get(0);
						date[1] = tempDate.get(1);
						break;
					}
				}	
			}
		}
		/*int count = 0;
		for (int i = 0; i < words.size(); i++) {
			String tempString = "";
			for (int j = i; j < words.size(); j++) {
				if (i == j)
					tempString = words.get(j);
				else
					tempString = tempString + " " + words.get(j);
				if (dateParser.parse(tempString).size() == 1) {
					date[count++] = dateParser.parse(tempString).get(0).getDates().get(0);
					nameSeparate = Math.min(nameSeparate, i - 1);
				}
			}
			/*SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");
			try {
				//if not valid, it will throw ParseException
				Date temp = sdf.parse(words.get(i));
				date[count++] = temp;
				nameSeparate = Math.min(nameSeparate, i - 1);
			} catch (ParseException e) {
			} finally {
			}
		}*/
		return date;	
	}

	public String decomposeName(ArrayList<String> words, int nameSeparate) {
		String result = "";
		for (int i = 0; i <= nameSeparate; i++) {
			result = result + words.get(i) + " ";
		}
		return result.trim();
	}
	
	public Task decompose(String task)
	{		
		Task resultTask = new Task();
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(task.split(" ")));
		nameSeparate = words.size() - 1;
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
		resultTask.setName(decomposeName(words, nameSeparate));
		return resultTask;
	}

	public LogicParser()
	{
		
	}
}
