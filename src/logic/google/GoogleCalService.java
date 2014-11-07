package logic.google;

import java.io.IOException;
import java.text.ParseException;



public class GoogleCalService implements Runnable{

	GoogleCal gCal = new GoogleCal();
	boolean flag = true;
	@Override
	public void run() {
		while(flag){
			System.out.println("Google sync");
			if (GoogleCal.isOnline()) {
				if (gCal.withExistingToken()) {
					try {
						gCal.syncGCalService(gCal.getTimedTasksBuffer());
					} catch (IOException | ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			try{
				Thread.sleep(20000);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	

}
