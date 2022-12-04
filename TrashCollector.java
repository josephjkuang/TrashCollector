import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.event.KeyAdapter.*;
import java.awt.event.KeyEvent.*;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.applet.Applet;
import java.lang.Math;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Date;
import static java.util.concurrent.TimeUnit.SECONDS;

public class TrashCollector extends JPanel implements KeyListener, ActionListener, Runnable {

	private static Block[][] board;
	public static JFrame frame = new JFrame("Trash Collector");
	private static JButton button;

	private static final String imageRelPath = "./Images/"; 
	
	private static Image userImage;
	private static Image trashImage;
	private static Image recycleImage;
	private static Image compostImage;
	private static Image sinkImage;
	private static Image tableImage;
	private static Image towelImage;
	private static Image outlineImage;
	private static Image[] itemImages;

	private static int scoreboard;
	private static boolean reset;
	private static ArrayList<Block> inventory;

	private static int UP_VALUE = 0;
	private static int RIGHT_VALUE = 1;
	private static int DOWN_VALUE = 2;
	private static int LEFT_VALUE = 3;
	private static int GRAB_VALUE = 32;
	private static int RESTART_VALUE = 82;

	private static int WIDTH = 10;
	private static int HEIGHT = 5;

	private static int BOX_DIMENSION = 100;
	private static int PADDING = 10;
	private static int MARGIN = 100;

	private static boolean listen;
	private final Object lock = new Object();

	public TrashCollector() { // Default Constructor where Key Listener is added
		frame.repaint();
		frame.addKeyListener(this);
		scoreboard = 0;
		reset = false;
	}

	public static void main(String[] args) { // Main Method that Starts the Game
		userImage = loadImage("user.png");
		trashImage = loadImage("trash.png");
		recycleImage = loadImage("recycle.png");
		compostImage = loadImage("compost.png");
		sinkImage = loadImage("sink.png");
		tableImage = loadImage("table.png");
		towelImage = loadImage("towel.png");
		outlineImage = loadImage("yellow.png");

		itemImages = new Image[15];
		itemImages[0] = loadImage("water.png");
		itemImages[1] = loadImage("soda.png");
		itemImages[2] = loadImage("aluminum.png");
		itemImages[3] = loadImage("plastic.png");
		itemImages[4] = loadImage("straw.png");
		itemImages[5] = loadImage("ketchup.png");
		itemImages[6] = loadImage("chinese.png");
		itemImages[7] = loadImage("black.png");
		itemImages[8] = loadImage("pizza.png");
		itemImages[9] = loadImage("muffin.png");
		itemImages[10] = loadImage("bananana.png");
		itemImages[11] = loadImage("wet_aluminum.png");
		itemImages[12] = loadImage("dirty_aluminum.png");
		itemImages[13] = loadImage("wet_plastic.png");
		itemImages[14] = loadImage("dirty_plastic.png");

		createNewGame();
		setBoard();
		printBoard();

		TrashCollector obj = new TrashCollector();
	    Thread thread = new Thread(obj);
	    thread.start();
	    listen = true;
	}

	public void run() { // Runs concurrently
		System.out.println("This code is running in a thread");
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        final Runnable runnable = new Runnable() {
            int countdownStarter = 20;

            public void run() {

                System.out.println(countdownStarter);
                countdownStarter--;

                if (countdownStarter < 0) {
                    System.out.println("Timer Over!");
                    scheduler.shutdown();
                }

                if (countdownStarter % 5 == 0) {
                	int number = (int)(Math.random() * (WIDTH - 1) * HEIGHT); // Random Space on the Board
					int i = number / (WIDTH - 1);
					int j = number % (WIDTH - 1);
					int item_index = (int)(Math.random() * 11);

					board[i][j] = new Block(itemImages[item_index], false);
                }
            }
        };
        scheduler.scheduleAtFixedRate(runnable, 0, 1, SECONDS);
	}

