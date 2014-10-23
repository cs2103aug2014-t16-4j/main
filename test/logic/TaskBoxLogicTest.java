package logic;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import model.Task;

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
	
	/*
	@Test
	public void testSearch() {
		System.out.println("=== Testing search function ===");
		String expectedString = "[{"+"\"Name"+":to do something","Description":"","EndDate":"23\/10\/2014 06:18:24","StartDate":"23\/10\/2014 06:18:24","Frequency":0,"Priority":0}, {"Name":"to do cs2103 project","Description":"","EndDate":"23\/10\/2014 06:18:34","StartDate":"23\/10\/2014 06:18:34","Frequency":0,"Priority":0}]""";
		logic.clear();
		logic.add(new Task("to sleep"),false);
		logic.add(new Task("to do something"),false);
		logic.add(new Task("to do cs2103 project"),false);
		logic.add(new Task("to eat"),false);
		logic.search("do");
		String returnString = logic.
		//System.out.println(returnString);
		//assertEquals(expectedString,returnObjects);
	}
	
	@Test
	public void testSearchNotFound(){
		System.out.println("=== Testing search function ===");
		String expectedString = "None was found";
		logic.clear();
		logic.add("to sleep");
		logic.add("to do something");
		logic.add("to do cs2103 project");
		logic.add("to eat");
		logic.search("Anything not from text file");
		ArrayList<JSONObject> returnObjects = logic.getDisplayList();
		//System.out.println(returnString);
		assertEquals(expectedString,returnObjects);
	}

	@Test
	public void testSort() throws Exception {
		System.out.println("=== Testing sort function ===");
		String expectedString = "1. AAAA\n2. CCCC\n3. DDDD\n4. EEEE\n5. FFFF";
		logic.clear();
		logic.add("AAAA");
		logic.add("DDDD");
		logic.add("CCCC");
		logic.add("FFFF");
		logic.add("EEEE");
		logic.sort();
		ArrayList<JSONObject> returnObjects= logic.getDisplayList();
		//System.out.println(returnString);
		assertEquals(expectedString,returnObjects);
	}

	@Test
	public void testSortwithSameFirstWord() throws Exception{
		System.out.println("=== Testing sort function ===");
		String expectedString = "1. to do A\n2. to do B\n3. to do C\n4. to do D\n5. to do E";
		logic.clear();
		logic.add("to do A");
		logic.add("to do C");
		logic.add("to do B");
		logic.add("to do E");
		logic.add("to do D");
		logic.sort();
		ArrayList<JSONObject> returnObjects = logic.getDisplayList();
		//System.out.println(returnString);
		assertEquals(expectedString,returnObjects);
	}
	*/
	
	@Test
	public void testDelete() {
		System.out.println("=== Testing delete function ===");
		Task soonToBeDeletedTask = new Task("Second line");
		logic.add(new Task("First line"),false);
		logic.add(soonToBeDeletedTask,false);
		logic.add(new Task("Third line"),false);
		logic.add(new Task("Forth line"),false);
		String expectedString = String.format(Consts.STRING_DELETE, fileName,soonToBeDeletedTask.getName());
		String returnString = logic.delete(Converter.taskToJSON(soonToBeDeletedTask));
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
	}

	@Test
	public void testDeleteLineNotFound(){
		System.out.println("=== Testing delete function ===");
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
		System.out.println(returnString);
		assertEquals(expectedString,returnString);
	}
	
	@Test
	public void testClear() {
		System.out.println("=== Testing clear function ===");
		logic.add(new Task("first one"),false);
		logic.add(new Task("second one"),false);
		Boolean returnBoolean = logic.clear(false);
		//System.out.println(returnString);
		assertEquals(true, returnBoolean);
	}

	@Test
	public void testAdd() {
		System.out.println("=== Testing add function ===");
		logic.clear();
		Boolean returnBoolean = logic.add(new Task("To eat"),false);
		//System.out.println(returnString);
		assertEquals(true,returnBoolean);
	}
	
	@Test
	public void testAddWithBlankTask(){
		System.out.println("=== Testing add function with blank todo ===");
		logic.clear();
		Boolean returnBoolean = logic.add(new Task(""),false);
		//System.out.println(returnString);
		assertEquals(false,returnBoolean);
	}
}
