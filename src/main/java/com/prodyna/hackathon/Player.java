package com.prodyna.hackathon;

import java.awt.Component;
import java.util.function.Consumer;

import javax.websocket.Session;

public class Player {
	private int id;
	private Position position;
	private Session session;
	private Consumer<Player> onMoved;

	public Player(Session session, int id, Consumer<Player> onMoved) {
		this.id = id;
		this.onMoved = onMoved;
		this.position = new Position(() -> this.onMoved.accept(this));
		this.session = session;
	}

	public int getId() {
		return id;
	}

	public Position getPosition() {
		return position;
	}

	public Session getSession() {
		return session;
	}

	public void sendInformationAbout(Player player) {
		try {
			session.getBasicRemote().sendText("{\"type\":\"player\",\"id\":" + player.getId() + ",\"payload\":\""
					+ player.getPosition().getPosition() + "\"}");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendGameOver(Player winner) {
		try {
			getSession().getBasicRemote().sendText("{\"type\":\"gameover\",\"payload\":\"" + winner.getId() + "\"}");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendLevel(String level) {
		try {
			getSession().getBasicRemote().sendText("{\"type\":\"level\",\"payload\":\"" + level + "\"}");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendUpdateTile(int position, int tileType) {
		try {
			getSession().getBasicRemote()
					.sendText("{\"type\":\"update\",\"id\":" + position + ",\"payload\":\"" + tileType + "\"}");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
