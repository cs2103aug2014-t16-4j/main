package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import org.json.simple.JSONObject;

import model.Task;

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
import com.google.api.services.calendar.Calendar.CalendarList;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class GoogleCal {
	GoogleAuthorizationCodeFlow flow;
	String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
	String appName = "TaskBox";
	HttpTransport httpTransport;
	JacksonFactory jsonFactory;
	private static Calendar client;
	
	public static boolean isOnline(){
		Socket sock = new Socket();
		InetSocketAddress addr = new InetSocketAddress("www.google.com",80);
		try{
			sock.connect(addr);
			return true;
		}catch(IOException e){
			System.err.println("User is offline.");
			e.printStackTrace();
			return false;
		}finally{
			try {
				sock.close();
			}catch(IOException e){}
		}
	}
	public void setUp() {

	}

	public String getURL() {
		httpTransport = new NetHttpTransport();
		jsonFactory = new JacksonFactory();
		String appName = "TaskBox";
		String clientId = "743259209106-g4qtcmneg0dhi9efos04d46bnnjiiich.apps.googleusercontent.com";
		String clientSecret = "AyJjPfMtT0gQPki-eArk4xKG";
		//String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";

		flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, clientId, clientSecret,
				Arrays.asList(CalendarScopes.CALENDAR)).build();

		String url = flow.newAuthorizationUrl().setRedirectUri(redirectUrl)
				.build();
		
		return url;
		/*
		System.out.println("Please open the following URL in your browser then type the authorization code:");
		System.out.println("  " + url);
		System.out.println("What is the authorization code?");
		String code = new Scanner(System.in).nextLine();

		AuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code)
				.setRedirectUri(redirectUrl);
		TokenResponse tokenRes = null;
		try {
			tokenRes = tokenRequest.execute();
		} catch (IOException e) {
			System.err.println("Token request failed");
		}
		if (tokenRes != null) {
			writeFile(tokenRes.getAccessToken());
			return tokenRes.getAccessToken();
		} else {
			return "Couldn't get token";
		}
		*/
	}
	
	public boolean withExistingToken(){
		TokenResponse tokenRes = new TokenResponse();
		if(validFile()){
			tokenRes.setAccessToken(readFile());
			try{
				Credential credential = flow.createAndStoreCredential(tokenRes, appName);
				HttpRequestInitializer initializer = credential;
				Calendar.Builder builder = new Calendar.Builder(httpTransport, jsonFactory,initializer);
				builder.setApplicationName(appName);
				client = builder.build();
				return true;
			}catch(Exception e){
				return false;
			}
		}else{
			return false;
		}
	}
	
	public boolean generateNewToken(String code) throws IOException{
		TokenResponse tokenRes = new TokenResponse();
		try{
			if(validFile()){
				tokenRes.setAccessToken(readFile());
			}
		}catch(Exception e){
			AuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code).setRedirectUri(redirectUrl);
			tokenRes = tokenRequest.execute();
			writeFile(tokenRes.getAccessToken());
			Credential credential = flow.createAndStoreCredential(tokenRes, appName);
			HttpRequestInitializer initializer = credential;
			Calendar.Builder builder = new Calendar.Builder(httpTransport, jsonFactory,initializer);
			builder.setApplicationName(appName);
			client = builder.build();
			return true;
		}
		return false;
	}
	
	public String syncGCal() throws IOException{
		TokenResponse tokenRes = new TokenResponse();
		//try {
		//	if(validFile()){
		//		tokenRes.setAccessToken(readFile());
		//	}else{
				//AuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code).setRedirectUri(redirectUrl);
				//tokenRes = tokenRequest.execute();
	//		}
	//	} catch (IOException e) {
	//		e.printStackTrace();
	//		System.err.println("Token request failed");
	//	}
		if (tokenRes != null) {
			com.google.api.services.calendar.model.Calendar calendar = client.calendars().get("primary").execute();
			System.out.println(calendar.getSummary());
			//for(JSONObject i:LogicController.tasksBuffer){
			//	createEvent(Converter.jsonToTask(i));
			//}
			return Consts.STRING_SYNC_COMPLETE;
		} else {
			return Consts.STRING_SYNC_NOT_COMPLETE;
		}
	}

	public String createEvent(Task tsk) throws IOException{
		Event event= new Event();
		event.setSummary(tsk.getName());
		event.setDescription(tsk.getDescription());
		event.setStart(new EventDateTime().setDateTime(new DateTime(tsk.getStartDate())));
		event.setEnd(new EventDateTime().setDateTime(new DateTime(tsk.getEndDate())));
		client.events().insert("primary", event).execute();
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
	
	public static void clearFile(){
		try {
			FileWriter fstream = new FileWriter("GoogleToken", false);
			BufferedWriter bufferedWriter = new BufferedWriter(fstream);
			bufferedWriter.write("");
			bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String readFile(){
		String token = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader("GoogleToken"));
			token = in.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Please generate the token again");
		}
		return token;
	}
	
	public static boolean validFile(){
		File f = new File("GoogleToken");
		if(f.exists()){
			if(readFile().equals("")){
				return false;
			}else{
				return true;
			}
		}else{
			return false;
		}
	}
}
