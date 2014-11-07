package logic.google;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import logic.LogicController;
import logic.command.Command;

public class SaveCache extends Command {

	@Override
	public boolean executeCommand() {
		try {
			FileWriter fstream = new FileWriter("cache.txt", true);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			for(Map.Entry e : LogicController.cacheMap.entrySet()){
				bufferedWriter.write(e.toString()+"\r\n");
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
