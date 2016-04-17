package textsweeper;

import java.util.ArrayList;
import java.util.List;

public class Cell {
	private int mX;
	private int mY;
	private boolean mIsRevealed;
	private boolean mIsMarked;
	private boolean mHasMine;

	private int mNeighbourMineCount;
	private List<Cell> mNeighbourList;

	public Cell(int x, int y) {
		mX = x;
		mY = y;

		mIsRevealed = false;
		mIsMarked = false;
		mHasMine = false;

		mNeighbourMineCount = 0;
		mNeighbourList = new ArrayList<>();
	}

	public int getX() {
		return mX;
	}

	public int getY() {
		return mY;
	}

	public boolean getIsRevealed() {
		return mIsRevealed;
	}

	public void setIsRevealed() {
		mIsRevealed = true;
	}

	public boolean getIsMarked() {
		return mIsMarked;
	}

	public void setIsMarked() {
		mIsMarked = true;
	}

	public void setIsUnmarked() {
		mIsMarked = false;
	}

	public void setHasMine() {
		mHasMine = true;
	}

	public boolean getHasMine() {
		return mHasMine;
	}

	public int getNeighbourMineCount() {
		return mNeighbourMineCount;
	}

	public List<Cell> getNeighbourList() {
		return mNeighbourList;
	}

	public void addNeighbour(Cell neighbour) {
		if (neighbour.getHasMine()) {
			mNeighbourMineCount += 1;
		}

		mNeighbourList.add(neighbour);
	}

	public String toString() {
		String cellString = "";

		if (!mIsRevealed) {
			if (mIsMarked) {
				cellString = "M";
			} else {
				cellString = "_";
			}
		} else {
			cellString = Integer.toString(mNeighbourMineCount);

			if (mHasMine) {
				cellString = "X";
			}
		}

		return cellString;
	}

}
