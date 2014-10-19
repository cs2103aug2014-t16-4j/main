package logic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class GoogleCal {

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
}
