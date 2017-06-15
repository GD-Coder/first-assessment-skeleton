package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.core.net.server.ServerListener;

public class ClientHandler implements Runnable {
	
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	private String username;
	private Socket socket;
	Server server;
	
	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			PrintWriter textholder = null;
			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);

				switch (message.getCommand()) {
					case "connect":
						
						log.info("user <{}> connected", message.getUsername());
//						userlist.put(message.getUsername(), socket);
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						this.socket.close();
						break;
					case "all":
//						Socket apple = userlist.get(socket);
						log.info("user <{}> (all): <{}>", message.getUsername(), message.getContents());
						
						String brodcast = mapper.writeValueAsString(message);
						writer.write(brodcast);
						writer.flush();
						break;
					case "@*":
						log.info("user <{}> (all): <{}>", message.getUsername(), message.getContents());
						
						String whisper = mapper.writeValueAsString(message);
						writer.write(whisper);
						writer.flush();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
		
		
		
	}
	public void getReply(Message mess) throws IOException {
		ObjectMapper mapped = new ObjectMapper();
		PrintWriter broad = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		String reply = mapped.writeValueAsString(mess);
		broad.write(reply);
		broad.flush();
	}

}
