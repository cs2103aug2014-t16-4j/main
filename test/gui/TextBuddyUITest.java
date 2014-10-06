package gui;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TextBuddyUITest {

	String fileName = "kaunghtet.txt";
	TextBuddyUI logic;
	@Before
	public void setUp() throws Exception {
		logic = new TextBuddyUI(fileName);
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
		ArrayList<JSONObject> returnString = logic.search("do");
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
		ArrayList<JSONObject> returnString = logic.search("Anything not from text file");
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

}
