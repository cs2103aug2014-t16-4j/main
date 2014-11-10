package logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import logic.Consts;
import logic.Converter;
import logic.LogicController;
import logic.LogicParser;
import model.SearchResult;
import model.Task;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TaskBoxLogicTest {

	String[] fileName = {"kaunghtet.txt"};
	LogicController logic;
	LogicParser parser;
	@Before
	public void setUp() throws Exception {
		logic = new LogicController();
		logic.setFileName(fileName[0]);
		parser = new LogicParser();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSearch() {
		System.out.println("=== Testing search function ===");
		logic.clear();
		logic.add(parser.decompose("do CS2103 today"),false);
		logic.add(parser.decompose("do CS2104 tomorrow"), false);
		logic.add(parser.decompose("do CS2105 day after tomorrow"),false);
		logic.add(parser.decompose("do CS2106 today to tomorrow"),false);
		String expectedStringJSON = "[{\"Name\":\"do CS2104\",\"Status\":1,\"Description\":\"\",\"EndDate\":\"11\\/11\\/2014 23:59:59\",\"StartDate\":\"11\\/11\\/2014 00:00:00\",\"Frequency\":0,\"Priority\":0}, {\"Name\":\"do CS2106\",\"Status\":1,\"Description\":\"\",\"EndDate\":\"11\\/11\\/2014 23:59:59\",\"StartDate\":\"10\\/11\\/2014 00:00:00\",\"Frequency\":0,\"Priority\":0}]";
		String expectedStringDate = "[Tue Nov 11 00:00:00 SGT 2014, Tue Nov 11 00:00:00 SGT 2014]";
		SearchResult searchResult = new SearchResult(new ArrayList<JSONObject>(), new ArrayList<Date>());
		try {
			searchResult = logic.search("tomorrow", Consts.STATUS_TIMED_TASK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(expectedStringJSON, searchResult.getTasksBuffer().toString());
		assertEquals(expectedStringDate, searchResult.getDate().toString());
	}	
	
	@Test
	public void testRepeatedFeature() {
		System.out.println("=== Testing search function ===");
		logic.clear();
		logic.add(parser.decompose("do CS2103 today weekly"),false);
		logic.add(parser.decompose("do CS2104 tomorrow daily"), false);
		logic.add(parser.decompose("do CS2105 today monthly"), false);
		String expectedString = "[{\"Name\":\"do CS2103\",\"Status\":1,\"Description\":\"\",\"EndDate\":\"10\\/11\\/2014 23:59:59\",\"StartDate\":\"10\\/11\\/2014 00:00:00\",\"Frequency\":2,\"Priority\":0}, {\"Name\":\"do CS2104\",\"Status\":1,\"Description\":\"\",\"EndDate\":\"11\\/11\\/2014 23:59:59\",\"StartDate\":\"11\\/11\\/2014 00:00:00\",\"Frequency\":1,\"Priority\":0}]";
		SearchResult searchResult = new SearchResult(new ArrayList<JSONObject>(), new ArrayList<Date>());
		try {
			searchResult = logic.search("11/15/2014 to 11/20/2014", Consts.STATUS_TIMED_TASK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(expectedString, searchResult.getTasksBuffer().toString());
	}	
	
	@Test
	public void testDelete() {
		System.out.println("=== Testing delete function ===");
		logic.clear();
		Task soonToBeDeletedTask = parser.decompose("Second line");
		Task first = parser.decompose("First line");
		Task third = parser.decompose("Third line");
		Task fourth = parser.decompose("Fourth line");
		logic.add(first,false);
		logic.add(soonToBeDeletedTask, false);
		logic.add(third,false);
		logic.add(fourth,false);
		String expectedString = String.format(Consts.STRING_DELETE, fileName[0],soonToBeDeletedTask.getName());
		String returnString = logic.delete(Converter.taskToJSON(soonToBeDeletedTask));
		//System.out.println(returnString);
		//System.out.println(expectedString);
		assertEquals(expectedString,returnString);
	}
	
	@Test
	public void testDeleteAndUndo() {
		System.out.println("=== Testing delete and undo function ===");
		logic.clear();
		Task soonToBeDeletedTask = parser.decompose("Second line");
		Task first = parser.decompose("First line");
		Task third = parser.decompose("Third line");
		Task fourth = parser.decompose("Fourth line");
		logic.add(first,true);
		logic.add(soonToBeDeletedTask, true);
		logic.add(third,true);
		logic.add(fourth,true);
		logic.delete(Converter.taskToJSON(soonToBeDeletedTask),true);
		logic.undo();
		boolean returnBoolean = false;
		for(JSONObject i:LogicController.tasksBuffer){
			if(Converter.jsonToTask(i).getName().equalsIgnoreCase(soonToBeDeletedTask.getName())){
				returnBoolean = true;
			}
		}
		assertTrue(returnBoolean);
	}
	
	@Test
	public void testAddAndUndo() {
		System.out.println("=== Testing add and undo  ===");
		logic.clear();
		Task first = parser.decompose("First line");
		Task third = parser.decompose("Third line");
		Task fourth = parser.decompose("Fourth line");
		logic.add(first,true);
		logic.add(third,true);
		logic.add(fourth,true);
		logic.undo();
		boolean returnBoolean = true;
		for(JSONObject i:LogicController.tasksBuffer){
			//System.out.println(Converter.jsonToTask(i).getName());
			if(Converter.jsonToTask(i).getName().equalsIgnoreCase(fourth.getName())){
				returnBoolean = false;
			}
		}
		assertTrue(returnBoolean);
	}


	@Test
	public void testDeleteLineNotFound(){
		System.out.println("=== Testing delete function with invalid selection ===");
		logic.clear();
		Task soonToBeDeletedTask = parser.decompose("Second line");
		Task first = parser.decompose("First line");
		Task third = parser.decompose("Third line");
		Task fourth = parser.decompose("Fourth line");
		logic.add(first,false);
		logic.add(third,false);
		logic.add(fourth,false);
		String expectedString = Consts.USAGE_DELETE;
		String returnString = logic.delete(Converter.taskToJSON(soonToBeDeletedTask));
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
	}
	
	@Test
	public void testClear() {
		System.out.println("=== Testing clear function ===");
		logic.add(parser.decompose("first one"),false);
		logic.add(parser.decompose("second one"),false);
		Boolean returnBoolean = logic.clear(false);
		//System.out.println(returnString);
		assertEquals(true, returnBoolean);
	}

	@Test
	public void testAdd() {
		System.out.println("=== Testing add function ===");
		logic.clear();
		Task newTask = parser.decompose("To eat");
		String returnString= logic.add(newTask,false);
		String expectedString = String.format(Consts.STRING_ADD, newTask.getName(), newTask.getStartDate(), newTask.getEndDate());
		//System.out.println(returnString); //debugging
		assertEquals(expectedString,returnString);
	}
	
	@Test
	public void testAddWithBlankTask(){
		System.out.println("=== Testing add function with blank todo ===");
		logic.clear();
		String returnString = logic.add(parser.decompose(" "),false);
		String expectedString = Consts.ERROR_ADD;
		//System.out.println(returnBoolean); //debugging
		assertEquals(expectedString,returnString);
	}
}
