//@author A0117993R
package logic.google;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import logic.Consts;
import logic.Converter;
import logic.LogicController;
import logic.command.Command;

import org.json.simple.JSONObject;

public class SaveCache extends Command {

	@SuppressWarnings("unchecked")
	@Override
	public boolean executeCommand() {
		try {
			FileWriter fstream = new FileWriter(Consts.CACHE, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			for(Map.Entry e : LogicController.cacheMap.entrySet()){
				String entryStr = e.toString();
				String [] splittedStr = entryStr.split("=");
				bufferedWriter.write(splittedStr[0]+"="+Converter.jsonListToString((List<JSONObject>)e.getValue())+"\r\n");
			}
			bufferedWriter.close();
			return true;
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return false;
	}

	@Override
	public boolean undo() {
		// TODO Auto-generated method stub
		return false;
	}

}
