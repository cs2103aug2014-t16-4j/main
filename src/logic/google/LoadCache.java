package logic.google;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import logic.LogicController;
import logic.command.Command;

public class LoadCache extends Command{
	
	@Override
	public boolean executeCommand() {
		String line;
		JSONParser jsonParser = new JSONParser();
		try {
			BufferedReader in = new BufferedReader(new FileReader("cache.txt"));
			while ((line = in.readLine()) != null) {
				String[] splittedStr = line.split("=");
				String key = splittedStr[0];
				String value = splittedStr[1];
				
				JSONObject obj = (JSONObject) jsonParser.parse(line);
			}
			in.close();
		} catch (FileNotFoundException | ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean undo() {
		// TODO Auto-generated method stub
		return false;
	}

}
