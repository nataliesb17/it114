package socket4;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


public class socketClient {
	Socket server;
	static ObjectOutputStream out;
	public Queue<String> messages = new LinkedList<String>();
	public void connect(String address, int port) {
		try {
			server = new Socket(address, port);
			System.out.println("Client has connected");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendRoll() {
		try {
			out.writeObject(new Payload(PayloadType.ROLL_IT, null));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() throws IOException {
		if(server == null) {
			return;
		}
		System.out.println("Client has Started");
		try(Scanner si = new Scanner(System.in);
				ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(server.getInputStream());){
			//Thread to listen for keyboard input so main thread isn't blocked
			Thread inputThread = new Thread() {
				@Override
				public void run() {
					try {
						while(!server.isClosed()) {
							System.out.println("Waiting for input");
							String line = si.nextLine();
							if(!"quit".equalsIgnoreCase(line) && line != null) {
								out.writeObject(new Payload(PayloadType.MESSAGE, line));
							}
							else {
								System.out.println("Stopping input thread");
								out.writeObject(new Payload(PayloadType.DISCONNECT, null));
								break;
							}
						}
					}
					catch(Exception e) {
						System.out.println("Client shutdown");
					}
					finally {
						close();
					}
				}
			};
			inputThread.start();
			
			Thread fromServerThread = new Thread() {
				@Override
				public void run() {
					try {
						Payload fromServer;
						while(!server.isClosed() && (fromServer = (Payload)in.readObject()) != null) {
							processPayload(fromServer);
						}
						System.out.println("Stopping server listen thread");
					}
					catch (Exception e) {
						if(!server.isClosed()) {
							e.printStackTrace();
							System.out.println("Server closed connection");
						}
						else {
							System.out.println("Connection closed");
						}
					}
					finally {
						close();
					}
				}
			};
			fromServerThread.start();//start the thread
			
			//Keep main thread alive until the socket is closed
			while(!server.isClosed()) {
				Thread.sleep(50);
			}
			System.out.println("Exited loop");
			System.exit(0);//force close
			//TODO implement cleaner closure when server stops before client
			//currently hangs/waits on the console/scanner input
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
	}
	
	void processPayload(Payload p) {
		switch(p.payloadType) {
		case CONNECT:
			System.out.println("A client connected");
			messages.add("A client connected");
			break;
		case DISCONNECT:
			System.out.println("A client disconnected");
			messages.add("A client disconnected");
			break;
		case MESSAGE:
			System.out.println("From Server: " + p.message);
			messages.add(p.message);
			break;
		default:
			System.out.println("We do not handle payloadType " + p.payloadType.toString());
			break;
		}
	}
	
	private void close() {
		if(server != null && !server.isClosed()) {
			try {
				server.close();
				System.out.println("Closed socket");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		socketClient client = new socketClient();
		int port = -1;
		try{
			//not safe but try-catch will get it
			port = Integer.parseInt(args[0]);
		}
		catch(Exception e){
			System.out.println("Invalid port");
		}
		if(port == -1){
			return;
		}
		client.connect("127.0.0.1", port);
		try {
			//if start is private, it's valid here since this main is part of the class
			client.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
