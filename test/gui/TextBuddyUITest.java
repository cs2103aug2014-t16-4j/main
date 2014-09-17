package gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
		String returnString = logic.sort();
		System.out.println(returnString);
		assertEquals(expectedString,returnString);
		
	}

	@Test
	public void testDelete() {
		fail("Not yet implemented"); 
	}

	@Test
	public void testClear() {
	}

	@Test
	public void testDisplay() {

	}

	@Test
	public void testAdd() {
		System.out.println("Testing adding function...");
		logic.clear();
		String expectedString = "added to "+ fileName +": \"To eat\"";
		String returnString = logic.add("To eat");
		System.out.println(returnString);
		assertEquals(expectedString,returnString);
	}

}
