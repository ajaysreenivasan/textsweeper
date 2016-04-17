package textsweeper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

public class TextSweeperGame {
	private int mRowCount;
	private int mColCount;
	private Cell[][] mGameGrid;

	private int mMineCount;
	private int mTotalCellCount;
	private int mHiddenCellCount;

	private String mDifficulty;

	private boolean mIsGameOver;

	private static Scanner mInputScanner;

	private static final String COMMAND_NONE = "";
	private static final String COMMAND_QUIT = "quit";
	private static final String COMMAND_MARK = "mark";
	private static final String COMMAND_UNMARK = "unmark";

	private static final String DIFFICULTY_EASY = "EASY";
	private static final String DIFFICULTY_MED = "MED";
	private static final String DIFFICULTY_HARD = "HARD";

	private static final float MODIFIER_EASY = 0.3f;
	private static final float MODIFIER_MED = 0.5f;
	private static final float MODIFIER_HARD = 0.7f;

	public static void main(String[] args) {
		mInputScanner = new Scanner(System.in);

		System.out.println("Welcome to TextSweeper.");
		System.out.println(" - \"Because text based is good enough.\"");

		System.out.print("Enter number of rows: ");
		int rowCount = mInputScanner.nextInt();
		while (rowCount < 2) {
			System.out.println("There must be at least two rows.");
			System.out.print("Enter number of rows: ");
			rowCount = mInputScanner.nextInt();
		}

		System.out.print("Enter number of columns: ");
		int colCount = mInputScanner.nextInt();
		while (colCount < 2) {
			System.out.println("There must be at least two columns.");
			System.out.print("Enter number of rows: ");
			colCount = mInputScanner.nextInt();
		}

		System.out.println("Level");
		System.out.println("1. Easy");
		System.out.println("2. Medium");
		System.out.println("3. Hard");
		System.out.print("Choose difficulty: ");
		int difficultyChoice = mInputScanner.nextInt();
		while (difficultyChoice < 1 && difficultyChoice > 3) {
			System.out.println("Please choose a valid difficulty.");
			System.out.print("Choose difficulty: ");
			difficultyChoice = mInputScanner.nextInt();
		}

		TextSweeperGame game = new TextSweeperGame(rowCount, colCount, difficultyChoice);
		game.printInstructions();
		game.play();
		mInputScanner.close();
	}

	public TextSweeperGame(int rowCount, int colCount, int difficultyChoice) {
		mRowCount = rowCount;
		mColCount = colCount;
		mTotalCellCount = mRowCount * mColCount;

		switch (difficultyChoice) {
		case 1:
			mDifficulty = DIFFICULTY_EASY;
		case 2:
			mDifficulty = DIFFICULTY_MED;
		case 3:
			mDifficulty = DIFFICULTY_HARD;
		}

		mGameGrid = new Cell[mRowCount][mColCount];
		initializeGrid();
	}

	public void play() {
		String input = "";
		String[] inputArray;

		int row = 0;
		int col = 0;

		mInputScanner.nextLine();
		while (!mIsGameOver && !(input.equals(COMMAND_QUIT))) {
			System.out.println("-------------------------------");
			System.out.println("-------------------------------");
			printGrid();
			System.out.println("");

			System.out.println("Enter coordinates [row] [col]: ");
			input = mInputScanner.nextLine();
			inputArray = input.split(" ");

			if (inputArray.length >= 2) {
				String command = "";

				if (inputArray.length == 3) {
					command = inputArray[2];
				}

				row = Integer.parseInt(inputArray[0]);
				col = Integer.parseInt(inputArray[1]);

				if (row < 0 || row > mRowCount || col < 0 || col > mColCount) {
					System.out.println("Invalid input.");
					continue;
				}

				if (mGameGrid[row][col].getIsRevealed()) {
					System.out.println("This cell has already been revealed.");
					if (command.equals(COMMAND_MARK)) {
						System.out.println(" It cannot be marked.");
					}
					if (command.equals(COMMAND_UNMARK)) {
						System.out.println(" It cannot be unmarked.");
					}

					continue;
				} else {
					if (command.equals(COMMAND_MARK)) {
						if (!mGameGrid[row][col].getIsMarked()) {
							mGameGrid[row][col].setIsMarked();
						} else {
							System.out.println("This cell is already marked.");
						}
					} else if (command.equals(COMMAND_UNMARK)) {
						if (mGameGrid[row][col].getIsMarked()) {
							mGameGrid[row][col].setIsUnmarked();
						} else {
							System.out.println("This cell is not marked.");
						}
					} else if (command.equals(COMMAND_NONE)) {
						mGameGrid[row][col].setIsRevealed();

						if (mGameGrid[row][col].getHasMine()) {
							mIsGameOver = true;
						} else {
							revealNeighbours(mGameGrid[row][col]);
						}
					} else {
						System.out.println("Invalid input.");
					}
				}
			} else {
				System.out.println("Invalid input.");
			}

			if (mHiddenCellCount == mMineCount) {
				mIsGameOver = true;
			}
		}

		System.out.println("");
		System.out.println("-------------------------------");
		if (!mGameGrid[row][col].getHasMine()) {
			System.out.println("SUCCESS!");
		} else {
			System.out.println("FAILURE!");
		}
		System.out.println("-------------------------------");
		revealAllCells();
		printGrid();
		System.out.println("");

	}

