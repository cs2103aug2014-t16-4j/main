package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
	GoogleAuthorizationCodeFlow flow;
	String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
	String appName = "TaskBox";
	HttpTransport httpTransport;
	JacksonFactory jsonFactory;
	private static Calendar client;
	
	public GoogleCal() {
		httpTransport = new NetHttpTransport();
		jsonFactory = new JacksonFactory();
		String appName = "TaskBox";
		String clientId = "743259209106-g4qtcmneg0dhi9efos04d46bnnjiiich.apps.googleusercontent.com";
		String clientSecret = "AyJjPfMtT0gQPki-eArk4xKG";
		// String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";

		flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
				jsonFactory, clientId, clientSecret,
				Arrays.asList(CalendarScopes.CALENDAR)).build();
	}
	
	public static boolean isOnline() {
		Socket sock = new Socket();
		InetSocketAddress addr = new InetSocketAddress("www.google.com", 80);
		try {
			sock.connect(addr);
			return true;
		} catch (IOException e) {
			System.err.println("User is offline.");
			e.printStackTrace();
			return false;
		} finally {
			try {
				sock.close();
			} catch (IOException e) {
			}
		}
	}

	public String getURL() {
		String url = flow.newAuthorizationUrl().setRedirectUri(redirectUrl)
				.build();

		return url;
	}

	public boolean withExistingToken() {
		TokenResponse tokenRes = new TokenResponse();
		if (validFile()) {
			System.out.println(readFile());
			tokenRes.setAccessToken(readFile());
			try {
				Credential credential = flow.createAndStoreCredential(tokenRes,
						appName);
				HttpRequestInitializer initializer = credential;
				Calendar.Builder builder = new Calendar.Builder(httpTransport,
						jsonFactory, initializer);
				builder.setApplicationName(appName);
				client = builder.build();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean generateNewToken(String code) throws IOException {
		try {
			TokenResponse tokenRes = new TokenResponse();
			AuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code)
					.setRedirectUri(redirectUrl);
			tokenRes = tokenRequest.execute();
			writeFile(tokenRes.getAccessToken());
			Credential credential = flow
					.createAndStoreCredential(tokenRes, appName);
			HttpRequestInitializer initializer = credential;
			Calendar.Builder builder = new Calendar.Builder(httpTransport,
					jsonFactory, initializer);
			builder.setApplicationName(appName);
			client = builder.build();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String syncGCal(ArrayList<JSONObject> timeTasks) throws IOException {
			com.google.api.services.calendar.model.Calendar calendar = client
					.calendars().get("primary").execute();
			//System.out.println(calendar.getId());
			Calendar.Events.List request = client.events().list("primary");
			List<Event> events = request.execute().getItems();
			System.out.println(events.toString());
			/*
			for(int i=0;i<timeTasks.size();i++){
				for(Event event:events){
					if(Converter.jsonToTask(timeTasks.get(i)).getName().equals(event.getSummary())){
						//System.out.println("Found it!" + event.getId());
						client.events().delete("primary", event.getId()).execute();
						System.out.println(event.getId() + " is deleted");
					}
				}
				
			}
			*/
			for(Event event:events){
				client.events().delete("primary", event.getId()).execute();
				System.out.println(event.getId() + " is deleted");
			}
			for(JSONObject obj:timeTasks){
				createEvent(Converter.jsonToTask(obj),calendar.getId());
			}
			return Consts.STRING_SYNC_COMPLETE;
	}

	public String createEvent(Task tsk,String calId) throws IOException {
		Event event = new Event();
		event.setSummary(tsk.getName());
		event.setDescription(tsk.getDescription());
		event.setStart(new EventDateTime().setDateTime(new DateTime(tsk
				.getStartDate())));
		event.setEnd(new EventDateTime().setDateTime(new DateTime(tsk
				.getEndDate())));
		client.events().insert(calId, event).execute();
		return event.getId();
	}

	public static boolean writeFile(String token) {
		try {
			FileWriter fstream = new FileWriter("GoogleToken", false);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			bufferedWriter.write(token);
			bufferedWriter.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void clearFile() {
		try {
			FileWriter fstream = new FileWriter("GoogleToken", false);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			bufferedWriter.write("");
			bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readFile() {
		String token = "";
		try {
			BufferedReader in = new BufferedReader(
					new FileReader("GoogleToken"));
			token = in.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Please generate the token again");
		}
		return token;
	}

	public static boolean validFile() {
		File f = new File("GoogleToken");
		if (f.exists()) {
			if (readFile().equals("")) {
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
		for (JSONObject jTask: LogicController.tasksBuffer) {
			if (Converter.jsonToTask(jTask).getStatus() == Consts.STATUS_TIMED_TASK) {
				//System.out.println(jTask);// For Debuging
				displayTasksBuffer.add(jTask);
			}
		}
		return displayTasksBuffer;
	}
}
