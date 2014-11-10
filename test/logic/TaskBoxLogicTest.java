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
	public void testCompleted() {
		System.out.println("=== Testing completed function ===");
		logic.clear();
		Task completeTask = parser.decompose("do CS2101");
		logic.add(completeTask);
		logic.add(parser.decompose("do CS2101"),false);
		logic.add(parser.decompose("do CS2103 today"),false);
		logic.add(parser.decompose("do CS2104 tomorrow"), false);
		logic.add(parser.decompose("do CS2105 day after tomorrow"),false);
		logic.add(parser.decompose("do CS2106 today to tomorrow"),false);
		String expectedString = "Completed do CS2101";
		assertEquals(expectedString, logic.complete(logic.getFloatingTasksBuffer().get(0), Consts.STATUS_COMPLETED_FLOATING_TASK));
		assertEquals(Consts.STATUS_COMPLETED_FLOATING_TASK, logic.getFloatingTasksBuffer().get(0).get(Consts.STATUS));		
	}	
	
	@Test
	public void testBlock() {
		System.out.println("=== Testing block function ===");
		logic.clear();
		String expectedString = "Blocked 10/11/2014 14:00:00 -> 10/11/2014 16:00:00";
		assertEquals(expectedString, logic.block("from 2pm to 4pm"));
		expectedString = "The time frame is blocked.";
		assertEquals(expectedString, logic.add(parser.decompose("do CS2101 2pm"),false));
	}	

	@Test
	public void testUpdate() {
		System.out.println("=== Testing update function ===");
		logic.clear();
		Task updateTask = parser.decompose("do CS2101 today");
		logic.add(updateTask);
		logic.add(parser.decompose("do CS2101"),false);
		logic.add(parser.decompose("do CS2103 today"),false);
		logic.add(parser.decompose("do CS2104 tomorrow"), false);
		logic.add(parser.decompose("do CS2105 day after tomorrow"),false);
		logic.add(parser.decompose("do CS2106 today to tomorrow"),false);
		String expectedString = "do CS2101 is updated.\n";
		assertEquals(expectedString, logic.update(logic.getTimedTasksBuffer().get(0), "date", "today"));
		expectedString = "do CS2101 is updated.";
		assertEquals("10/11/2014 00:00:00", logic.getTimedTasksBuffer().get(0).get(Consts.STARTDATE));
	}	
	
	@Test
	public void testRepeatedFeature() {
		System.out.println("=== Testing repeated feature ===");
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
		assertEquals(expectedString,returnString);
	}
	
	@Test
	public void testClear() {
		System.out.println("=== Testing clear function ===");
		logic.add(parser.decompose("first one"),false);
		logic.add(parser.decompose("second one"),false);
		Boolean returnBoolean = logic.clear(false);
		assertEquals(true, returnBoolean);
	}

	@Test
	public void testAdd() {
		System.out.println("=== Testing add function ===");
		logic.clear();
		Task newTask = parser.decompose("To eat");
		String returnString= logic.add(newTask,false);
		String expectedString = String.format(Consts.STRING_ADD, newTask.getName(), newTask.getStartDate(), newTask.getEndDate());
		assertEquals(expectedString,returnString);
	}
	
	@Test
	public void testAddWithBlankNameTask(){
		System.out.println("=== Testing add function with blank todo ===");
		logic.clear();
		String returnString = logic.add(parser.decompose(" today "),false);
		String expectedString = Consts.ERROR_ADD;
		assertEquals(expectedString,returnString);
	}
}
