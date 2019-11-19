package socket4;

//a majority of this code comes from https://github.com/MattToegel/IT114/blob/master/Examples/SocketsPart4/src/ServerThread.java

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;


//Class to hold client connection and prevent it from blocking the main thread of the server
public class ServerThread extends Thread{
	private Socket client;//server's reference to the connect client
	private String clientName;//this connected client's name
	//Object streams that let us pass objects
	private ObjectInputStream in;//from client
	private ObjectOutputStream out;//to client
	private boolean isRunning = false;//boolean to control termination
	socketServer server;
	public ServerThread(Socket myClient, String clientName, socketServer server) throws IOException {
		this.client = myClient;
		this.clientName = clientName;
		this.server = server;
		isRunning = true;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
		System.out.println("Spawned thread for client " + clientName);
	}
	@Override
	public void run() {
		try{
			Payload fromClient;
			//if disconnected in.readObject will throw an EOFException
			while(isRunning 
					&& !client.isClosed() 
					&& (fromClient = (Payload)in.readObject()) != null) {
				processPayload(fromClient);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Terminating client");
		}
		finally {
			System.out.println("Server cleaning up IO for " + clientName);
			stopThread();
			cleanup();
		}
	}
	public String getClientName() {
		return this.clientName;
	}
	void processPayload(Payload payload) throws IOException {
		System.out.println("Received: " + payload);
		switch(payload.payloadType) {
			case MESSAGE:
				Payload toClient = new Payload(PayloadType.MESSAGE,clientName + ": " + payload.message);
				System.out.println("Sending: " + toClient.toString());
				server.broadcast(toClient);
			break;
			case DISCONNECT:
				System.out.println("Removing client " + clientName);
				server.removeClient(this);
				stopThread();
			break;
			case ROLL_IT:
				Random random = new Random();
				int roll = random.nextInt(7);
				System.out.println("You rolled: " + roll);
				send(new Payload(PayloadType.ROLL_IT, "You rolled " + roll));
				server.broadcast(new Payload(PayloadType.ROLL_IT, "Client " + clientName + "rolled " + roll));
			break;
			default:
				break;
		}
	}
	public void stopThread() {
		isRunning = false;
	}
	/***
	 * Returns true if we lost connection to our client
	 * @return
	 */
	public boolean isClosed() {
		return client.isClosed();
	}
	public void send(Payload p) {
		try {
			out.writeObject(p);
		} catch (IOException e) {
			System.out.println("Error sending payload to client");
			e.printStackTrace();
			cleanup();
		}
	}
	private void cleanup() {
		if(in != null) {
			try{in.close();}
			catch(Exception e) { System.out.println("Input already closed");}
		}
		if(out != null) {
			try {out.close();}
			catch(Exception e) {System.out.println("Output already closed");}
		}
		//most likely not necessary since should all be closed already
		if(client != null && !client.isClosed()) {
			//try close input
			try {client.shutdownInput();} 
			catch (IOException e) {System.out.println("Socket/Input already closed");}
			//try close output
			try {client.shutdownOutput();}
			catch (IOException e) {System.out.println("Socket/Output already closed");}
			//try close socket
			try {client.close();}
			catch (IOException e) {System.out.println("Socket already closed");}
		}
		System.out.println("Client " + clientName + " has been cleaned up");
	}
}