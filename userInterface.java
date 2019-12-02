package socket4;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class userInterface {
	public static boolean running = true;
	public static void main(String[] args) {
		JFrame frame = new JFrame("Trouble Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(500,500));
		panel.setLayout(new BorderLayout());
		JTextArea textSpot = new JTextArea();
		textSpot.setEditable(false);
		textSpot.setText("");
		
		JPanel area = new JPanel();
		area.setLayout(new BorderLayout());
		area.add(textSpot, BorderLayout.CENTER);
		area.setBorder(BorderFactory.createLineBorder(Color.CYAN));
		panel.add(area, BorderLayout.CENTER);
		JPanel input = new JPanel();
		
		Interaction interaction = new Interaction();
		Thread clientMessageReader = new Thread() {
			@Override
			public void run() {
				while(running && interaction.isClientConnected()) {
					String mes = interaction.getMessage();
					if(mes != null) {
						System.out.println("Got message " + mes);
						if(mes.indexOf("[name]") > -1) {
							String[] n = mes.split("]");
							frame.setTitle(frame.getTitle() + " - " + n[1]);
							
						}
						else {
							System.out.println("Appending to the text area");
							textSpot.append(mes + "\n");
						}
					}
					try {
						Thread.sleep(25);
					} catch (InterruptedException e ) {
						e.printStackTrace();
					}
				}
				System.out.println("Message reader thread has finished");
			}
			
		};
		
		JPanel connectionPanel = new JPanel();
		JTextField hostTextField = new JTextField();
		hostTextField.setText("127.0.0.1");
		JTextField portTextField = new JTextField();
		portTextField.setText("3200");
		JTextField errorTextField = new JTextField();
		errorTextField.setText("");
		JButton connect = new JButton();
		connect.setText("Connect");
		connect.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        String hostStr = hostTextField.getText();
		        String portString = portTextField.getText();
		        try {
		        	int port = Integer.parseInt(portString.trim());
		        	interaction.connect(hostStr, port, errorTextField);
		        	errorTextField.setText("Success!");
		        	connectionPanel.setVisible(false);
		        	clientMessageReader.start();
		        	System.out.println("Connected");
		        }
		        catch(Exception ex) {
		        	ex.printStackTrace();
		        	errorTextField.setText(ex.getMessage());
		        }
		    }
		});
		connectionPanel.add(hostTextField);
		connectionPanel.add(portTextField);
		connectionPanel.add(connect);
		connectionPanel.add(errorTextField);
		frame.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent we) {
				  	interaction.client.disconnect();
				    System.exit(0);
				  }
		});
		//creating the roll dice button
		JButton rollDice = new JButton();
		rollDice.setPreferredSize(new Dimension(200,40));
		rollDice.setText("Roll Dice!");
		rollDice.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					interaction.sendRoll();
			}
		});
		
		input.add(rollDice);
		panel.add(input, BorderLayout.SOUTH);
		frame.add(panel, BorderLayout.CENTER);
		panel.add(connectionPanel, BorderLayout.NORTH);
		frame.pack();
		frame.setVisible(true);
	}
	
}
class Interaction {
	socketClient client;
	public Interaction() {
		
	}
	public void connect(String host, int port, JTextField errorField) throws IOException{
		//thread just so we don't lock up main UI
		Thread connectionThread = new Thread() {
			@Override
			public void run() {
				client = new socketClient();
				try {
					client.connect(host, port);
					client.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					errorField.setText(e.getMessage());
					errorField.getParent().setVisible(true);
				}//this terminates when client is closed
				
				System.out.println("Connection thread finished");
			}
		};
		connectionThread.start();
	}
	public void sendRoll() {
		client.sendRoll();
	}
	public String getMessage() {
		if(client == null) {
			return null;
		}
		return client.messages.poll();
	}
	public boolean isClientConnected() {
		if(client == null) {
			return true;//just so loop doesn't die early
		}
		return client.isStillConnected();
	}
}
