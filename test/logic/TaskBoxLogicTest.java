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
		//System.out.println(returnString);
		//System.out.println(expectedString);
		assertEquals(expectedStringJSON, searchResult.getTasksBuffer().toString());
		assertEquals(expectedStringDate, searchResult.getDate().toString());
	}
	
	@Test
	public void testSearchNotFound(){
		System.out.println("=== Testing search function ===");
		String expectedString = "None was found";
		//System.out.println(returnString);
		//assertEquals(expectedString,returnObjects);
	}

//	@Test
//	public void testSort() throws Exception {
//		System.out.println("=== Testing sort function ===");
//		String expectedString = "1. AAAA\n2. CCCC\n3. DDDD\n4. EEEE\n5. FFFF";
//		logic.clear();
//		logic.add("AAAA");
//		logic.add("DDDD");
//		logic.add("CCCC");
//		logic.add("FFFF");
//		logic.add("EEEE");
//		logic.sort();
//		ArrayList<JSONObject> returnObjects= logic.getDisplayList();
//		//System.out.println(returnString);
//		assertEquals(expectedString,returnObjects);
//	}
//
//	@Test
//	public void testSortwithSameFirstWord() throws Exception{
//		System.out.println("=== Testing sort function ===");
//		String expectedString = "1. to do A\n2. to do B\n3. to do C\n4. to do D\n5. to do E";
//		logic.clear();
//		logic.add("to do A");
//		logic.add("to do C");
//		logic.add("to do B");
//		logic.add("to do E");
//		logic.add("to do D");
//		logic.sort();
//		ArrayList<JSONObject> returnObjects = logic.getDisplayList();
//		//System.out.println(returnString);
//		assertEquals(expectedString,returnObjects);
//	}

	
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