	public static void createNewGame() { // Sets the Board to be Initially Empty
		board = new Block [HEIGHT][WIDTH];
		Block empty = new Block(null, false);

		for(int i = 0; i < HEIGHT; i++) {
			for(int j = 0; j < WIDTH; j++) {
				board[i][j] = empty;
			}
		}

		frame.setSize(1300, 600);
		frame.setVisible(true);
		frame.setBackground(new Color(255, 236, 207) );

		if(reset == false) {
			TrashCollector gui = new TrashCollector();
			frame.add(gui);
			frame.getContentPane().add(gui);
		}

		inventory = new ArrayList<Block>();
	}

	public static void printBoard() { // Prints Board into Terminal
		System.out.println("\n");
		for(int r = 0; r < board.length; ++r) {
			for(int c = 0; c < board[0].length; ++c) {
				if(board[r][c] == null) {
					System.out.print(". ");
				}
				else
					System.out.print(board[r][c] + " ");
			}
			System.out.println();
		}
		System.out.println("\n");
	}

	public void actionPerformed(ActionEvent e) {
		frame.repaint();
	}

	@Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) { // Method that determines which way you want to move and changes the board for that movement
    	synchronized(lock) {
	    	if (!listen) { // Fixes double movement problem
	    		listen = true;
	    		return;
	    	}

	    	int direction = -1;
	    	Block empty = new Block(null, false);

	        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) { // Moving Up
	            direction = UP_VALUE;
	        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) { // Moving Right
	            direction = RIGHT_VALUE;
	        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) { // Moving Down
	            direction = DOWN_VALUE;
	        } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) { // Moving Left
	            direction = LEFT_VALUE;
	        } else if (e.getKeyCode() == GRAB_VALUE) {
	        	System.out.println("Space");
	        } else if(e.getKeyCode() == RESTART_VALUE) {
	        	endgame();
	        	return;
	        } else {
	        	return;
	        }

	        boolean moved = false;
	        for(int i = 0; i < HEIGHT; i++) {
	        	for(int j = 0; j < WIDTH; j++) {
	        		if(board[i][j].getMoveable() && !moved) {
	        			board[i][j].setOrientation(direction);
	        			int new_pos = move(i, j, direction);
	        			if (direction % 2 == 0 && board[new_pos][j].getImage() == null) {
	    					board[new_pos][j] = board[i][j];
	    					moved = true;
	        			} else if (direction % 2 == 1 && board[i][new_pos].getImage() == null) {
	        				board[i][new_pos] = board[i][j];
	        				moved = true;
	        			}
	        			
	        			// Deleting Tiles
	        			if (i != new_pos && direction % 2 == 0 && moved) {
	        				board[i][j] = empty;
	        			} else if (j != new_pos && direction % 2 == 1 && moved) {
	        				board[i][j] = empty;
	        			}
	        		}
	        	}
	        }

	        printBoard();
	        frame.repaint();
	        listen = false;
	    }
    }

	public void paintComponent(Graphics g) {
		Graphics2D gui = (Graphics2D) g;

		gui.setColor(Color.LIGHT_GRAY); // Board
		gui.fillRect(BOX_DIMENSION, BOX_DIMENSION, WIDTH * BOX_DIMENSION, HEIGHT * BOX_DIMENSION); 

		gui.setColor(Color.DARK_GRAY); // Dividing Lines
		for(int r = MARGIN; r <= WIDTH * BOX_DIMENSION + MARGIN; r = r + BOX_DIMENSION) {
			gui.fillRect(r, BOX_DIMENSION, PADDING, HEIGHT * BOX_DIMENSION);
		}
		for(int c = MARGIN; c <= HEIGHT * BOX_DIMENSION + MARGIN; c = c + BOX_DIMENSION) {
			gui.fillRect(BOX_DIMENSION, c, WIDTH * BOX_DIMENSION + PADDING, PADDING);
		}

		for(int r = 0; r < HEIGHT; r++ ) { // Blocks
			for(int c = 0; c < WIDTH; c++) {
				gui.drawImage(board[r][c].getImage(), MARGIN + PADDING + c * BOX_DIMENSION, MARGIN + PADDING + r * BOX_DIMENSION, BOX_DIMENSION - PADDING, BOX_DIMENSION - PADDING, null);
				if (board[r][c].getMoveable()) {
					if (board[r][c].getOrientation() == UP_VALUE && r != 0) {
						gui.drawImage(outlineImage, MARGIN + PADDING + c * BOX_DIMENSION, PADDING + r * BOX_DIMENSION, BOX_DIMENSION - PADDING, BOX_DIMENSION - PADDING, null);
						gui.drawImage(board[r-1][c].getImage(), MARGIN + PADDING + c * BOX_DIMENSION, PADDING + r * BOX_DIMENSION, BOX_DIMENSION - PADDING, BOX_DIMENSION - PADDING, null);
					} else if (board[r][c].getOrientation() == DOWN_VALUE && r != 4) {
						gui.drawImage(outlineImage, MARGIN + PADDING + c * BOX_DIMENSION, BOX_DIMENSION + MARGIN + PADDING + r * BOX_DIMENSION, BOX_DIMENSION - PADDING, BOX_DIMENSION - PADDING, null);
					} else if (board[r][c].getOrientation() == LEFT_VALUE && c != 0) {
						gui.drawImage(outlineImage, PADDING + c * BOX_DIMENSION, MARGIN + PADDING + r * BOX_DIMENSION, BOX_DIMENSION - PADDING, BOX_DIMENSION - PADDING, null);
						gui.drawImage(board[r][c-1].getImage(), PADDING + c * BOX_DIMENSION, MARGIN + PADDING + r * BOX_DIMENSION, BOX_DIMENSION - PADDING, BOX_DIMENSION - PADDING, null);
					} else if (board[r][c].getOrientation() == RIGHT_VALUE && c != 9) {
						gui.drawImage(outlineImage, BOX_DIMENSION + MARGIN + PADDING + c * BOX_DIMENSION, MARGIN + PADDING + r * BOX_DIMENSION, BOX_DIMENSION - PADDING, BOX_DIMENSION - PADDING, null);
					}
				}
			}
		}
	}

	private static Image loadImage(String filename) { // Loads images into frame
		Image tempImage = null;
		if (filename != null && filename != "") {
			try {
				tempImage = Toolkit.getDefaultToolkit().getImage(imageRelPath + filename);
			}
			catch (Exception e) {
				System.out.println("error getImage with toolkit unable to load filename " + filename);
			}
		}
		return tempImage;
	}

	public static void setBoard() { // Set Initial Position of board
		board[0][0] = new Block(userImage, true);
		board[0][0].setScore(-1);
		board[0][WIDTH - 1] = new Block(sinkImage, false);
		board[1][WIDTH - 1] = new Block(towelImage, false);
		board[2][WIDTH - 1] = new Block(trashImage, false);
		board[3][WIDTH - 1] = new Block(recycleImage, false);
		board[4][WIDTH - 1] = new Block(compostImage, false);
		frame.repaint(); // Changing the frame for new pieces
	}

	public static int move(int i, int j, int direction) { // Determines where the blocks will move
		if (direction == 0) {
			if (i == 0) {
				return 0;
			}
			return i - 1;
		} else if (direction == 1) {
			if (j == 9) {
				return 9;
			}
			return j + 1;
		} else if (direction == 2) {
			if (i == 4) {
				return 4;
			}
			return i + 1;
		} else {
			if (j == 0) {
				return 0;
			}
			return j - 1;
		}
	}

	public static void endgame() { // Ends the game if no open spaces left
		reset = true;
		createNewGame();
		frame.repaint();
		setBoard();
		System.out.println();
		System.out.println("You have started a new game. GOOD LUCK!");
		System.out.println();
		printBoard();
	}
}