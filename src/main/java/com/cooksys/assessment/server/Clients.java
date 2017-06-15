package com.cooksys.assessment.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Clients {


	private static List<String> userList; 
	private static List<Socket> sockList;
	
	public Clients() {
		userList = Collections.synchronizedList(new ArrayList<String>());
		sockList = Collections.synchronizedList(new ArrayList<Socket>());
	}

	public synchronized static void addUser(String m, Socket s) {
		userList.add(m);
		sockList.add(s);
	}
	
	public synchronized static void removeUser(String m) {
		userList.remove(userList.indexOf(m));
		sockList.remove(m);
	}	
	
	public synchronized static List<String> getUsers() {
		return userList;
	}
	
	public synchronized static List<Socket> getSockets() {
		return sockList;
	}

}
