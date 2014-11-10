//@author A0097699X

package gui;

import static org.junit.Assert.assertEquals;
import gui.UIController;

import java.util.ArrayList;

import logic.CommandEnum;
import logic.Consts;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.SWT;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TaskBoxUITest {

	String fileName = "taskbox.txt";
	UIController UC = new UIController(fileName);

	@Test
	// ensures invalid complete is returning usage string correctly
	public void testInvalidComplete() {
		String s = UC.complete("random");
		String expected = Consts.USAGE_COMPLETE;
		assertEquals(expected, s);
	}

	@Test
	// ensures invalid undo is returning usage string correctly
	public void testInvalidUndo() {
		String s = UC.undo("random");
		String expected = Consts.USAGE_UNDO;
		assertEquals(expected, s);
	}

	@Test
	// ensures invalid delete is returning usage string correctly
	public void testInvalidDelete() {
		String s = UC.delete("random");
		String expected = Consts.USAGE_DELETE;
		assertEquals(expected, s);
	}

	@Test
	// ensures invalid expand is returning usage string correctly
	public void testInvalidExpand() {
		int s = UC.expand("random");
		int expected = -1;
		assertEquals(expected, s);
	}

	@Test
	// ensures the expand is returning values correctly for expand none
	public void testExpandNone() {
		int s = UC.expand("none");
		int expected = Consts.EXPAND_NONE;
		assertEquals(expected, s);
	}

	@Test
	// ensures the expand is returning values correctly
	public void testExpandAll() {
		int s = UC.expand("all");
		int expected = Consts.EXPAND_ALL;
		assertEquals(expected, s);
	}

	@Test
	// ensures the block command is blocking correctly
	public void testValidBlock() {
		String s = UC.block("10 Nov 5pm to 8pm");
		String expected = "Blocked 10/11/2014 17:00:00 -> 10/11/2014 20:00:00";
		assertEquals(expected, s);
	}

	@Test
	// ensures the block fail is returned correctly
	public void testInvalidBlock() {
		String s = UC.block("taskbox");
		assertEquals(s, "Block fail");
	}

	@Test
	// ensures the add command is returned correctly
	public void testInvalidUpdateEmpty() {
		String s = UC.update("");
		String expected = Consts.USAGE_UPDATE;
		assertEquals(expected, s);
	}

	@Test
	// ensures that .txt is added if it isn't there
	public void testAppendsExtensionFileOnlyWhenNecessary() {
		String a = UC.checkFileName("taskbox.txt");
		String b = UC.checkFileName("taskbox");
		assertEquals(a, b);
	}

	@Test
	// ensures the add command is returned correctly
	public void testAddCommand() {
		CommandEnum s = UC.getCommandType("add");
		assertEquals(s, CommandEnum.ADD);
	}

	@Test
	// ensures the delete command is returned correctly
	public void testDeleteCommand() {
		CommandEnum s = UC.getCommandType("delete");
		assertEquals(s, CommandEnum.DELETE);
	}

	@Test
	// ensures the invalid command is returned correctly
	public void testInvalidCommand() {
		CommandEnum s = UC.getCommandType("something");
		assertEquals(s, CommandEnum.INVALID);
	}

	@Test
	// ensures the string splitting is done correctly
	public void testSplittedString() {
		String[] sa1 = UC.getSplittedString("add something");
		String s1 = "add";
		String s2 = "something";
		assertEquals(s1.compareTo(sa1[0]) == 0, s2.compareTo(sa1[1]) == 0);
	}

	@Test
	// ensures that the ellipsize function doesnt show half a word
	public void checkEllipsize() {
		String s1 = UC.ellipsize("Hello, this is a test", 13);
		String s2 = "Hello, this...";
		assertEquals(s2, s1);
	}

	@Test
	// ensures that the ellipsize function truncates fully if max length is 0
	public void checkEllipsizeZero() {
		String s1 = UC.ellipsize("Hello, this is a test", 0);
		String s2 = "";
		assertEquals(s2, s1);
	}

	@Test
	// ensures that the ellipsize function shows everything if length is longer
	// than word
	public void checkEllipsizeShowAll() {
		String s1 = UC.ellipsize("Hello, this is a test", 99);
		String s2 = "Hello, this is a test";
		assertEquals(s2, s1);
	}
}
