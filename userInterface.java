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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class userInterface {
	public static boolean isRunning = true;
	
	public static void CreateBoard(JPanel panel, String location, JFrame frame, Dimension dimension) {
		JPanel panelBoard = new JPanel();
		for (int i = 0; i < 5; i++) {
			JButton b = new JButton();
			b.setPreferredSize(new Dimension(40,40));
			panelBoard.add(b);
		}
		panelBoard.setPreferredSize(dimension);
		panel.add(panelBoard);
		frame.add(panel);
	}
	
	public static void main(String[] args) {
		//creating the frame
		JFrame frame = new JFrame("Trouble Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//create the panel that will hold the board panels
		JPanel board = new JPanel();
		board.setPreferredSize(new Dimension(700,500));
		frame.setLayout(new BorderLayout());
				
		//create a dice roll panel
		JPanel diceRoll = new JPanel();
		diceRoll.setPreferredSize(new Dimension(300,300));
				
		//create a dice roll button to go into the dice roll panel
		JButton rollDice = new JButton();
		rollDice.setText("Roll Dice!");
		rollDice.setPreferredSize(new Dimension(100,100));
		diceRoll.add(rollDice, BorderLayout.CENTER);
		
		//create a text area underneath a dice roll button
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(10,11,400,300);
		diceRoll.add(textArea, BorderLayout.SOUTH);
	
		//Interaction will be our instance to interact with
		//socket client
		Interaction interaction = new Interaction();
		Thread clientMessageReader = new Thread() {
			@Override
			public void run() {
				while(isRunning && interaction.isClientConnected()) {
					String m = interaction.getMessage();
					if(m != null) {
						System.out.println("Got message " + m);
						if(m.indexOf("[name]") > -1) {
							String[] n = m.split("]");
							frame.setTitle(frame.getTitle() + " - " + n[1]);
						}
						else {
							System.out.println("Appending to textarea");
							textArea.append(m +"\n");
						}
					}
				
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("Message reader thread finished");
			}
			
		};
		
		//create action listener for dice roll
		rollDice.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				interaction.sendRoll(); //this needs to be changed
			}
		});
		
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
		
		board.add(diceRoll, BorderLayout.CENTER);
		board.add(textArea, BorderLayout.SOUTH);
		CreateBoard(board, BorderLayout.NORTH, frame, new Dimension(450, 50));
		CreateBoard(board, BorderLayout.SOUTH, frame, new Dimension(450, 50));
		CreateBoard(board, BorderLayout.WEST, frame, new Dimension(450,50));
		frame.add(board, BorderLayout.CENTER);
		frame.add(connectionPanel, BorderLayout.NORTH);
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
	public boolean isClientConnected() {
		if(client == null) {
			return true;//just so loop doesn't die early
		}
		return client.isStillConnected();
	}
	public String getMessage() {
		if(client == null) {
			return null;
		}
		return client.messages.poll();
	}
}