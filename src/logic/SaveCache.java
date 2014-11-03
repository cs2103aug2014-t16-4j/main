package logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONObject;

public class SaveCache extends Command {
	ArrayList<JSONObject> tasksToSync;
	public SaveCache(ArrayList<JSONObject> temp){
		this.tasksToSync = temp;
	}
	@Override
	public boolean executeCommand() {
		try {
			FileWriter fstream = new FileWriter("cache.txt", true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			for(int i=0;i<tasksToSync.size();i++){
				bufferedWriter.write(tasksToSync.get(i).toString() + "\r\n");
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
