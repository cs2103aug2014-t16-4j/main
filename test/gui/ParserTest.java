package gui;
import com.joestelmach.natty.*;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ParserTest {

	Parser parser = new Parser();
	
	@Test
	public void test() {
		Parser parser = new Parser();
		List<Date> dates = parser.parse("the day before next thursday").get(0).getDates();
		System.out.println(dates.get(0).toString());
	}

}
