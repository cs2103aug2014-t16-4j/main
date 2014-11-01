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
		jTask.put(Consts.STARTDATE, task.getStartDate()!=null?Consts.FORMAT_DATE.format(task.getStartDate()):"");
		jTask.put(Consts.ENDDATE, task.getEndDate()!=null?Consts.FORMAT_DATE.format(task.getEndDate()):"");
		jTask.put(Consts.PRIORITY, task.getPriority());
		jTask.put(Consts.FREQUENCY, task.getFrequency());
		jTask.put(Consts.STATUS, task.getStatus());
		return jTask;
	}		
	
	public static Task jsonToTask(JSONObject obj){
		Task temp = null;
		try{
			temp = new Task(obj.get(Consts.NAME).toString());
			temp.setDescription(obj.get(Consts.DESCRIPTION).toString());
			temp.setStartDate(Consts.FORMAT_DATE.parse(obj.get(Consts.STARTDATE).toString()));
			temp.setEndDate(Consts.FORMAT_DATE.parse(obj.get(Consts.ENDDATE).toString()));
			temp.setFrequency((int) Integer.parseInt(obj.get(Consts.FREQUENCY).toString()));
			temp.setPriority((int) Integer.parseInt(obj.get(Consts.PRIORITY).toString()));
			temp.setStatus((int) Integer.parseInt(obj.get(Consts.STATUS).toString()));
		}catch(Exception e){
			e.printStackTrace();
		}
		return temp;	
	}
}
