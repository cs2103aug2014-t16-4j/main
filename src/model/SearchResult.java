package model;


import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONObject;

public class SearchResult {
	ArrayList<JSONObject> tasksBuffer;
	ArrayList<Date> date;
	public ArrayList<JSONObject> getTasksBuffer() {
		return tasksBuffer;
	}
	public void setTasksBuffer(ArrayList<JSONObject> tasksBuffer) {
		this.tasksBuffer = tasksBuffer;
	}
	public ArrayList<Date> getDate() {
		return date;
	}
	public void setDate(ArrayList<Date> date) {
		this.date = date;
	}
	public SearchResult(ArrayList<JSONObject> tasksBuffer, ArrayList<Date> date) {
		this.tasksBuffer = tasksBuffer;
		this.date = date;
	}
	
}
