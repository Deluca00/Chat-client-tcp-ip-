package server;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;

import packet.Packet;

public class GUIServer extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JTextField portName = new JTextField(20);
	private JButton startStopButton = new JButton("Start");
	private JButton copyIpButton = new JButton("Copy IP");
	private JLabel serverIpLabel = new JLabel(); // Hiển thị địa chỉ IP của server
	private JLabel connectionCountLabel = new JLabel("Connected Clients: 0"); // Hiển thị số lượng người kết nối
	private ListenServer listenServer;
	private Thread serverThread = null;
	private DefaultListModel<String> clientListModel = new DefaultListModel<>();
	private JList<String> clientList = new JList<>(clientListModel);

	public GUIServer() {
		portName.setText("9999");

		JPanel panel = new JPanel();
		JPanel panelControl = new JPanel(new BorderLayout());
		panel.add(new JLabel("Port"));
		panel.add(portName);
		panelControl.add(startStopButton, BorderLayout.EAST);

		JPanel clientPanel = new JPanel(new BorderLayout());
		clientPanel.add(connectionCountLabel, BorderLayout.NORTH); // Thêm nhãn hiển thị số lượng client
		clientPanel.add(new JScrollPane(clientList), BorderLayout.CENTER);

		// Hiển thị địa chỉ IP của server
		try {
			InetAddress ipv4 = Inet4Address.getLocalHost();
			serverIpLabel.setText("Server IP: " + ipv4.getHostAddress());
		} catch (IOException e) {
			serverIpLabel.setText("Không thể lấy địa chỉ IP.");
		}

		JPanel ipPanel = new JPanel();
		ipPanel.add(serverIpLabel);
		ipPanel.add(copyIpButton);

		this.setLayout(new GridLayout(4, 1));
		this.add(panel);
		this.add(panelControl);
		this.add(clientPanel);
		this.add(ipPanel); // Thêm panel hiển thị IP

		this.pack();
		this.setTitle("Server");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		startStopButton.addActionListener(this);
		copyIpButton.addActionListener(this); // Thêm hành động cho nút copy IP
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == startStopButton) {
			if (startStopButton.getText().equals("Start")) {
				// Bắt đầu server
				if (!portName.getText().equals("")) {
					try {
						InetAddress ipv4ma = Inet4Address.getLocalHost();
						serverIpLabel.setText("Server IP: " + ipv4ma.getHostAddress());
						listenServer = new ListenServer(ipv4ma.getHostAddress(), Integer.parseInt(portName.getText()));
						serverThread = new Thread(() -> {
							final ArrayList<Socket> list = new ArrayList<>();

							while (true) {
								Socket socket = listenServer.accept();
								if (socket != null) {
									synchronized (list) {
										list.add(socket);
										String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
										clientListModel.addElement(socket.getInetAddress().getHostAddress() + ":"
												+ socket.getPort() + " đã đăng nhập lúc " + timeStamp + ")");
										connectionCountLabel.setText("Connected Clients: " + list.size()); // Cập nhật
																											// số lượng
																											// kết nối
									}

									new Thread(() -> {
										while (true) {
											Packet packet = listenServer.receiveMessage(socket);
											if (packet == null) {
												synchronized (list) {
													list.remove(socket);
													String timeStamp1 = new SimpleDateFormat("HH:mm:ss")
															.format(new Date());
													clientListModel
															.removeElement(socket.getInetAddress().getHostAddress()
																	+ ":" + socket.getPort() + " đã đăng nhập lúc "
																	+ timeStamp1 + ")");
													connectionCountLabel.setText("Connected Clients: " + list.size()); // Cập
																														// nhật
																														// số
																														// lượng
																														// kết
																														// nối
													JOptionPane.showMessageDialog(null,
															"Client " + clientListModel + " đã disconnect lúc "
																	+ timeStamp1,
															"Server", JOptionPane.INFORMATION_MESSAGE);

													// Cập nhật số lượng kết nối
													connectionCountLabel.setText("Connected Clients: " + list.size());
												}
												break;
											}

											synchronized (list) {
												for (Socket soc : list) {
													if (!soc.equals(socket)) {
														listenServer.sendMessage(packet, soc);
													}
												}
											}
										}
									}).start();
								}
							}
						});

						serverThread.start();
						startStopButton.setText("Stop"); // Đổi nút thành "Stop"
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this,
								"Không thể khởi động server. Vui lòng kiểm tra lại thông tin.", "Lỗi khởi động server",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			} else if (startStopButton.getText().equals("Stop")) {
				// Dừng server
				try {

					listenServer.close(); // Đóng server socket
					serverThread.interrupt(); // Dừng luồng server
					clientListModel.clear(); // Xóa danh sách client
					connectionCountLabel.setText("Connected Clients: 0"); // Đặt lại số lượng kết nối
					startStopButton.setText("Start"); // Đổi lại nút thành "Start"
					JOptionPane.showMessageDialog(this, "Server đã dừng.", "Thông báo",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "Lỗi khi dừng server.", "Lỗi dừng server",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (arg0.getSource() == copyIpButton) {
			// Sao chép địa chỉ IP vào clipboard
			StringSelection stringSelection = new StringSelection(serverIpLabel.getText().replace("Server IP: ", ""));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
			JOptionPane.showMessageDialog(this, "Địa chỉ IP đã được sao chép vào clipboard.", "Sao chép IP",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			GUIServer server = new GUIServer();
			server.setVisible(true);
		});
	}
}