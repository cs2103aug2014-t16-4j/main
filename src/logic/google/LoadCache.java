package logic.google;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import logic.Consts;
import logic.Converter;
import logic.LogicController;
import logic.command.Command;

import org.json.simple.parser.ParseException;

public class LoadCache extends Command{
	
	@Override
	public boolean executeCommand() {
		String line = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(Consts.CACHE));
			while ((line = in.readLine()) != null) {
				String [] splittedStr = line.split("=");
				String key = splittedStr[0];
				LogicController.cacheMap.put(key, Converter.stringToJSONList(splittedStr[1]));
			}
			in.close();
			System.out.println(line);
			return line == null ? true:false;
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean undo() {
		// TODO Auto-generated method stub
		return false;
	}

}
