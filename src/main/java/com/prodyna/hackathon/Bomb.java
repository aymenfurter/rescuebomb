package com.prodyna.hackathon;

import java.util.List;

public class Bomb {
	private long creationTime;
	private long delayMs;
	private long explosionTime = 300;
	private int position;
	private int[] walls;
	private int explosionRadius = 1;

	boolean exploding = false;
	boolean done = false;

	public Bomb(int position, long delayMs, int[] walls) {
		this.creationTime = System.currentTimeMillis();
		this.delayMs = delayMs;
		this.position = position;
		this.walls = walls;
	}

	public boolean isDone() {
		return done;
	}

	public void sendEffect(List<Player> players) {
		boolean shouldExplode = System.currentTimeMillis() - creationTime > delayMs;
		boolean shouldBeDone = System.currentTimeMillis() - creationTime > (delayMs + explosionTime);

		if (shouldExplode) {
			if (!exploding) {
				exploding = true;
				int x = Position.getXFor(position);
				int y = Position.getYFor(position);

				for (int dx = -explosionRadius; dx <= explosionRadius; dx++) {
					for (int dy = -explosionRadius; dy <= explosionRadius; dy++) {
						try {
							int pos = Position.positionFor(x + dx, y + dy);
							players.forEach(p -> p.sendUpdateTile(pos, TileType.EXPLOSION.ordinal()));
						} catch (InvalidPositionException e) {
						}
					}
				}
			} else if (shouldBeDone && !done) {
				int x = Position.getXFor(position);
				int y = Position.getYFor(position);

				for (int dx = -explosionRadius; dx <= explosionRadius; dx++) {
					for (int dy = -explosionRadius; dy <= explosionRadius; dy++) {
						try {
							int pos = Position.positionFor(x + dx, y + dy);
							if (walls[pos] == TileType.TREE.ordinal()) {
								walls[pos] = TileType.GRASS.ordinal();
							} else if (walls[pos] == TileType.PRINCESS.ordinal()) {
								walls[pos] = TileType.ROASTED_PRINCESS.ordinal();
							} else if (walls[pos] == TileType.GOBLIN.ordinal()) {
								walls[pos] = TileType.ROASTED_PRINCESS.ordinal();
							}
							players.forEach(p -> p.sendUpdateTile(pos, walls[pos]));
						} catch (InvalidPositionException e) {
						}
					}
				}
			}
		}

	}
}
