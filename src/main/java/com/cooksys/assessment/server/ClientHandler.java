package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private Server server;
	private String username;


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public ClientHandler(Socket socket, Server server) {
		super();
		this.socket = socket;
		this.server = server;
	}
	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				switch (message.getCommand()) {
					case "connect":
						// Checks to see if the username already exists and rejects it if so
						if(server.checkDupe(message.getUsername())){
							message.setContents("dupe");
							String dupe = mapper.writeValueAsString(message);
							writer.write(dupe);
							writer.flush();
							this.socket.close();
							// If client username isn't in use, add a new client thread
						} else {
						server.addClient(this);
						createTimestamp(message);
						setUsername(message.getUsername());
						server.broadcastSend(message);
						}
						break;
						// Happens when client types disconnect
					case "disconnect":
						server.removeClient(this);
						createTimestamp(message);
						server.broadcastSend(message);
						this.socket.close();
						break;
						// Happens when client types echo
					case "echo":
						createTimestamp(message);
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
						// Broadcast message to all logged on users
					case "broadcast":
						createTimestamp(message);
						server.broadcastSend(message);
						break;
						// Get a list of users
					case "users":
						message.setContents(server.getUsers());
						createTimestamp(message);
						String users = mapper.writeValueAsString(message);
						writer.write(users);
						writer.flush();
						break;
					default:
						// Issues whisper command 
						if(message.getCommand().matches("@(.*)")){
							createTimestamp(message);
							String command = message.getCommand().replaceAll("[^\\w\\s]", "");
							server.sendWhisper(message, command);
							String self = mapper.writeValueAsString(message);
							writer.write(self);
							writer.flush();
						}
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}	
	// Retrieve a message from the CLI and send it to the intended party/parties
	public void getReply(Message m) throws IOException {
		ObjectMapper br = new ObjectMapper();
		PrintWriter broad = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		String response = br.writeValueAsString(m);
		broad.write(response);
		broad.flush();
	}
	
	// Create the timestamp object to append to  the message
	public void createTimestamp(Message m){
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		m.setTimestamp(timeStamp);
	}
	
}