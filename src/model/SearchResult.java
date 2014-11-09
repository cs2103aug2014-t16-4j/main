package model;


import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONObject;

public class SearchResult {
	ArrayList<JSONObject> tasksBuffer;
	Date startDate;
	Date endDate;
	
	public SearchResult(ArrayList<JSONObject> tasksBuffer, Date startDate,
			Date endDate) {
		this.tasksBuffer = tasksBuffer;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public ArrayList<JSONObject> getTasksBuffer() {
		return tasksBuffer;
	}
	public void setTasksBuffer(ArrayList<JSONObject> tasksBuffer) {
		this.tasksBuffer = tasksBuffer;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
}
