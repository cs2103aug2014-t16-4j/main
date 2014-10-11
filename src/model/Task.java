package model;

import java.util.Date;

public class Task {
	private String name;
	private String description;
	private Date startDate;
	private Date endDate;
	private int priority;
	private int frequency;

	public Task(String name, String description, Date startDate, Date endDate,
			int priority, int frequency) {
		super();
		this.name = name;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
		this.priority = priority;
		this.frequency = frequency;
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
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	public Task() {
		
	}
	
	public Task(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isEmpty() {
		return name.isEmpty();
	}

}
