package logic.google;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import logic.command.Command;

import org.json.simple.JSONObject;

public class SaveCache extends Command {
	Map<String,JSONObject> toSync;
	public SaveCache(Map<String,JSONObject> temp){
		this.toSync = temp;
	}

	@Override
	public boolean executeCommand() {
		try {
			FileWriter fstream = new FileWriter("cache.txt", true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			bufferedWriter.write(toSync.toString());
			bufferedWriter.close();
			return true;
		} catch (IOException e) {
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
