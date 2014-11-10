//@author A0117993R

package logic.google;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import logic.Consts;
import logic.Converter;
import logic.LogicController;
import model.Task;

import org.json.simple.JSONObject;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

public class GoogleCal {
	private static Calendar client;
	private static Logger logger = Logger.getLogger("GoogleCal");

	GoogleAuthorizationCodeFlow flow;
	String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
	String appName = "TaskBox";
	HttpTransport httpTransport;
	JacksonFactory jsonFactory;

	public GoogleCal() {
		httpTransport = new NetHttpTransport();
		jsonFactory = new JacksonFactory();
		String clientId = "743259209106-g4qtcmneg0dhi9efos04d46bnnjiiich.apps.googleusercontent.com";
		String clientSecret = "AyJjPfMtT0gQPki-eArk4xKG";

		flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,jsonFactory, clientId, clientSecret,Arrays.asList(CalendarScopes.CALENDAR)).build();
	}

	public static boolean isOnline() {
		Socket sock = new Socket();
		InetSocketAddress addr = new InetSocketAddress("www.google.com", 80); // Can ping to google?
		try {
			sock.connect(addr);
			return true;
		} catch (IOException e) {
			System.err.println("User is offline.\n"+e.getMessage());
			return false;
		} finally {
			try {
				sock.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	public String getURL() {
		String url = flow.newAuthorizationUrl().setRedirectUri(redirectUrl).build();
		return url;
	}

	public boolean withExistingToken() {
		TokenResponse tokenRes = new TokenResponse();
		if (validFile()) {
			logger.log(Level.CONFIG,readFile(Consts.GOOGLETOKEN));
			tokenRes.setAccessToken(readFile(Consts.GOOGLETOKEN));
			try {
				Credential credential = flow.createAndStoreCredential(tokenRes,appName);
				HttpRequestInitializer initializer = credential;
				Calendar.Builder builder = new Calendar.Builder(httpTransport,jsonFactory, initializer);
				builder.setApplicationName(appName);
				client = builder.build();
				return true;
			} catch (Exception e) {
				System.err.println(e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean generateNewToken(String code) throws IOException {
		try {
			TokenResponse tokenRes = new TokenResponse();
			AuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code).setRedirectUri(redirectUrl);
			tokenRes = tokenRequest.execute();
			writeFile(Consts.GOOGLETOKEN,tokenRes.getAccessToken(),false);
			Credential credential = flow.createAndStoreCredential(tokenRes,appName);
			HttpRequestInitializer initializer = credential;
			Calendar.Builder builder = new Calendar.Builder(httpTransport,jsonFactory,initializer);
			builder.setApplicationName(appName);
			client = builder.build();
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	// Service method running in separate thread
	public void syncGCalService(ArrayList<JSONObject> timeTasks) throws IOException, ParseException {
		String pageToken = null;
		String str = "";
		do {
			Events events = client.events().list("primary").setPageToken(pageToken).execute();
			List<Event> items = events.getItems();
			JSONObject toDel = null;
			if (pageToken != null) {
				if(items.size()>timeTasks.size()){
					for(Event event:items){
						System.out.println(event.toPrettyString());
						boolean found = false;
						for(int i=0;i<timeTasks.size();i++){
							if(event.getSummary().equals(Converter.jsonToTask(timeTasks.get(i)).getName())){
								found = true;
							}
						}
						if(!found){
							logger.log(Level.INFO,"Not match found: " + event.getSummary());
							str += Converter.eventToJSON(event).toString() + "\r\n";
							LogicController.tasksBuffer.add(Converter.eventToJSON(event));
							logger.log(Level.INFO,"Writing to file " + event.getSummary());
						}
					}
				}else{
					for(int i=0;i<timeTasks.size();i++){
						boolean found = false;
						for(Event event:items){
							if(Converter.jsonToTask(timeTasks.get(i)).getName().equals(event.getSummary())){
								found = true;
							}
						}
						if(found){
							str += timeTasks.get(i).toString() + "\r\n";
						}else{
							logger.log(Level.INFO,"Removing task: " + timeTasks.get(i).get(Consts.NAME));
							LogicController.tasksBuffer.remove(timeTasks.get(i));
						}
					}
				}
			}
			pageToken = events.getNextPageToken();
			//System.out.println(pageToken);
		} while (pageToken != null);
		assert (!str.isEmpty());
		writeFile(LogicController.fileName, str, false);
	}

	public String syncGCal(ArrayList<JSONObject> timeTasks) throws IOException {
		String pageToken = null;
		do {
			Events events = client.events().list("primary").setPageToken(pageToken).execute();
			List<Event> items = events.getItems();
			if(pageToken!=null) {
				for (int i = 0; i < timeTasks.size(); i++) {
					boolean found = false;
					for (Event event : items) {
						//System.out.println(event.toPrettyString());
						if (Converter.jsonToTask(timeTasks.get(i)).getName().equals(event.getSummary())) {
							found = true;
						}
					}
					if(!found){
						createEvent(Converter.jsonToTask(timeTasks.get(i)),"primary");
						logger.log(Level.INFO,"Creating event.. " + timeTasks.get(i));
					}
				}
			}
			pageToken = events.getNextPageToken();
		} while (pageToken != null);
		return Consts.STRING_SYNC_COMPLETE;
	}

	public String createEvent(Task tsk, String calId) throws IOException {
		Event event = new Event();
		event.setSummary(tsk.getName());
		event.setDescription(tsk.getDescription());
		event.setStart(new EventDateTime().setDateTime(new DateTime(tsk.getStartDate())));
		event.setEnd(new EventDateTime().setDateTime(new DateTime(tsk.getEndDate())));
		event.setSequence(tsk.getPriority());
		client.events().insert(calId, event).execute();
		return event.getId();
	}
	
	public void deleteAllEntries() throws IOException{
		String pageToken = null;
		do{
			Events events = client.events().list("primary").setPageToken(pageToken).execute();
			List<Event> items = events.getItems();
			for(Event event:items){
				logger.log(Level.INFO,"Deleting " + event.getId());
				client.events().delete("primary", event.getId()).execute();
			}
			pageToken = events.getNextPageToken();
		}while(pageToken != null);
	}

	public boolean deleteEvent(String name) throws IOException{
		String pageToken = null;
		do{
			Events events = client.events().list("primary").setPageToken(pageToken).execute();
			List<Event> items = events.getItems();
			for(Event event:items){
				if(event.getSummary().equals(name)){
					logger.log(Level.INFO,"Deleting " + event.getId());
					client.events().delete("primary", event.getId()).execute();
					return true;
				}
			}
			pageToken = events.getNextPageToken();
		}while(pageToken != null);
		return false;
	}

	public static boolean writeFile(String fileName,String str,boolean flag) {
		try {
			FileWriter fstream = new FileWriter(fileName, flag);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			bufferedWriter.write(str);
			bufferedWriter.close();
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	public static void clearFile() {
		try {
			FileWriter fstream = new FileWriter(Consts.GOOGLETOKEN, false);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			bufferedWriter.write("");
			bufferedWriter.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static String readFile(String fileName) {
		String token = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			token = in.readLine();
		} catch (Exception e) {
			System.err.println("Please generate the token again " + e.getMessage());
		}
		return token;
	}

	public static boolean validFile() {
		File f = new File(Consts.GOOGLETOKEN);
		if (f.exists()) {
			if (readFile(Consts.GOOGLETOKEN).equals("")) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public ArrayList<JSONObject> getTimedTasksBuffer() {
		ArrayList<JSONObject> displayTasksBuffer = new ArrayList<JSONObject>();
		for (JSONObject jTask : LogicController.tasksBuffer) {
			if (Converter.jsonToTask(jTask).getStatus() == Consts.STATUS_TIMED_TASK) {
				// System.out.println(jTask);// For Debuging
				displayTasksBuffer.add(jTask);
			}
		}
		return displayTasksBuffer;
	}
}
