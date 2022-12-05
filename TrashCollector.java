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
	private static Image inventoryImage;
	private static Image[] itemImages;

	private static int scoreboard;
	private static boolean reset;
	private static boolean correctBin;
	private static Block inventory;

	private static int UP_VALUE = 0;
	private static int RIGHT_VALUE = 1;
	private static int DOWN_VALUE = 2;
	private static int LEFT_VALUE = 3;
	private static int INTERACT_VALUE = 32;
	private static int RESTART_VALUE = 82;

	private static int WIDTH = 10;
	private static int HEIGHT = 5;
	private static int TIME_LENGTH = 120;

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
		correctBin = true;
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
		inventoryImage = loadImage("inventory.png");

		itemImages = new Image[15];
		itemImages[0] = loadImage("water.png"); // Recycle
		itemImages[1] = loadImage("soda.png"); // Recycle
		itemImages[2] = loadImage("dirty_aluminum.png"); // Recycle + Wash
		itemImages[3] = loadImage("dirty_plastic.png"); // Recycle + Wash
		itemImages[4] = loadImage("straw.png"); // Garbage
		itemImages[5] = loadImage("ketchup.png"); // Garbage
		itemImages[6] = loadImage("chinese.png"); // Garbage
		itemImages[7] = loadImage("black.png"); // Garbage
		itemImages[8] = loadImage("pizza.png"); // Compost
		itemImages[9] = loadImage("muffin.png"); // Compost
		itemImages[10] = loadImage("bananana.png"); // Compost
		itemImages[11] = loadImage("wet_aluminum.png");
		itemImages[12] = loadImage("wet_plastic.png");
		itemImages[13] = loadImage("aluminum.png");
		itemImages[14] = loadImage("plastic.png");

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
            int countdownStarter = TIME_LENGTH;

            public void run() {

                System.out.println(countdownStarter);
                countdownStarter--;
                updateScore(countdownStarter);

                if (countdownStarter < 0) {
                    System.out.println("Timer Over!");
                    scheduler.shutdown();
                }

                if (countdownStarter % 4 == 0) {
                	spawn(countdownStarter);
                }

                if (reset) {
                	countdownStarter = TIME_LENGTH;
                	reset = false;
                }
            }
        };
        scheduler.scheduleAtFixedRate(runnable, 0, 1, SECONDS);
	}

	public void spawn(int time) { // Spawn items randomly
		int i, j;
		while (true) {
			int number = (int)(Math.random() * (WIDTH - 1) * HEIGHT); // Random Space on the Board
			i = number / (WIDTH - 1);
			j = number % (WIDTH - 1);

			if (board[i][j].getImage() == null) {
				break;
			}
		}
		
		int item_index = (int)(Math.random() * 11);

		boolean clean = true;
		Image wetImage = null;
		Image cleanImage = null;
		if (item_index == 2 || item_index == 3)  {
			clean = false;
			wetImage = itemImages[item_index + 9];
			cleanImage = itemImages[item_index + 11];
		}

		Dispose disposeType = Dispose.COMPOST;
		if (item_index <= 3) {
			disposeType = Dispose.RECYCLE;
		} else if (item_index <= 7) {
			disposeType = Dispose.TRASH;
		}

		board[i][j] = new Block(itemImages[item_index], time, clean, disposeType, wetImage, cleanImage);
		frame.repaint();
	}

	public void updateScore(int time) {
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				board[i][j].decay(time);
			}
		}
	}

	public void interact() {
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				if (board[i][j].getMoveable() == true) {
					int r = -1;
					int c = 9;
					if (board[i][j].getOrientation() == UP_VALUE && i != 0) {
						r = i - 1;
						c = j;
					} else if (board[i][j].getOrientation() == DOWN_VALUE && i != HEIGHT - 1) {
						r = i + 1;
						c = j;
					} else if (board[i][j].getOrientation() == LEFT_VALUE && j != 0) {
						r = i;
						c = j - 1;
					} else if (board[i][j].getOrientation() == RIGHT_VALUE && j < WIDTH - 1) {
						r = i;
						c = j + 1;
					}

					if (r == 0 && c == WIDTH - 1 && !inventory.getClean()) { // Wash an item
						inventory.setClean(true);
						inventory.setDry(false);
						inventory.setImage(inventory.getWetImage());
					} else if (r == 1 && c == WIDTH - 1 && !inventory.getDry()) { // Dry an item
						inventory.setDry(true);
						inventory.setImage(inventory.getCleanImage());
					} else if (r == 2 && c == WIDTH - 1 && inventory.getImage() != null) { // Trash an item
						if (inventory.getDisposeType() != Dispose.TRASH) {
							scoreboard -= 100;
							correctBin = false;
						} else {
							scoreboard += inventory.getScore();
							correctBin = true;
						}
						inventory = new Block(null, false);
					} else if (r == 3 && c == WIDTH - 1 && inventory.getImage() != null) { // Recycle an item
						if (inventory.getDisposeType() != Dispose.RECYCLE || !inventory.getClean() || !inventory.getDry()) {
							scoreboard -= 100;
							correctBin = false;
						} else {
							scoreboard += inventory.getScore();
							correctBin = true;
						}
						inventory = new Block(null, false);
					} else if (r == 4 && c == WIDTH - 1 && inventory.getImage() != null) { // Compost an item
						if (inventory.getDisposeType() != Dispose.COMPOST) {
							scoreboard -= 100;
							correctBin = false;
						} else {
							scoreboard += inventory.getScore();
							correctBin = true;
						}
						inventory = new Block(null, false);
					} else if (c != 9 && board[r][c].getImage() != null) { // Grab an item
						Block old_inventory = inventory;
						inventory = board[r][c];
						board[r][c] = old_inventory;
					} else if (inventory.getImage() != null && board[r][c].getImage() == null) { // Drop an item
						board[r][c] = inventory;
						inventory = new Block(null, false);
					}

					System.out.println("Score: " + scoreboard);
					frame.repaint();
					return;
				}
			}
		}
	}

	public static void createNewGame() { // Sets the Board to be Initially Empty
		board = new Block[HEIGHT][WIDTH];
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

		inventory = new Block(null, false);
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
	        } else if (e.getKeyCode() == INTERACT_VALUE) {
	        	interact();
	        	listen = false;
	        	return;
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

		g.setFont(new Font("Serif", Font.PLAIN, 28));
		if (correctBin) {
			g.setColor(Color.BLACK);
		} else {
			g.setColor(Color.RED);
		}
		String textToWrite = "Score: " + scoreboard; 
		g.drawString(textToWrite, (WIDTH / 2) * BOX_DIMENSION + MARGIN / 2, MARGIN - PADDING);

		gui.setColor(Color.LIGHT_GRAY); // Board
		gui.fillRect(BOX_DIMENSION, BOX_DIMENSION, WIDTH * BOX_DIMENSION, HEIGHT * BOX_DIMENSION); 

		gui.setColor(Color.DARK_GRAY); // Dividing Lines
		for(int r = MARGIN; r <= WIDTH * BOX_DIMENSION + MARGIN; r = r + BOX_DIMENSION) {
			gui.fillRect(r, BOX_DIMENSION, PADDING, HEIGHT * BOX_DIMENSION);
		}
		for(int c = MARGIN; c <= HEIGHT * BOX_DIMENSION + MARGIN; c = c + BOX_DIMENSION) {
			gui.fillRect(BOX_DIMENSION, c, WIDTH * BOX_DIMENSION + PADDING, PADDING);
		}

		gui.drawImage(inventoryImage, 1150, 225, 200, 200, null);
		gui.drawImage(inventory.getImage(), 1200, 320, BOX_DIMENSION - PADDING, BOX_DIMENSION - PADDING, null);

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
