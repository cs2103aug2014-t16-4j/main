package logic;

import static org.junit.Assert.*;
import logic.Converter;
import logic.LogicParser;
import model.Task;

import org.junit.Test;

public class ParserTest {

	LogicParser parser = new LogicParser();

	@Test
	public void test() {
		assertEquals("{\"Name\":\"a task\",\"Status\":1,\"Description\":\"some desc\",\"EndDate\":\"11\\/11\\/2014 23:59:59\",\"StartDate\":\"11\\/11\\/2014 00:00:00\",\"Frequency\":0,\"Priority\":1}",
				Converter.taskToJSON(parser.decompose("a task day after today (some desc) important")));
		assertEquals("",
				Converter.taskToJSON(parser.decompose("an impotant task 12/15/2014 to 12/19/2014 (some desc)")));
		assertEquals("", 
				Converter.taskToJSON(parser.decompose("a task today daily")));
		assertEquals("",
				Converter.taskToJSON(parser.decompose("a task on the day after day after tomorrow weekly")));
	}

}
