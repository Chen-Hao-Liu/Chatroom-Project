package chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Chen Hao Liu
 * Type /name myName to change name 
 * Similarly, type /join myChatroom to join/create a new chatroom
 */
public class ChatServer extends ChatWindow {

	private ClientHandler handler;
	//ArrayList of clientHandler made to assist the server with reaching all clients
	private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

	public ChatServer(){
		super();
		this.setTitle("Chat Server");
		this.setLocation(80,80);

		try {
			// Create a listening service for connections
			// at the designated port number.
			ServerSocket srv = new ServerSocket(2113);

			ArrayList<Thread> clientList = new ArrayList<Thread>();

			printMsg("/join <Chatroom> to create/join new chatroom!");
			printMsg("/name <name> to change username!");

			while (true) {
				// The method accept() blocks until a client connects.
				printMsg("Waiting for a connection");
				Socket socket = srv.accept();

				//New client is instantiated and added to list
				ClientHandler newClient = new ClientHandler(socket);
				clients.add(newClient);
				//New thread is created for the client in order to accomodate for multiple clients
				Thread myClient = new Thread(newClient);
				clientList.add(myClient);
				//Start the thread
				myClient.start();
			}

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/** This innter class handles communication to/from one client. */
	class ClientHandler implements Runnable{
		public String chatroom = "Default";
		private PrintWriter writer;
		private BufferedReader reader;

		public ClientHandler(Socket socket) {
			try {
				InetAddress serverIP = socket.getInetAddress();
				printMsg("Connection made to " + serverIP);
				writer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}
			catch (IOException e){
				printMsg("\nERROR:" + e.getLocalizedMessage() + "\n");
			}
		}
		
		public void handleConnection() {
			try {
				while(true) {
					// read a message from the client
					readMsg();
				}
			}
			catch (IOException e){
				printMsg("\nERROR:" + e.getLocalizedMessage() + "\n");
			}
		}

		/** Receive and display a message */
		public void readMsg() throws IOException {
			//Read a message received from client
			String s = reader.readLine();
			boolean joinChat = false;

			//Split string
			String[] strArr = s.split(" ");
			//Detect if join chat command has been entered
			if(strArr.length == 3){
				//Command detected
				if(strArr[1].equals("/join")){
					//Set previous chat name
					String prevChat = chatroom;
					//Change chatroom
					chatroom = strArr[2];
					//Show on server
					printMsg(strArr[0].substring(0,strArr[0].length()-1) + " joined " + chatroom);
					//Echo the message to the other clients in the same chat
					for(int i=0; i<clients.size(); i++){
						if(clients.get(i).chatroom.equals(chatroom)){
							clients.get(i).sendMsg(strArr[0].substring(0,strArr[0].length()-1) + " joined " + chatroom);
						}else if(clients.get(i).chatroom.equals(prevChat)){
							clients.get(i).sendMsg(strArr[0].substring(0,strArr[0].length()-1) + " has left " + prevChat);	
						}
					}
					//Do not print further lines
					joinChat = true;
				}
			}

			if(!joinChat){
				printMsg("[" + chatroom + "]" + s);
				//Echo the message to the other clients
				for(int i=0; i<clients.size(); i++){
					if(clients.get(i).chatroom.equals(chatroom)){
						clients.get(i).sendMsg("[" + chatroom + "]" + s);
					}
				}
			}
		}
		/** Send a string */
		public void sendMsg(String s){
			writer.println(s);
		}

		/** Run the client*/
		public void run(){
			//First handle connection
			handleConnection();
			System.out.println("Client is running");
		}

	}

	public static void main(String args[]){
		new ChatServer();
	}
}
