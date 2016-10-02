package com.prodyna.hackathon;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
public class WebsocketEndpoint {
	private static Map<Session, Player> players = new HashMap<>();

	private static Game game = new Game();
	private static ArrayDeque<Integer> freeIds = new ArrayDeque<>();
	static {
		game.newLevel();
		
		for (int i = 0; i < 100; i++) {
			freeIds.add(i);
		}
	}

	@OnOpen
	public void onOpen(Session session) throws IOException {
		Player player = new Player(session, freeIds.pop(), thisPlayer -> {
			game.getPlayers().forEach(otherPlayer -> otherPlayer.sendInformationAbout(thisPlayer));
			if (game.isFinished(thisPlayer)) {
				game.getPlayers().forEach(otherPlayer -> {
					otherPlayer.sendGameOver(thisPlayer);
					game.newLevel();
					game.getPlayers().forEach(p -> {
						p.sendLevel(game.getLevel());
						game.setToStartPosition(p);
					});
				});
			}
		});
		player.sendLevel(game.getLevel());
		players.put(session, player);
		game.addPlayer(player);
	}

	@OnMessage
	public synchronized void onMessage(Session session, String message) {
		Player player = players.get(session);

		switch (message) {
		case "up":
			game.moveUp(player);
			break;
		case "down":
			game.moveDown(player);
			break;
		case "right":
			game.moveRight(player);
			break;
		case "left":
			game.moveLeft(player);
			break;
		case "space":
			game.addBomb(player.getPosition().getPosition());
			game.getPlayers().forEach(p -> p.sendUpdateTile(player.getPosition().getPosition(), TileType.BOMB.ordinal()));
			break;
		}
	}

	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@OnClose
	public void onClose(Session session) {
		Player playerToRemove = players.get(session);
		players.remove(session);
		game.removePlayer(playerToRemove);
	}
}