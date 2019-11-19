package socket4;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
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
		
	}
}
