package logic;

import model.Task;

import org.json.simple.JSONObject;

public class Converter {
	
	@SuppressWarnings("unchecked")
	public static JSONObject taskToJSON(Task task)
	{
		JSONObject jTask=new JSONObject();
		jTask.put(Consts.NAME, task.getName());
		jTask.put(Consts.DESCRIPTION, task.getDescription());
		jTask.put(Consts.STARTDATE, Consts.formatter.format(task.getStartDate()));
		jTask.put(Consts.ENDDATE, Consts.formatter.format(task.getEndDate()));
		jTask.put(Consts.PRIORITY, task.getPriority());
		jTask.put(Consts.FREQUENCY, task.getFrequency());
		return jTask;
	}		
	
	public static Task jsonToTask(JSONObject obj){
		Task temp = null;
		try{
			temp = new Task(obj.get(Consts.NAME).toString());
			temp.setDescription(obj.get(Consts.DESCRIPTION).toString());
			temp.setStartDate(Consts.formatter.parse(obj.get(Consts.STARTDATE).toString()));
			temp.setEndDate(Consts.formatter.parse(obj.get(Consts.ENDDATE).toString()));
			temp.setFrequency((int) obj.get(Consts.FREQUENCY));
			temp.setPriority((int) obj.get(Consts.PRIORITY));
		}catch(Exception e){
			//e.printStackTrace();
		}
		return temp;	
	}
}
