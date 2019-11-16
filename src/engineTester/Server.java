package engineTester;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import entities.Tank;
import network.SerializableBullet;
import network.SerializableTank;

public class Server {
	static ServerSocket server;
	static int clientCount = 0;
	static HashMap<Socket, String> clientList = new HashMap<Socket, String>();
	static HashMap<String, SerializableTank> clientTankList = new HashMap<String, SerializableTank>();

	static final String TankClassName = "network.SerializableTank";
	static final String BulletClassName = "network.SerializableBullet";

	public static void main(String[] args) {

		try {
			server = new ServerSocket(5555);
			System.out.println("SERVER IS ALIVE");
			while (true) {
				Socket clientSocket = server.accept();
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
				String clientId = in.readLine();

				InetAddress clientInet = clientSocket.getInetAddress();
				int clientPort = clientSocket.getPort();
				String clientIp = clientInet.getHostAddress();

				clientList.put(clientSocket, clientId);
				clientCount++;
				System.out.println("NEW CLIENT ID : " + clientId + " CONNTECTED FROM IP : " + clientIp + "/"
						+ clientPort + " ####### " + "CLIENT COUNT : " + clientCount);
				new ThreadServer(clientSocket, in).start();
			}
		} catch (IOException ioe) {
			System.err.println("CLIENT ACCEPT ERROR");
		} finally {
			try {
				server.close();
				System.out.println("SERVER IS CLOSED");
			} catch (IOException ioe) {
				System.err.println("SERVER CLOSING ERROR");
			}
		}
	}

}

class ThreadServer extends Thread {
	private Socket clientSocket;
	ObjectInputStream in;
	boolean firstLogin;

	public ThreadServer(Socket clientSocket, ObjectInputStream in) {
		this.clientSocket = clientSocket;
		this.in = in;
		firstLogin = true;
	}

	public void run() {
		while (true) {
			try {
				if (firstLogin) {
					if (Server.clientList.size() > 0) {
						synchronized (Server.clientTankList) {
							for (String clientId : Server.clientTankList.keySet()) {
								ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
								out.writeObject(Server.clientTankList.get(clientId));
								out.flush();
							}
						}
					}
					firstLogin = false;
				}

				Object obj = in.readObject();
				String objClassName = obj.getClass().getName();
				if (objClassName.equals(Server.TankClassName)) {
					SerializableTank tank = (SerializableTank) obj;
					synchronized (Server.clientTankList) {
						if (Server.clientTankList.containsKey(tank.getId())) {
							Server.clientTankList.replace(tank.getId(), tank);
						} else {
							Server.clientTankList.put(tank.getId(), tank);
						}
					}
					sendTankToClients(tank);
				} else if (objClassName.equals(Server.BulletClassName)) {
					SerializableBullet bullet = (SerializableBullet) obj;
					sendBulletToClients(bullet);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (Server.clientList.containsKey(clientSocket)) {
					String id = Server.clientList.get(clientSocket);
					synchronized (Server.clientTankList) {
						if (Server.clientTankList.containsKey(id)) {
							Server.clientTankList.remove(id);
						}

					}
					synchronized (Server.clientList) {
						Server.clientList.remove(clientSocket);
						try {
							clientSocket.close();
							in.close();
							Server.clientCount--;
							System.out.println(
									"CLIENT ID " + id + " DISCONNECTED ## CLIENT COUNT : " + Server.clientCount);
						} catch (IOException e1) {
							System.out.println("ERROR OCURRED AFTER CLIENT " + id + " LEFT");
						}
					}

					SerializableTank leftClientTank = new SerializableTank(
							new Tank(null, new Vector3f(0, 0, 0), 0, 0, 0, 0, id, 0));
					try {
						sendTankToClients(leftClientTank);
					} catch (IOException e1) {
						System.out.println("ERROR OCURRED WHILE DELETING " + id + " 'S TANK");
					}
					return;
				}

			} catch (ClassNotFoundException e) {
				System.out.println("ERROR OCURRED WHILE RECEIVING DATA FROM CLIENT");
			}
		}
	}

	public void sendTankToClients(SerializableTank tank) throws IOException {
		for (Socket socket : Server.clientList.keySet()) {
			synchronized (socket) {
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				out.writeObject(tank);
				out.flush();
			}
		}
	}

	public void sendBulletToClients(SerializableBullet bullet) throws IOException {
		for (Socket socket : Server.clientList.keySet())
			if (socket != clientSocket) {
				synchronized (socket) {
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					out.writeObject(bullet);
					out.flush();
				}
			}
	}
}
