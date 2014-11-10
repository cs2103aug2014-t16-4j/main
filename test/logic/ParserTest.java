//@author A0112069M
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
				Converter.taskToJSON(parser.decompose("a task day after today (some desc) important")).toString());
		assertEquals("{\"Name\":\"an impotant task\",\"Status\":1,\"Description\":\"some desc\",\"EndDate\":\"19\\/12\\/2014 23:59:59\",\"StartDate\":\"15\\/12\\/2014 00:00:00\",\"Frequency\":0,\"Priority\":0}",
				Converter.taskToJSON(parser.decompose("an impotant task 12/15/2014 to 12/19/2014 (some desc)")).toString());
		assertEquals("{\"Name\":\"a task\",\"Status\":1,\"Description\":\"\",\"EndDate\":\"10\\/11\\/2014 23:59:59\",\"StartDate\":\"10\\/11\\/2014 00:00:00\",\"Frequency\":1,\"Priority\":0}", 
				Converter.taskToJSON(parser.decompose("a task today daily")).toString());
		assertEquals("{\"Name\":\"a task\",\"Status\":1,\"Description\":\"\",\"EndDate\":\"13\\/11\\/2014 23:59:59\",\"StartDate\":\"13\\/11\\/2014 00:00:00\",\"Frequency\":2,\"Priority\":0}",
				Converter.taskToJSON(parser.decompose("a task on the day after day after tomorrow weekly")).toString());
	}

}
