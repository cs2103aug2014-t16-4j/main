package logic.google;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import logic.command.Command;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SaveCache extends Command {
	ArrayList<JSONObject> tasksToSync;
	public SaveCache(ArrayList<JSONObject> temp){
		this.tasksToSync = temp;
	}
	@Override
	public boolean executeCommand() {
		try {
			ArrayList<JSONObject> tasks = new ArrayList<JSONObject>();
			JSONParser jsonParser = new JSONParser();
			String line;
			try {
				BufferedReader in = new BufferedReader(new FileReader("cache.txt"));
				while ((line = in.readLine()) != null) {
					JSONObject obj = (JSONObject) jsonParser.parse(line);
					tasks.add(obj);
				}
				in.close();
			} catch (FileNotFoundException | ParseException e) {
				e.printStackTrace();
			}	
			
			FileWriter fstream = new FileWriter("cache.txt", true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			for(int i=0;i<tasksToSync.size();i++){
				if(!tasks.contains(tasksToSync.get(i))){
					bufferedWriter.write(tasksToSync.get(i).toString() + "\r\n");
				}
			}
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
