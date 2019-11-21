package socket4;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

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
		textSpot.setEditable(true);
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
				while(running) {
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
						Thread.sleep(30);
					} catch (InterruptedException e ) {
						e.printStackTrace();
					}
				}
				System.out.println("Message reader thread has finished");
			}
			
		};
		
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
		frame.pack();
		frame.setVisible(true);
	}
	
}
class Interaction {
	socketClient client;
	public Interaction() {
		
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
}
