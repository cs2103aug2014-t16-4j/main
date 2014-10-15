package logic;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.util.AuthenticationException;

public class GoogleCal {
	private static final String URL = "https://www.google.com/calendar/feeds/default/private/full";
	private String appName = "";
	private String username,password;
	private CalendarService client;

	public GoogleCal(String appName){
		this.appName = appName;
	}
	
	public String authenticate(String username,String password){
		this.username = username;
		this.password = password;
		this.client = new CalendarService(appName);
		try {
			this.client.setUserCredentials(username, password);
			return Consts.STRING_CRE_CORRECT;
		} catch (AuthenticationException e) {
			return Consts.STRING_CRE_NOT_CORRECT;
		}
	}
	
}
