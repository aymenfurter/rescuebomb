package com.prodyna.hackathon;

import java.util.function.Consumer;

public class Position {
	private int position = 0;
	public static int width = 25;
	public static int height = 25;

	private Runnable onChange;

	public Position(Runnable onChange) {
		this.onChange = onChange;
	}

	public int getX() {
		return position % width;
	}

	public int getY() {
		return position / width;
	}

	public void setPosition(int newValue) {
		position = newValue;
		System.out.println("Position Updated [" + getX() + "," + getY() + "]");
		onChange.run();
	}

	public int getPosition() {
		return position;
	}

	public void setX(int x) {
		try {
			setPosition(positionFor(x, getY()));
		} catch (InvalidPositionException e) {
		}
	}

	public void setY(int y) {
		try {
			setPosition(positionFor(getX(), y));
		} catch (InvalidPositionException e) {
		}
	}

	public static int positionFor(int x, int y) throws InvalidPositionException {
		if (x < 0 || x >= Position.width || y < 0 || y >= Position.height) {
			throw new InvalidPositionException();
		}
		return y * width + x;
	}

	public static int getXFor(int position) {
		return position % width;
	}

	public static int getYFor(int position) {
		return position / width;
	}
}
