package model;

import org.json.simple.JSONObject;

import logic.CommandEnum;

public class Operation {
	Task task;
	JSONObject jTask;
	CommandEnum commandType;
	
	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
		this.task = task;
	}

	public CommandEnum getCommandType() {
		return commandType;
	}
	
	public JSONObject getjTask() {
		return jTask;
	}

	public void setjTask(JSONObject jTask) {
		this.jTask = jTask;
	}

	public void setCommandType(CommandEnum commandType) {
		this.commandType = commandType;
	}
	
	public Operation(Task task, CommandEnum commandType) {
		super();
		this.task = task;
		this.jTask = null;
		this.commandType = commandType;
	}

	public Operation(JSONObject jTask, CommandEnum commandType) {
		super();
		this.task = null;
		this.jTask = jTask;
		this.commandType = commandType;
	}
}
