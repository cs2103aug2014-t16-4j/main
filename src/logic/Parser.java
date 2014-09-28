package logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import model.Task;

public class Parser {

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
	
	public Date decomposeDate(ArrayList<String> words) {
		Date date = new Date();
		for (int i = 0; i < words.size(); i++) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");
			try {
				//if not valid, it will throw ParseException
				date = sdf.parse(words.get(i));	 
				nameSeparate = Math.min(nameSeparate, i - 1);
			} catch (ParseException e) {
			} finally {
			}
		}
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
		resultTask.setDate(decomposeDate(words));
		resultTask.setName(decomposeName(words, nameSeparate));
		return resultTask;
	}

	public Parser()
	{
		
	}
}
