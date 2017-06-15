package com.cooksys.assessment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.jws.soap.SOAPBinding.Use;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
	
	private int port;
	private ExecutorService executor;
	// Create a Set to hold the client array
	private final Set<ClientHandler> active = Collections.synchronizedSet(new HashSet<ClientHandler>());
	public synchronized Set<ClientHandler> getConnections() {
		return active;
	}
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}
	
	
	public void run() {
		log.info("server started");
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket, this);
				executor.execute(handler);
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	// Send whisper to specified client
	public void sendWhisper(Message message, String c) throws IOException {
		for(ClientHandler h : active){
			if(h.getUsername().equals(c)){
				h.getReply(message);
			}
		}
		
	}
	// use synchronized methods so that data is consistent
	public synchronized void removeClient(ClientHandler t){
		active.remove(t);
	}
	
	public synchronized void addClient(ClientHandler t){
		active.add(t);
	}
	// get a list of all the users that are logged in
	public String getUsers(){
		String userList = "";
		for(ClientHandler c : active){
			userList = userList + c.getUsername() + "\n";
		}
		return userList;
	}
	// Send message to all users
	public void broadcastSend(Message m) throws IOException{
		for(ClientHandler c : active){
			c.getReply(m);
		}
	}
	// Check for duplicate user name
	public boolean checkDupe(String name){
		for(ClientHandler c : active){
			if(c.getUsername().equals(name))
				return true;
		}
		return false;
	}
}