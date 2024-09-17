package packet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import packet.*;
import client.*;

public class GUIPacket extends JPanel {
	private static final long serialVersionUID = 1L;

	public GUIPacket(Packet packet) {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel(packet.getName());
		Font font = new Font("Arial", Font.BOLD, 16);
		label.setForeground(Color.BLUE);
		label.setFont(font);
		panel.add(label, BorderLayout.NORTH);
		panel.add(new JTextArea(packet.getMessage()), BorderLayout.CENTER);
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
		this.setVisible(true);
	}

}