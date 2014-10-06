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
		String sTest = "abc january to february";
		System.out.println(parser.parse(sTest).get(0).getDates().size());
		if (parser.parse(sTest).isEmpty() == false) {
			List<Date> dates = parser.parse(sTest).get(0).getDates();
			System.out.println(dates.get(0).toString());
			System.out.println(dates.get(1).toString());
		}
	}

}
