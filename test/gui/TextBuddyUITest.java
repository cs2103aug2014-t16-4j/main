package gui;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import logic.Consts;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.SWT;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TextBuddyUITest {

	String[] fileName = {"kaunghtet.txt"};
	UIController logic;
	ArrayList<JSONObject> taskList;
	@Before
	public void setUp() throws Exception {
		logic = new UIController(fileName);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSearch() {
		System.out.println("=== Testing search function ===");
		String expectedString = "1. to do something\n2. to do cs2103 project";
		logic.clear();
		logic.add("to sleep");
		logic.add("to do something");
		logic.add("to do cs2103 project");
		logic.add("to eat");
		logic.search("do");
		ArrayList<JSONObject> returnString = logic.getDisplayList();
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
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
		ArrayList<JSONObject> returnString = logic.getDisplayList();
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
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
		ArrayList<JSONObject> returnString = logic.getDisplayList();
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
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
		ArrayList<JSONObject> returnString = logic.getDisplayList();
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
	}
	@Test
	public void testDelete() {
		System.out.println("=== Testing delete function ===");
		String expectedString = "deleted from " + fileName + ": \"Second line\"";
		logic.clear();
		logic.add("First line");
		logic.add("Second line");
		logic.add("Third line");
		logic.add("Forth line");
		String returnString = logic.delete("2");
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
	}

	@Test
	public void testDeleteLineNotFound(){
		System.out.println("=== Testing delete function ===");
		String expectedString = "Line number not found! None was deleted.";
		logic.clear();
		logic.add("First line");
		logic.add("Second line");
		logic.add("Third line");
		logic.add("Forth line");
		String returnString = logic.delete("-1");
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
	}
	
	@Test
	public void testClear() {
		System.out.println("=== Testing clear function ===");
		String expectedString = "All content deleted from "+ fileName;
		logic.add("first one");
		logic.add("second one");
		String returnString = logic.clear();
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
	}

	@Test
	public void testAdd() {
		System.out.println("=== Testing add function ===");
		String expectedString = "added to "+ fileName +": \"To eat\"";
		logic.clear();
		String returnString = logic.add("To eat");
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
	}
	
	@Test
	public void testAddWithBlankTask(){
		System.out.println("=== Testing add function with blank todo ===");
		String expectedString = "Task cannot be blank.";
		logic.clear();
		String returnString = logic.add("");
		//System.out.println(returnString);
		assertEquals(expectedString,returnString);
	}

	@Test
	//ensures the UI is getting correct taskList
	public void testGetTaskList(){
		System.out.println("=== Testing getTaskList function ===");
		int expectedSize = 4;
		logic.clear();
		logic.add("to sleep");
		logic.add("to do something");
		logic.add("to do cs2103 project");
		logic.add("to eat");
		taskList = logic.getTaskList();
		assertEquals(expectedSize,taskList.size());
	}
	
	@SuppressWarnings("static-access")
	@Test
	//ensures the UI is assigning correct color based on priority
	public void getImportantColor(){
		System.out.println("=== Testing getColorWithPriority function ===");
		Color red = logic.DISPLAY.getSystemColor(SWT.COLOR_RED);
		Color testColor = logic.getColorWithPriority(Consts.TASK_IMPORTANT);
		assertEquals(red,testColor);
	}
	
	@SuppressWarnings("static-access")
	@Test
	//ensures the UI is assigning correct color based on priority
	public void getNormalColor(){
		System.out.println("=== Testing getColorWithPriority function ===");
		Color black = logic.DISPLAY.getSystemColor(SWT.COLOR_BLACK);
		Color testColor = logic.getColorWithPriority(Consts.TASK_NORMAL);
		assertEquals(black,testColor);
	}
	
}
