//@author A0117993R
package logic.google;

import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleCalService implements Runnable{

	private static Logger logger = Logger.getLogger("GoogleCalService");
	GoogleCal gCal = new GoogleCal();
	boolean flag = true;
	@Override
	public void run() {
		while(flag){
			logger.log(Level.INFO,"Google sync");
			if (GoogleCal.isOnline()) {
				if (gCal.withExistingToken()) {
					try {
						gCal.syncGCalService(gCal.getTimedTasksBuffer());
					} catch (IOException | ParseException e) {
						logger.log(Level.INFO,"Existing token doesn't work.");
						System.err.println(e.getMessage());
					}
				}else{
					logger.log(Level.INFO,"Token file doesn't exist.");
				}
			}
			try{
				Thread.sleep(10000); // 10 seconds 
			}catch(InterruptedException e){
				System.err.println(e.getMessage());
			}
		}
	}
	

}