	private void printInstructions() {
		System.out.println("-------------------------------");
		System.out.println("INSTRUCTIONS");
		System.out.println("-------------------------------");
		System.out.println("Find all the mines hidden in the grid.");
		System.out.println("Unrevealed cells are shown as _");
		System.out.println("Revealed cells without mines will show a number.");
		System.out.println("This number indicates the number of mines surrounding that particular cell.");
		System.out.println("This includes mines above, below, to both sides as well as diagonally around that cell.");
		System.out.println("Mark the cells you suspect of having mines.");
		System.out.println("Marked cells will show a \"M\".");
		System.out.println("You can also unmark cells you have previously marked.");
		System.out.println("Revealing a cell with a mine will result in game over.");
		System.out.println("Enter the coordinates of the cell you want to reveal.");
		System.out.println("Use a \"row column\" format.");
		System.out.println("Enter \"row column mark\" to mark that cell.");
		System.out.println("Enter \"row column unmark\" to remove a mark.");
		System.out.println("Enter \"QUIT\" to quit.");
		System.out.println("");
		System.out.println("The game will end when all the cells without mines have been revealed.");
		System.out.println("-------------------------------");
	}

	private int generateMineCount() {
		int mineCount = 0;
		int cellCount = mRowCount * mColCount;

		Random randomizer = new Random();
		switch (mDifficulty) {
		case DIFFICULTY_EASY:
			mineCount = randomizer.nextInt((int) (cellCount * MODIFIER_EASY)) + 1;
		case DIFFICULTY_MED:
			mineCount = randomizer.nextInt((int) (cellCount * MODIFIER_MED)) + 1;
		case DIFFICULTY_HARD:
			mineCount = randomizer.nextInt((int) (cellCount * MODIFIER_HARD)) + 1;
		}

		return mineCount;
	}

	private void initializeGrid() {
		for (int i = 0; i < mRowCount; i++) {
			for (int j = 0; j < mColCount; j++) {
				Cell newCell = new Cell(i, j);

				mGameGrid[i][j] = newCell;
			}
		}

		mHiddenCellCount = mTotalCellCount;
		mMineCount = generateMineCount();
		initializeMines(mMineCount);
		connectCells();
	}

	private void initializeMines(int mineCount) {
		int x = -1;
		int y = -1;

		Random randomizer = new Random();

		while (mineCount > 0) {
			x = randomizer.nextInt(mRowCount);
			y = randomizer.nextInt(mColCount);

			if (x < mRowCount && y < mColCount) {
				if (!mGameGrid[x][y].getHasMine()) {
					mGameGrid[x][y].setHasMine();
					mineCount -= 1;
				}
			}
		}
	}

