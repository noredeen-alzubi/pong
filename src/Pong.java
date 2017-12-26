import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Pong extends JPanel implements KeyListener, ActionListener, Runnable {
	private final int width = 700, height = 700, window_height = 750;
	private final int handle_width = 20, handle_height = 100;
	private int AI_handle_height = 100;
	private final int radius = 8;
	// initially set to level 1
	public int pong_speedx = 2, pong_speedy = 1, handle_speed = 4, AI_handle_speed = 2;
	//position initially in the middle
	public int ballx = width / 2, bally = height / 2;
	//position is centered vertically and at the sides horizontally
	// minus the handle width because the coordinate is at the top left corner of the rectangle -> if we don't
	//subtract this then the handle/paddle on the right side will not be visible(go over the edge) so width must be added
	public int p2x = width - handle_width - radius, p2y = height / 2, p1x = 0, p1y = height / 2;
	public int scoreAI = 0, scorePL = 0;
	public boolean up, down, up2, down2;
	Random rand = new Random();
	public int randNum;
	public int hitCount = 0;
	
	public boolean active = false;
	public int max_x = 6, min_x = 4, max_y = 3, min_y = 2;
	public String status = "Press space to start";
	public int shot_rect_x, shot_rect_y, shot_width = 50, shot_height = 5;
	public boolean shot = false;
	public int shot_speed = 7;
	Graphics gshot;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pong game = new Pong();
		game.start_game();
	}

	public void start_game() {
		// UI ---------------------------------
		JFrame frame = new JFrame();
		JButton button = new JButton("restart");
		frame.setSize(width, window_height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.add(button, BorderLayout.SOUTH);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		button.addActionListener(this);
		
		// restart button
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				// reset game with scores. speeds back to initial value. back to initial
				// positions using reinitialize
				scoreAI = 0;
				scorePL = 0;
				max_x = 6;
				min_x = 4;
				max_y = 3;
				min_y = 2;
				active = false;
				hitCount = 0;
				reinitialize();
			}
		});
		this.addKeyListener(this);
		this.setFocusable(true);
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {
		// closing the graphics window will end the program
		while (true) {
			// waits for player to press space bar -> active becomes true and game resumes
			//active controls whether the game is stopped or not
			//how the game works:
			//every frame, the ball and handles will be moved(player handle according to input and computer according to algorithm),
			//checked for collisions or points, checked for input to fire projectiles, and projectile hits.
			//all of this could be interrupted by manipulating active.
			if (active) {
				status = "";
				repaint();
				move_ball();
				move_handles();
				check_collisions_rebound();
				AI_move_handle();
				reach_edge_restart();
				shot_move();
				shot_hit();
				// rests for a hundredth of a second
				try {
					Thread.sleep(10);
				} catch (Exception ex) {
				}
			} else {
				// shot does not freeze in mid air. stays moving until it leaves the boundaries.
				shot_move();
				if (scorePL == 4) {
					// Extreme difficulty
					status = "Press space to start. Good Luck beating this.";
				} else {
					status = "Press space to start";
				}
				repaint();
			}
			// draws the game

		}
	}

	public void paint(Graphics g) {
		// background color is gray
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, width, height);

		// draws the ball blue
		g.setColor(Color.blue);
		g.fillOval(ballx, bally, radius, radius);

		// draws the handles black
		g.setColor(Color.black);
		g.fill3DRect(p1x, p1y, handle_width, AI_handle_height, true);
		g.fill3DRect(p2x, p2y, handle_width, handle_height, true);

		// writes the score of the game
		Font f = new Font("Arial", Font.BOLD, 14);
		g.setFont(f);
		g.setColor(Color.red);
		g.drawString("AI Score: " + scoreAI, width / 5, 20);
		g.drawString("PL Score: " + scorePL, (width * 3 / 5), 20);
		// -100 for position
		g.drawString(status, width / 2 - 100, 60);
		if (shot = true) {
			// draw the rect only when the user has pressed F.
			//only supports single shots.
			g.fill3DRect(shot_rect_x, shot_rect_y, shot_width, shot_height, true);
		}
		repaint();
	}

	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// keyReleased and keyPressed used together to make handle movement smooth +
		// switching from up arrow to down simultaneously.
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UP) {
			up = false;
		} else if (keyCode == KeyEvent.VK_DOWN) {
			down = false;
		}

	}

	public void shot_move() {
		if (shot) {
			//move across canvas
			shot_rect_x -= shot_speed;
		}
	}

	public void shot_hit() {
		//means: if the projectile has hit the handle/paddle on the left side.
		//same as before, the handle width and height added because the coordinate for the handles is at the top left corner
		if (shot_rect_x <= (p1x + handle_width) && shot_rect_x >= (0.5 * handle_width)
				&& (shot_rect_y >= (p1y - 4) && (shot_rect_y + shot_height) <= (p1y + AI_handle_height + 4))) {
			AI_handle_height -= 20;
		}
	}

	public void restart() {
		requestFocus(true);
		repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// used with keyReleased for fluid transition between up and down directions and
		// keys.
		int keyCode = e.getKeyCode();

		// changes handle direction based on what button is pressed
		if (keyCode == KeyEvent.VK_DOWN) {
			up = false;
			down = true;
		}
		if (keyCode == KeyEvent.VK_UP) {
			up = true;
			down = false;
		}
		if (keyCode == KeyEvent.VK_SPACE) {
			// start game / resume
			active = true;
		}
		if (keyCode == KeyEvent.VK_F) {
			// shoot
			shot = true;
			shot_rect_x = p2x;
			shot_rect_y = p2y + (int) (0.5 * handle_height);

		}

	}

	public void actionPerformed(ActionEvent e) {
		// distinguish between space bar and restart button
		String str = e.getActionCommand();
		if (str.equals("restart"))
			restart();

	}

	public void move_handles() {
		// controlling the player handle.
		// at bottom -> only allowed to move up
		if (p2y > height - handle_height) {
			if (up == true) {
				p2y -= handle_speed;
			}
			// at the top -> only allowed to move down
		} else if (p2y < 0) {
			if (down == true) {
				p2y += handle_speed;
			}
			// in the middle of the screen -> move freely.
		} else {
			if (up == true) {
				p2y -= handle_speed;
			} else if (down == true) {
				p2y += handle_speed;
			}
		}

	}

	// ball movement.
	public void move_ball() {
		ballx += pong_speedx;
		bally += pong_speedy;
	}

	// balls hit the boundary
	public void reach_edge_restart() {
		// right boundary + buffer(at player handle) -> 1 point for AI
		if ((ballx + radius) > (p2x + (0.5 * handle_width))) {
			// point for Ai
			// when point scored, reset positions and values, stop detecting active shot and
			// pause game
			shot = false;
			active = false;
			scoreAI++;
			reinitialize();
		} else if (ballx < (p1x + (0.5 * handle_width))) {
			// left boundary + buffer(at AI handle) -> 1 point for player
			// point for player
			// same as above
			shot = false;
			active = false;
			scorePL++;
			reinitialize();

		}

	}

	public void reinitialize() {
		// reset positions, but not scores -> reset, not restart game
		ballx = width / 2;
		bally = height / 2;
		p1x = 0;
		p2x = width - handle_width - 5;
		p1y = height / 2;
		p2y = height / 2;
		hitCount = 0;
		pong_speedx = 2;
		pong_speedy = 1;
		shot = false;
		AI_handle_height = 100;
		repaint();
	}

	public void check_collisions_rebound() {
		// ball hit at player handle
		if ((ballx + radius) >= p2x && (ballx + radius) <= (width - (0.5 * handle_width))
				&& (bally >= (p2y - 4) && (bally + radius) <= (p2y + handle_height + 4))) {

			if (hitCount == 0) {
				// first time hitting(opening rebound)? then make the speed 3
				// game mechanic to ensure that game does not end from the beginning because
				// random numbers are used for speed.
				// these values will not make the AI lose.
				pong_speedx = 3;
				// abs to get either -1 or 1 based on direction.
				pong_speedy = (Math.abs(pong_speedy) / pong_speedy);
				hitCount++;
			} else {
				if (scorePL == 2) {
					// score is 2 -> increase difficulty and speeds
					// random range higher
					max_x = 7;
					min_x = 5;
					max_y = 4;
				} else if (scorePL == 4) {
					// hardest unbeatable level
					AI_handle_speed = 4;

				}
				// randomize x and y values after first shot
				randNum = rand.nextInt((max_x - min_x) + 1) + min_x;
				pong_speedx = -1 * randNum;
				randNum = rand.nextInt((max_y - min_y) + 1) + min_y;
				// abs equation to get either -1 or 1 to decide direction of ball based on its
				// values.
				pong_speedy = (Math.abs(pong_speedy) / pong_speedy) * randNum;
				hitCount++;
			}

		}
		// same for AI handle
		if ((ballx) <= (p1x + handle_width) && ballx >= (0.5 * handle_width)
				&& (bally >= (p1y - 4) && (bally + radius) <= (p1y + AI_handle_height + 4))) {
			randNum = rand.nextInt((max_x - min_x) + 1) + min_x;
			pong_speedx = randNum;
			randNum = rand.nextInt((max_y - min_y) + 1) + min_y;
			pong_speedy = (Math.abs(pong_speedy) / pong_speedy) * randNum;
		}
		if (bally >= height || (bally + radius) <= 0) {
			pong_speedy = -1 * pong_speedy;
		}
	}

	public void AI_move_handle() {
		// ball is below center of handle -> move down. ball above center of handle ->
		// move up
		//simple algorithm
		if (p1y >= height - AI_handle_height) {
			if ((bally + radius) < (p1y + (0.5 * AI_handle_height))) {
				p1y -= AI_handle_speed;
			}
		} else if (p1y <= 0) {
			if (bally > (p1y + (0.5 * AI_handle_height))) {
				p1y += AI_handle_speed;
			}
		} else {
			if (bally > (p1y + (0.5 * AI_handle_height))) {

				p1y += AI_handle_speed;
			} else if ((bally + radius) < (p1y + (0.5 * AI_handle_height))) {
				p1y -= AI_handle_speed;
			}
		}

	}

}
