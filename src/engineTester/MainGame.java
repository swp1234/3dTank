package engineTester;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Bullet;
import entities.Camera;
import entities.Light;
import entities.Tank;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import models.RawModel;
import models.TexturedModel;
import network.SerializableBullet;
import network.SerializableTank;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;

public class MainGame {

	static String serverIp = "127.0.0.1";
	static int serverPort = 5555;

	static HashMap<String, SerializableTank> receivedTanks = null;
	static HashMap<String, Tank> otherTanks = null;
	static HashMap<String, SerializableBullet> receivedBullets = null;
	static HashMap<String, Bullet> otherBullets = null;

	static Tank playerTank = null;
	static String clientId = "";
	static String uniqueId = "";
	static RespawnPlayerTank respawnThread = null;
	static Camera camera = null;
	static int deadCount = 0;
	static int killCount = 0;
	static GUIText killCountText;
	static GUIText deadCountText;

	public static void checkIdIsEnglish() {
		boolean isKoreanIncluded = false;
		Scanner scan = new Scanner(System.in);
		do {
			isKoreanIncluded = false;
			System.out.print("ID(ONLY ENGLISH) : ");
			clientId = scan.next();
			String[] letters = clientId.split(" ");
			for (String letter : letters) {
				if (getType(letter) == false) {
					isKoreanIncluded = true;
					break;
				}
			}
		} while (isKoreanIncluded);
	}

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		Random random = new Random();
		TextMaster.init(loader);

		Socket socket = null;
		int clientPort = 0;
		String clientIp = "";

		ReceiverThread receiverThread = null;
		ArrayList<String> leftClientList = new ArrayList<String>();
		ObjectOutputStream out = null;

		receivedTanks = new HashMap<String, SerializableTank>();
		otherTanks = new HashMap<String, Tank>();
		receivedBullets = new HashMap<String, SerializableBullet>();
		otherBullets = new HashMap<String, Bullet>();
		Vector3f lastTankPosition = new Vector3f(0, 0, 0);

		List<Bullet> myBullets = new ArrayList<Bullet>();
		ArrayList<Bullet> destroyedMyBullets = new ArrayList<Bullet>();