	private void connectCells() {
		Cell currentCell = null;

		List<Cell> neighbourList = new ArrayList<>();

		for (int i = 0; i < mRowCount; i++) {
			for (int j = 0; j < mColCount; j++) {
				currentCell = mGameGrid[i][j];

				Cell northNeighbour = null;
				Cell northEastNeighbour = null;
				Cell eastNeighbour = null;
				Cell southEastNeighbour = null;
				Cell southNeighbour = null;
				Cell southWestNeighbour = null;
				Cell westNeighbour = null;
				Cell northWestNeighbour = null;

				if (i == 0) {
					if (j == 0) {
						eastNeighbour = mGameGrid[i][j + 1];
						southEastNeighbour = mGameGrid[i + 1][j + 1];
						southNeighbour = mGameGrid[i + 1][j];
					} else if (j == mColCount - 1) {
						southNeighbour = mGameGrid[i + 1][j];
						southWestNeighbour = mGameGrid[i + 1][j - 1];
						westNeighbour = mGameGrid[i][j - 1];
					} else {
						eastNeighbour = mGameGrid[i][j + 1];
						southEastNeighbour = mGameGrid[i + 1][j + 1];
						southNeighbour = mGameGrid[i + 1][j];
						southWestNeighbour = mGameGrid[i + 1][j - 1];
						westNeighbour = mGameGrid[i][j - 1];
					}
				} else if (i == mRowCount - 1) {
					if (j == 0) {
						northNeighbour = mGameGrid[i - 1][j];
						northEastNeighbour = mGameGrid[i - 1][j + 1];
						eastNeighbour = mGameGrid[i][j + 1];
					} else if (j == mColCount - 1) {
						northNeighbour = mGameGrid[i - 1][j];
						westNeighbour = mGameGrid[i][j - 1];
						northWestNeighbour = mGameGrid[i - 1][j - 1];
					} else {
						northNeighbour = mGameGrid[i - 1][j];
						northEastNeighbour = mGameGrid[i - 1][j + 1];
						eastNeighbour = mGameGrid[i][j + 1];
						westNeighbour = mGameGrid[i][j - 1];
						northWestNeighbour = mGameGrid[i - 1][j - 1];
					}
				} else {
					if (j == 0) {
						northNeighbour = mGameGrid[i - 1][j];
						northEastNeighbour = mGameGrid[i - 1][j + 1];
						eastNeighbour = mGameGrid[i][j + 1];
						southEastNeighbour = mGameGrid[i + 1][j + 1];
						southNeighbour = mGameGrid[i + 1][j];
					} else if (j == mColCount - 1) {
						northNeighbour = mGameGrid[i - 1][j];
						southNeighbour = mGameGrid[i + 1][j];
						southWestNeighbour = mGameGrid[i + 1][j - 1];
						westNeighbour = mGameGrid[i][j - 1];
						northWestNeighbour = mGameGrid[i - 1][j - 1];
					} else {
						northNeighbour = mGameGrid[i - 1][j];
						northEastNeighbour = mGameGrid[i - 1][j + 1];
						eastNeighbour = mGameGrid[i][j + 1];
						southEastNeighbour = mGameGrid[i + 1][j + 1];
						southNeighbour = mGameGrid[i + 1][j];
						southWestNeighbour = mGameGrid[i + 1][j - 1];
						westNeighbour = mGameGrid[i][j - 1];
						northWestNeighbour = mGameGrid[i - 1][j - 1];
					}
				}

				neighbourList.add(northNeighbour);
				neighbourList.add(northEastNeighbour);
				neighbourList.add(eastNeighbour);
				neighbourList.add(southEastNeighbour);
				neighbourList.add(southNeighbour);
				neighbourList.add(southWestNeighbour);
				neighbourList.add(westNeighbour);
				neighbourList.add(northWestNeighbour);

				for (Cell neighbour : neighbourList) {
					if (neighbour != null) {
						currentCell.addNeighbour(neighbour);
					}
				}

				neighbourList.clear();
			}
		}
	}

	private void revealNeighbours(Cell startCell) {
		Queue<Cell> cellQueue = new LinkedList<>();
		cellQueue.add(startCell);

		while (!cellQueue.isEmpty()) {
			Cell currentCell = cellQueue.remove();

			currentCell.setIsRevealed();
			mHiddenCellCount -= 1;

			if (currentCell.getNeighbourMineCount() == 0) {
				List<Cell> neighbourList = currentCell.getNeighbourList();
				for (Cell neighbour : neighbourList) {
					if (!neighbour.getHasMine() && !neighbour.getIsRevealed() && !cellQueue.contains(neighbour)) {
						cellQueue.add(neighbour);
					}
				}
			}
		}
	}

	private void revealAllCells() {
		for (int i = 0; i < mRowCount; i++) {
			for (int j = 0; j < mRowCount; j++) {
				mGameGrid[i][j].setIsRevealed();
			}
		}
	}

	public boolean checkCellForMine(int x, int y) {
		return mGameGrid[x][y].getHasMine();
	}

	public void printGrid() {
		System.out.print("  ");
		for (int i = 0; i < mColCount; i++) {
			System.out.print(i + " ");
		}
		System.out.println("");

		for (int i = 0; i < mRowCount; i++) {
			System.out.print(i + " ");
			for (int j = 0; j < mColCount; j++) {
				System.out.print(mGameGrid[i][j] + " ");
			}
			System.out.println("");
		}
	}
}
