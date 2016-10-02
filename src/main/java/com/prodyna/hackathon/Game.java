package com.prodyna.hackathon;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class Game {
	private List<Player> players = new ArrayList<>();
	private int[] walls = new int[Position.width * Position.height];

	private int startPosition = 0;
	private int goalPosition = 0;
	private Thread thread;

	private List<Bomb> bombs = new ArrayList<>();

	public Game() {
		thread = new Thread(() -> {
			long lastMove = System.currentTimeMillis();
			while (true) {
				
				// Continue no matter what
				
				try {				
					synchronized (Game.class) {
						try {
							List<Bomb> leftOverBombs = new ArrayList<>();
							for (Bomb bomb : bombs) {
								if (!bomb.isDone()) {
									bomb.sendEffect(getPlayers());
									leftOverBombs.add(bomb);
								}
							}
							bombs = leftOverBombs;
						} catch (Exception e) {
	
						}
					}
	
					List<Integer> goblins = new ArrayList<>();
					for (int i = 0; i < this.getTiles().length; i++) {
						if (this.getTiles()[i] == TileType.GOBLIN.ordinal()) {
							goblins.add(i);
						}
					}
					if (System.currentTimeMillis() - lastMove > 500) {
						lastMove = System.currentTimeMillis();
						Random random = new Random();
						for (Integer goblin : goblins) {
							int gx = Position.getXFor(goblin);
							int gy = Position.getYFor(goblin);
							switch (random.nextInt(4)) {
							case 0:
								gx--;
								break;
							case 1:
								gx++;
								break;
							case 2:
								gy--;
								break;
							case 3:
								gy++;
								break;
							}
							try {
								int targetPos = Position.positionFor(gx, gy);
	
								if (this.getTiles()[targetPos] == TileType.GRASS.ordinal()) {
									this.getTiles()[goblin] = TileType.GRASS.ordinal();
									this.getTiles()[targetPos] = TileType.GOBLIN.ordinal();
									getPlayers().forEach(p -> {
										p.sendUpdateTile(targetPos, this.getTiles()[targetPos]);
										p.sendUpdateTile(goblin, this.getTiles()[goblin]);
										try {
											Thread.sleep(200);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									});
								}
							} catch (InvalidPositionException e) {
							}
						}
					}
	
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	private int[] getTiles() {
		return this.walls;
	}

	public void addPlayer(Player addedPlayer) {
		players.add(addedPlayer);
		setToStartPosition(addedPlayer);
		getPlayers().forEach(existingPlayer -> addedPlayer.sendInformationAbout(existingPlayer));
	}

	private void setLevel(int[] level) {
		if (level.length != Position.width * Position.height) {
			throw new IllegalArgumentException("level has to be " + Position.width + " by " + Position.height);
		}
		this.walls = level;

		startPosition = 0;
		goalPosition = Position.width;
		for (int i = 0; i < walls.length; i++) {
			if (walls[i] == 3) {
				startPosition = i;
			} else if (walls[i] == 2) {
				goalPosition = i;
			}
		}

		players.forEach(player -> player.getPosition().setPosition(0));
	}

	private boolean hasWall(int x, int y) {
		try {
			int tile = walls[Position.positionFor(x, y)];
			return tile == TileType.TREE.ordinal() || tile == TileType.STONE.ordinal();
		} catch (InvalidPositionException e) {
			return true;
		}
	}

	public void moveUp(Player player) {
		if (player.getPosition().getY() > 0 && !hasWall(player.getPosition().getX(), player.getPosition().getY() - 1)) {
			player.getPosition().setY(player.getPosition().getY() - 1);
		}
	}

	public void moveDown(Player player) {
		if (player.getPosition().getY() + 1 < Position.height
				&& !hasWall(player.getPosition().getX(), player.getPosition().getY() + 1)) {
			player.getPosition().setY(player.getPosition().getY() + 1);
		}
	}

	public void moveRight(Player player) {
		if (player.getPosition().getX() + 1 < Position.width
				&& !hasWall(player.getPosition().getX() + 1, player.getPosition().getY())) {
			player.getPosition().setX(player.getPosition().getX() + 1);
		}
	}

	public void moveLeft(Player player) {
		if (player.getPosition().getX() > 0 && !hasWall(player.getPosition().getX() - 1, player.getPosition().getY())) {
			player.getPosition().setX(player.getPosition().getX() - 1);
		}
	}

	public void newLevel() {
		synchronized (Game.class) {
			bombs.clear();
			int[] level = createMaze(Position.width);
			Random random = new Random();

			for (int i = 0; i < level.length; i++) {
				if (level[i] != TileType.TREE.ordinal()) {
					continue;
				}

				int r = random.nextInt(100);
				if (r < 3) {
					level[i] = TileType.GOBLIN.ordinal();
				} else if (r < 50) {
					level[i] = TileType.STONE.ordinal();
				}
			}
			setLevel(level);
		}
	}

	public int finish = 0;

	public int[] createMaze(int dimension) {

		Random random = new Random();
		int start = random.nextInt(dimension * dimension);
		List<Integer> notWalls = generateMaze(dimension, start);
		int[] result = new int[dimension * dimension];
		for (int i = 1; i <= dimension * dimension; i++) {
			if (i % dimension == 1) {
				System.out.println();
			}
			if (notWalls.contains(i)) {
				// System.out.print("0 ");
				result[i - 1] = 0;
			} else {
				// System.out.print("1 ");
				result[i - 1] = 1;
			}
		}
		result[start - 1] = 3;
		result[finish - 1] = 2;

		return result;

	}

	public void addNeighbourWallsAndMark(int fieldIndex, List<Integer> walls, List<Integer> notWalls, int dimension) {
		notWalls.add(fieldIndex);
		// add Right wall
		if (fieldIndex % dimension != 0 && !notWalls.contains(fieldIndex + 1)) {
			walls.add(fieldIndex + 1);
		}
		// add left wall
		if (fieldIndex % dimension != 1 && !notWalls.contains(fieldIndex - 1)) {
			walls.add(fieldIndex - 1);
		}
		// add top wall
		if (fieldIndex + dimension <= dimension * dimension && !notWalls.contains(fieldIndex + dimension)) {
			walls.add(fieldIndex + dimension);
		}

		if (fieldIndex - dimension > 0 && !notWalls.contains(fieldIndex - dimension)) {
			walls.add(fieldIndex - dimension);
		}
	}

	public List<Integer> generateMaze(int dimension, int start) {
		List<Integer> walls = new ArrayList<Integer>();
		List<Integer> notWalls = new ArrayList<Integer>();
		walls.add(start);
		// initialization finished
		while (!walls.isEmpty()) {
			Collections.shuffle(walls, new Random());
			int neighbourCount = 0;
			if (notWalls.contains(walls.get(0) + 1)) {
				neighbourCount++;
			}
			if (notWalls.contains(walls.get(0) - 1)) {
				neighbourCount++;
			}
			if (notWalls.contains(walls.get(0) + dimension)) {
				neighbourCount++;
			}
			if (notWalls.contains(walls.get(0) - dimension)) {
				neighbourCount++;
			}
			if (neighbourCount < 2) {
				addNeighbourWallsAndMark(walls.get(0), walls, notWalls, dimension);
			}
			finish = walls.get(0);
			walls.remove(walls.get(0));

		}
		return notWalls;
	}

	public String getLevel() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < walls.length; i++) {
			sb.append(Integer.toString(walls[i]));
		}
		return sb.toString();
	}

	public List<Player> getPlayers() {
		return this.players;
	}

	public boolean isFinished(Player player) {
		return goalPosition == player.getPosition().getPosition();
	}

	public void setToStartPosition(Player player) {
		player.getPosition().setPosition(startPosition);
	}

	public void removePlayer(Player playerToRemove) {
		this.players.remove(playerToRemove);
	}

	public void addBomb(int position) {
		synchronized (Game.class) {
			bombs.add(new Bomb(position, 1000, walls));
		}
	}
}
