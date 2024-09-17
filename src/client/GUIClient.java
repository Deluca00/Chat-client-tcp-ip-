package client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import server.*;
import packet.*;

public class GUIClient extends JFrame implements ActionListener, KeyListener {

	private static final long serialVersionUID = -2253155641100317203L;

	private DefaultListModel<Packet> listModel = new DefaultListModel<>();
	private final JList<Packet> log;
	private JTextField message = new JTextField();
	private JButton send = new JButton("Send");
	private JScrollPane scrollPane = new JScrollPane();
	private JTextField serverName = new JTextField(10);
	private JTextField nickName = new JTextField(10);
	private JTextField port = new JTextField(10);
	private JButton connect = new JButton("Connect");
	private JPanel panelControl = new JPanel(new GridLayout(4, 2));
	private JPanel panelTop = new JPanel(new BorderLayout());
	private Packet packetSend = new Packet("Me", "");
	private Packet packetRevice = new Packet("", "");
	private ServerConnect server;
	private boolean mySend = false;

	public GUIClient() {
		serverName.setText("172.20.1.200"); // Địa chỉ IP của server trong mạng
		String name = "nickName" + (int) (Math.random() * 100);
		nickName.setText(name);
		port.setText("9999"); // Cổng của server

		panelControl.add(new JLabel("Server name:"));
		panelControl.add(serverName);
		panelControl.add(new JLabel("Port:"));
		panelControl.add(port);
		panelControl.add(new JLabel("Nick name:"));
		panelControl.add(nickName);
		panelControl.add(new JLabel());
		panelControl.add(connect);

		panelTop.add(panelControl, BorderLayout.EAST);
		log = new JList<>(listModel);
		scrollPane.setViewportView(log);
		scrollPane.setPreferredSize(getPreferredSize());
		scrollPane.createVerticalScrollBar();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				e.getAdjustable().setValue(e.getAdjustable().getMaximum());
			}
		});
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(message, BorderLayout.CENTER);
		panel.add(send, BorderLayout.EAST);

		this.setSize(400, 600);
		this.setLayout(new BorderLayout());
		this.add(panelTop, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(panel, BorderLayout.SOUTH);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle(name);
		send.addActionListener(this);
		connect.addActionListener(this);
		message.addKeyListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == connect && !serverName.getText().equals("") && !port.getText().equals("")
				&& !nickName.getText().equals("")) {

			if (connect.getText().equals("Connect")) {
				try {
					// Kết nối tới server
					server = new ServerConnect(serverName.getText(), Integer.parseInt(port.getText()));

					// Nếu kết nối thành công
					if (server != null) {

						this.setTitle(nickName.getText());
						JOptionPane.showMessageDialog(this, "Đang chờ kết nối tới server!", "Client",
								JOptionPane.INFORMATION_MESSAGE);

						new Thread(() -> {
							while (true) {
								Packet msg = server.receiveMessage();
								if (msg == null) {
									continue;
								}

								// Nếu nhận thông báo server_stop, ngắt kết nối
								if (connect.equals("Disconnect")) {
									SwingUtilities.invokeLater(() -> {
										JOptionPane.showMessageDialog(this,
												"Server đã dừng. Bạn không còn kết nối được nữa.", "Thông báo",
												JOptionPane.INFORMATION_MESSAGE);
										restarapp();
									});
									break;
								}

								// Nếu tin nhắn đến từ người khác, thêm vào danh sách
								if (!msg.getName().equalsIgnoreCase(nickName.getText())) {
									SwingUtilities.invokeLater(() -> {
										listModel.addElement(msg);
										log.ensureIndexIsVisible(listModel.getSize() - 1);
									});
								}
							}
						}).start();
						connect.setText("Disconnect");

					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this,
							"Không thể kết nối tới server. Vui lòng kiểm tra lại thông tin.", "Lỗi kết nối",
							JOptionPane.ERROR_MESSAGE);
				}
			} else if (connect.getText().equals("Disconnect")) {
				// Xử lý ngắt kết nối
				try {
					if (server != null) {
						server.close();
						listModel.clear();
						restarapp();
						this.revalidate();
					}
					connect.setText("Connect");
					JOptionPane.showMessageDialog(this, "Đã ngắt kết nối khỏi server.", "Ngắt kết nối",
							JOptionPane.INFORMATION_MESSAGE);
					listModel.clear();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, "Lỗi khi ngắt kết nối.", "Lỗi ngắt kết nối",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		if (ae.getSource() == send) {
			try {
				Packet packet = new Packet(nickName.getText(), message.getText());
				server.sendMessage(packet);

				SwingUtilities.invokeLater(() -> {
					listModel.addElement(new Packet("Me", message.getText()));
					log.ensureIndexIsVisible(listModel.getSize() - 1);
					message.setText("");
				});

				mySend = true;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Không thể gửi tin nhắn. Vui lòng kiểm tra kết nối.",
						"Lỗi gửi tin nhắn", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void restarapp() {
		// Đóng cửa sổ hiện tại
		this.dispose();
		// Khởi động lại ứng dụng bằng cách gọi lại ChatClient.main()
		ChatClient.main(null); // Gọi lại hàm main để chạy lại client
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			try {
				// Tạo gói tin để gửi
				Packet packet = new Packet(nickName.getText(), message.getText());

				// Gửi tin nhắn tới server
				server.sendMessage(packet);
				// Cập nhật tin nhắn vào danh sách tin nhắn
				SwingUtilities.invokeLater(() -> {
					System.out.println("Message: " + message.getText());
					listModel.addElement(new Packet("Me", message.getText())); // Thêm tin nhắn vào DefaultListModel
					message.setText(""); // Xóa trường nhập sau khi gửi
				});

				mySend = true; // Đánh dấu đã gửi tin
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this, "Không thể gửi tin nhắn. Vui lòng kiểm tra kết nối.",
						"Lỗi gửi tin nhắn", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Phương thức này có thể bỏ trống nếu không cần xử lý sự kiện keyTyped
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
	}
}
