package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				switch (message.getCommand()) {
					case "connect":
						log.info(timeStamp, "user <{}> connected", message.getUsername());
						try {
						Thread.currentThread().setName(message.getUsername());
						log.info(Thread.currentThread().getName());
						}
						catch(Exception ex) {
							log.info("Username is already in use... try another.");
						}
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "all":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String brodcast = mapper.writeValueAsString(message);
						writer.write(brodcast);
						writer.flush();
						break;
					case "@":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String whisper = mapper.writeValueAsString(message);
						writer.write(whisper);
						writer.flush();
						break;
					case "users":
						Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String users = mapper.writeValueAsString(threadSet);
						writer.write(users);
						writer.flush();
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
