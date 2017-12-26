import java.awt.EventQueue;

import javax.swing.JFrame;

public class Pong1 {

	private JFrame frame;
	private final int width = 700, height = 700, window_height=750;
	private final int handle_width = 20, handle_height = 100, radius = 8;
	private final int pong_speed = 2, handle_speed = 4;
	
	public int ballx = width/2, bally = height/2;
	public int p2x = width - handle_width - 5, p2y = height/2, p1x = 0, p1y = height/2;
	public int score1 = 0, score2 = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Pong1 window = new Pong1();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Pong1() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		//frame.setBounds(100, 100, 450, 300);
		frame.setSize(width, window_height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}

}
