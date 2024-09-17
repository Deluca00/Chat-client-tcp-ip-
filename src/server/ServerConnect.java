package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import packet.*;

public class ServerConnect {
	private Socket socket;

	public ServerConnect(String serverName, int port) {
		try {
			socket = new Socket(serverName, port);
		} catch (IOException e) {
			System.out.print("Không kết nối được tới server " + serverName + " qua cổng " + port);
		}
	}

	public Packet receiveMessage() {
		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(socket.getInputStream());
			try {
				Packet packet = (Packet) input.readObject();
				return packet;
			} catch (ClassNotFoundException e) {
			}
		} catch (IOException e) {
		}
		return null;
	}

	public void sendMessage(Packet packet) {
		try {
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(packet);
			output.flush();
		} catch (IOException e) {
		}
	}

	// Hàm mới để đóng kết nối
	public void close() throws IOException {
		if (socket != null && !socket.isClosed()) {
			socket.close();

		}
	}
}