		try {
			checkIdIsEnglish();
			uniqueId = new String(clientId);

			socket = new Socket(serverIp, serverPort);
			clientPort = socket.getLocalPort();
			clientIp = socket.getInetAddress().getHostAddress();

			clientId = clientId + " " + clientIp + "/" + clientPort;
			out = new ObjectOutputStream(socket.getOutputStream());
			out.write(clientId.getBytes());

		} catch (IOException ioe) {
			System.err.println("Connect Exception generated...");
		} finally {

			// *--- Tank, Bullet 3D Model Setting --*
			RawModel rawTank = OBJLoader.loadObjModel("tank", loader);
			TexturedModel tankModel = new TexturedModel(rawTank, new ModelTexture(loader.loadTexture("tankTexture")));
			RawModel rawBullet = OBJLoader.loadObjModel("bullet", loader);
			TexturedModel bulletModel = new TexturedModel(rawBullet,
					new ModelTexture(loader.loadTexture("bulletTexture")));

			// x, z = random.nextFloat() * 400
			playerTank = new Tank(tankModel, new Vector3f(400, 1, 400), 0, 0, 0, 1, clientId, 100);
			FontType font = new FontType(loader.loadTexture("font"), new File("res/font.fnt"));
			GUIText Idtext = new GUIText(uniqueId, 2, font, new Vector2f(0, 0.35f), 1f, true);
			killCountText = new GUIText("Kill : " + killCount, 1.5f, font, new Vector2f(0.4f, 0.05f), 1f, true);
			deadCountText = new GUIText("Dead : " + deadCount, 1.5f, font, new Vector2f(0.4f, 0.1f), 1f, true);

			camera = new Camera(playerTank);
			Light light = new Light(new Vector3f(20000, 20000, 2000), new Vector3f(1, 1, 1));
			Terrain terrain = new Terrain(0, 0, loader, new ModelTexture(loader.loadTexture("grass")), "heightmap");
			MasterRenderer renderer = new MasterRenderer();

			while (!Display.isCloseRequested()) {
				camera.move();

				// *--- WHEN PLAYER IS ALIVE --- *
				if (playerTank.getHp() > 0) {
					playerTank.move(terrain);
					playerTank.checkBulletTime();

					// *--- Shoot Bullet ---*
					if (playerTank.getIsFired()) {
						Bullet bullet = new Bullet(bulletModel,
								new Vector3f(playerTank.getPosition().x + Bullet.OFFSET_X,
										playerTank.getPosition().y + Bullet.OFFSET_Y,
										playerTank.getPosition().z + Bullet.OFFSET_Z),
								playerTank.getRotX(), playerTank.getRotY(), playerTank.getRotZ(), 0.4f, clientId);
						myBullets.add(bullet);
						playerTank.setIsFired(false);
					}
					renderer.processEntity(playerTank);
				} else {
					// *--- WHEN PLAYER IS DEAD ---*
					if (respawnThread == null) {
						respawnThread = new RespawnPlayerTank();
						respawnThread.start();
						deadCount++;
						System.out.println(deadCount);
						deadCountText.textRenew("Dead : " + deadCount);
					}
				}
				if (!(playerTank.getPosition().x == lastTankPosition.x
						&& playerTank.getPosition().y == lastTankPosition.y
						&& playerTank.getPosition().z == lastTankPosition.z)) {
					sendTankToServer(playerTank, out);
				}
				lastTankPosition.x = playerTank.getPosition().x;
				lastTankPosition.y = playerTank.getPosition().y;
				lastTankPosition.z = playerTank.getPosition().z;

				if (receiverThread == null) {
					receiverThread = new ReceiverThread(socket);
					receiverThread.start();
				}

				getBulletsFromOtherBullets(bulletModel);
				getTanksFromReceivedTanks(tankModel);

				// *--- RENDER OTHER PLAYERS' TANKS --- *
				for (String id : otherTanks.keySet()) {
					if (otherTanks.get(id).getHp() > 0) {
						renderer.processEntity(otherTanks.get(id));
					} else {
						leftClientList.add(id);
					}
				}
				// *--- RENDER OTEHR PLAYERS' BULLETS
				for (String id : otherBullets.keySet()) {
					Bullet bullet = otherBullets.get(id);
					if (bullet.isDamageable()) {
						renderer.processEntity(bullet);
					}
				}

				// *--- REMOVE LEFT CLIENTS' TANKS --- *
				for (String id : leftClientList) {
					synchronized (otherTanks) {
						if (otherTanks.containsKey(id)) {
							otherTanks.remove(id);
						}
					}
				}
				leftClientList.clear();

				for (Bullet bullet : myBullets) {
					if (bullet.isDamageable()) {
						renderer.processEntity(bullet);
						bullet.move();
						Tank tank = bullet.inflictDamageonTank(otherTanks);
						sendBulletToServer(bullet, out);
						if (tank != null) {
							if (tank.getHp() <= 0) {
								killCount++;
								killCountText.textRenew("Kill : " + killCount);
							}
							sendTankToServer(tank, out);
						}
					} else {
						destroyedMyBullets.add(bullet);
					}
				}

				for (Bullet bullet : destroyedMyBullets) {
					myBullets.remove(bullet);
				}
				destroyedMyBullets.clear();

				renderer.processTerrain(terrain);
				renderer.render(light, camera);
				TextMaster.render();
				DisplayManager.updateDisplay();

			}
			renderer.cleanUp();
			loader.cleanUp();
			TextMaster.cleanUp();
			DisplayManager.closeDisplay();

		}
		try {
			socket.close();
			out.close();
		} catch (IOException e) {
			System.err.println("SOCKET CLOSE EXCEPTION");
		}

	}

	public static boolean getType(String word) {
		return Pattern.matches("^[0-9a-zA-Z]*$", word);
	}

	public static void sendTankToServer(Tank tank, ObjectOutputStream out) {
		try {
			SerializableTank szTank = new SerializableTank(tank);
			out.writeObject(szTank);
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR OCURRED WHILE SENDING PLAYER TANK");
		}
	}

	public static void sendBulletToServer(Bullet bullet, ObjectOutputStream out) {
		try {
			SerializableBullet szBullet = new SerializableBullet(bullet);
			out.writeObject(szBullet);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROR OCURRED WHILE SENDING PLAYER BULLET");
		}
	}

	public static void getTanksFromReceivedTanks(TexturedModel tankModel) {
		for (String id : receivedTanks.keySet()) {
			SerializableTank receivedTank = receivedTanks.get(id);

			if (otherTanks.containsKey(id)) {
				Tank otherTank = otherTanks.get(id);
				otherTank.setPosition(receivedTank.getPosition());
				otherTank.setRotX(receivedTank.getRotX());
				otherTank.setRotY(receivedTank.getRotY());
				otherTank.setRotZ(receivedTank.getRotZ());
				otherTank.setHp(receivedTank.getHp());
			} else {
				Tank otherTank = new Tank(tankModel, receivedTank.getPosition(), receivedTank.getRotX(),
						receivedTank.getRotY(), receivedTank.getRotZ(), receivedTank.getScale(), id,
						receivedTank.getHp());
				otherTanks.put(id, otherTank);
			}
		}
	}

	public static void getBulletsFromOtherBullets(TexturedModel bulletModel) {
		for (String id : receivedBullets.keySet()) {
			SerializableBullet receivedBullet = receivedBullets.get(id);

			if (otherBullets.containsKey(id)) {
				Bullet otherBullet = otherBullets.get(id);
				otherBullet.setPosition(receivedBullet.getPosition());
				otherBullet.setRotX(receivedBullet.getRotX());
				otherBullet.setRotY(receivedBullet.getRotY());
				otherBullet.setRotZ(receivedBullet.getRotZ());
				otherBullet.setAttacked(receivedBullet.getIsAttacked());
			} else {
				Bullet otherBullet = new Bullet(bulletModel, receivedBullet.getPosition(), receivedBullet.getRotX(),
						receivedBullet.getRotY(), receivedBullet.getRotZ(), receivedBullet.getScale(), id);
				otherBullets.put(id, otherBullet);
			}
		}
	}

	static class ReceiverThread extends Thread {
		Socket socket;

		ReceiverThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			while (true) {
				try {
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
					Object obj = in.readObject();
					String objClassName = obj.getClass().getName();
					if (objClassName.equals(Server.TankClassName)) {
						SerializableTank tank = (SerializableTank) obj;
						if (tank.getId().equals(clientId)) {
							synchronized (playerTank) {
								playerTank.setHp(tank.getHp());
								continue;
							}
						}
						if (receivedTanks.containsKey(tank.getId())) {
							receivedTanks.replace(tank.getId(), tank);
						} else {
							receivedTanks.put(tank.getId(), tank);
						}
					} else if (objClassName.equals(Server.BulletClassName)) {
						SerializableBullet bullet = (SerializableBullet) obj;
						if (receivedBullets.containsKey(bullet.getId())) {
							receivedBullets.replace(bullet.getId(), bullet);
						} else {
							receivedBullets.put(bullet.getId(), bullet);
						}
					}
				} catch (ClassNotFoundException e) {
					System.out.println("ERROR OCURRED WHILE RECEIVING DATA FROM SERVER");
				} catch (IOException e) {
					System.out.println("GAME DISCONNTECTED");
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;
				}
			}
		}
	}

	static class RespawnPlayerTank extends Thread {
		Random random = new Random();

		public void run() {
			try {
				Thread.sleep(3000);
				playerTank.setPosition(new Vector3f(random.nextFloat() * 400, 1, random.nextFloat() * 400));
				playerTank.setHp(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			respawnThread = null;
			return;
		}
	}

}
