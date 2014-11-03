package logic;

import java.io.IOException;

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
						gCal.syncGCal(gCal.getTimedTasksBuffer());
					} catch (IOException e) {
					}
				}
			}
			try{
				Thread.sleep(10000);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	

}
