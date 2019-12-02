package socket4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class socketServer {
	int port = -1;
	List<ServerThread> clients = new ArrayList<ServerThread>();
	public static boolean running = true;
	public socketServer() {
		running = true;
	}
	
	public synchronized void broadcast(Payload payload) {
		System.out.println("Sending message to " + clients.size() + " clients");
		for(int i = 0; i < clients.size(); i++) {
			clients.get(i).send(payload);
		}
	}
	
	public void removeClient(ServerThread client) {
		Iterator<ServerThread> it = clients.iterator();
		while(it.hasNext()) {
			ServerThread st = it.next();
			if(st == client) {
				System.out.println("Matched client");
				it.remove();
			}
		}
	}
	
	void cleanupClients() {
		if(clients.size() == 0) {
			return;
		}
		Iterator<ServerThread> it = clients.iterator();
		System.out.println("Start Cleanup count " + clients.size());
		while(it.hasNext()) {
			ServerThread s = it.next();
			if(s.isClosed()) {
				broadcast(new Payload(PayloadType.DISCONNECT, null));
				s.stopThread();
				it.remove();
			}
		}
		System.out.println("End Cleanup count " + clients.size());
	}
	
	public synchronized void sendToClientByIndex(int index, Payload payload) {
		clients.get(index).send(payload);
	}
	
	void runCleanupThread() {
		Thread cleanupThread = new Thread() {
			@Override
			public void run() {
				while(socketServer.running) {
					//cleanupClients();
					try {
						Thread.sleep(1000*30);//30 seconds
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("Cleanup thread has been exited");
			}
		};
		cleanupThread.start();
	}
	
	
	public void startServer(int port) {
		this.port = port;
		System.out.println("Waiting for client...");
		runCleanupThread();
		try (ServerSocket serverSocket = new ServerSocket(port);){
			while(socketServer.running) {
				try {
					//if(clients.size() > 3) {
						//System.out.println("Too many players, can't connect");
					//}
					//else {
					Socket theClient = serverSocket.accept();
					System.out.println("Client has connected.");
					ServerThread thread = new ServerThread(theClient, "Client[" + clients.size() + "]", this);
					thread.start();
					clients.add(thread);
					//}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				running = false;
				Thread.sleep(50);
				System.out.println("Server socket is closing.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
				
	}
		
	public static void main(String[] arg) {
		System.out.println("Starting Server");
		socketServer server = new socketServer();
		int port = -1;
		if(arg.length > 0){
			try{
				port = Integer.parseInt(arg[0]);
			}
			catch(Exception e){
				System.out.println("Invalid port: " + arg[0]);
			}		
		}
		if(port > -1){
			System.out.println("Server is listening on port " + port);
			server.startServer(port);
		}
		System.out.println("Server Stopped");
	}

}