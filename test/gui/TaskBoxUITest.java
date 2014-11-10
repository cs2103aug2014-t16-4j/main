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

public class TaskBoxUITest {

	String fileName = "mytext.txt";
	UIController UC;
	ArrayList<JSONObject> taskList;
	@Before
	public void setUp() throws Exception {
		UC = new UIController(fileName);
	}

	@After
	public void tearDown() throws Exception {
	}
	
//	@Test
//	public void testSearch() {
//		System.out.println("=== Testing search function ===");
//		String expectedString = "1. to do something\n2. to do cs2103 project";
//		UC.clear();
//		UC.add("to sleep");
//		UC.add("to do something");
//		UC.add("to do cs2103 project");
//		UC.add("to eat");
//		UC.search("do");
//		ArrayList<JSONObject> returnObjects = UC.getDisplayList();
//		//System.out.println(returnString);
//		assertEquals(expectedString,returnObjects);
//	}
//	
//	@Test
//	public void testSearchNotFound(){
//		System.out.println("=== Testing search function ===");
//		String expectedString = Consts.STRING_NOT_FOUND;
//		UC.clear();
//		UC.add("to sleep");
//		UC.add("to do something");
//		UC.add("to do cs2103 project");
//		UC.add("to eat");
//		UC.search("Anything not from text file");
//		ArrayList<JSONObject> returnObjects = UC.getDisplayList();
//		//System.out.println(returnString);
//		assertEquals(expectedString,returnObjects);
//	}
//
//	@Test
//	public void testSort() throws Exception {
//		System.out.println("=== Testing sort function ===");
//		String expectedString = "1. AAAA\n2. CCCC\n3. DDDD\n4. EEEE\n5. FFFF";
//		UC.clear();
//		UC.add("AAAA");
//		UC.add("DDDD");
//		UC.add("CCCC");
//		UC.add("FFFF");
//		UC.add("EEEE");
//		UC.sort();
//		ArrayList<JSONObject> returnObjects= UC.getDisplayList();
//		//System.out.println(returnString);
//		assertEquals(expectedString,returnObjects);
//	}
//
//	@Test
//	public void testSortwithSameFirstWord() throws Exception{
//		System.out.println("=== Testing sort function ===");
//		String expectedString = "1. to do A\n2. to do B\n3. to do C\n4. to do D\n5. to do E";
//		UC.clear();
//		UC.add("to do A");
//		UC.add("to do C");
//		UC.add("to do B");
//		UC.add("to do E");
//		UC.add("to do D");
//		UC.sort();
//		ArrayList<JSONObject> returnObjects = UC.getDisplayList();
//		//System.out.println(returnString);
//		assertEquals(expectedString,returnObjects);
//	}
//	@Test
//	public void testDelete() {
//		System.out.println("=== Testing delete function ===");
//		String expectedString = "deleted from " + fileName[0] + ": \"Second line\"";
//		UC.clear();
//		UC.add("First line");
//		UC.add("Second line");
//		UC.add("Third line");
//		UC.add("Forth line");
//		String returnString = UC.delete("2");
//		//System.out.println(returnString);
//		assertEquals(expectedString,returnString);
//	}
//
//	@Test
//	public void testDeleteLineNotFound(){
//		System.out.println("=== Testing delete function ===");
//		String expectedString = "Line number not found! None was deleted.";
//		UC.clear();
//		UC.add("First line");
//		UC.add("Second line");
//		UC.add("Third line");
//		UC.add("Forth line");
//		String returnString = UC.delete("-1");
//		//System.out.println(returnString);
//		assertEquals(expectedString,returnString);
//	}
//	
//	@Test
//	public void testClear() {
//		System.out.println("=== Testing clear function ===");
//		String expectedString = "All content deleted from "+ fileName[0];
//		String returnString = UC.clear();
//		System.out.println(returnString);
//		assertEquals(expectedString,returnString);
//	}
//
//	@Test
//	public void testAdd() {
//		System.out.println("=== Testing add function ===");
//		String expectedString = "added to "+ fileName[0] +": \"To eat\"";
//		UC.clear();
//		String returnString = UC.add("To eat");
//		//System.out.println(returnString);
//		assertEquals(expectedString,returnString);
//	}
//	
//	@Test
//	public void testAddWithBlankTask(){
//		System.out.println("=== Testing add function with blank todo ===");
//		String expectedString = "Task cannot be blank.";
//		UC.clear();
//		String returnString = UC.add("");
//		//System.out.println(returnString);
//		assertEquals(expectedString,returnString);
//	}
//
//	@Test
//	//ensures the UI is getting correct taskList
//	public void testGetTaskList(){
//		System.out.println("=== Testing getTaskList function ===");
//		int expectedSize = 4;
//		UC.clear();
//		UC.add("to sleep");
//		UC.add("to do something");
//		UC.add("to do cs2103 project");
//		UC.add("to eat");
//		taskList = UC.getTaskList();
//		assertEquals(expectedSize,taskList.size());
//	}
//	
//	@Test
//	//ensures the UI is assigning black color for normal tasks
//	public void getNormalColor(){
//		System.out.println("=== Testing normal color function ===");
//		Color black = UIController.DISPLAY.getSystemColor(SWT.COLOR_BLACK);
//		Color testColor = UC.getColorWithPriority(Consts.TASK_NORMAL);
//		assertEquals(black,testColor);
//	}
//	
//	@Test
//	//ensures the UI is assigning red color for important tasks
//	public void getImportantColor(){
//		System.out.println("=== Testing important color function ===");
//		Color red = UIController.DISPLAY.getSystemColor(SWT.COLOR_RED);
//		Color testColor = UC.getColorWithPriority(Consts.TASK_IMPORTANT);
//		assertEquals(red,testColor);
//	}
	
	@Test
	//ensures the UI is assigning black color for all other tasks
	//this is also a boundary value analysis
	public void getDefaultColor(){
		System.out.println("=== Testing all other color function ===");
		Color testColor1 = UC.getColorWithPriority(-1);
		Color testColor2 = UC.getColorWithPriority(2);
		assertEquals(true,testColor1.equals(testColor2));
	}
	
}
