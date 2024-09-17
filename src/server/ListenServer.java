package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;
import packet.Packet;

public class ListenServer {
	private ServerSocket serverSocket;

	// Lắng nghe trên một địa chỉ IP cụ thể
	public ListenServer(String ipAddress, int port) throws IOException {
		try {
			// Lắng nghe trên địa chỉ IP cụ thể
			InetAddress inetAddress = InetAddress.getByName(ipAddress);
			serverSocket = new ServerSocket(port, 0, inetAddress);
			int localPort = serverSocket.getLocalPort();
			System.out.println("Hệ thống đã lắng nghe kết nối từ địa chỉ IP " + ipAddress + " và cổng " + localPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Socket accept() {
		try {
			Socket socket = serverSocket.accept();
			// Hiển thị hộp thoại để hỏi xem có cho phép kết nối hay không
			int response = JOptionPane.showConfirmDialog(null,
					"Client " + socket.getInetAddress().getHostAddress() + " muốn kết nối. Bạn có đồng ý không?",
					"Yêu cầu kết nối", JOptionPane.YES_NO_OPTION);

			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

			// Nếu đồng ý, chấp nhận kết nối và gửi gói tin xác nhận
			if (response == JOptionPane.YES_OPTION) {
				output.writeObject(new Packet("server", "accepted"));
				output.flush();
				return socket;
			} else {
				// Nếu từ chối, gửi thông báo từ chối và đóng kết nối
				output.writeObject(new Packet("server", "rejected"));
				output.flush();
				socket.close();
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Packet receiveMessage(Socket socket) {
		try {
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			try {
				return (Packet) input.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendMessage(Packet packet, Socket socket) {
		try {
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(packet);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Hàm đóng server khi dừng
	public void close() throws Exception {
		if (serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();
		}
	}
}