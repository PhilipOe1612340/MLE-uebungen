import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	public static boolean display = true;
	public static int sleepTime = 100;
	public static final int imageWidth = 360;
	public static final int imageHeight = 360;
	public InputOutput inputOutput = new InputOutput(this);
	public boolean stop = false;
	ImagePanel canvas = new ImagePanel();
	ImageObserver imo = null;
	Image renderTarget = null;
	public int mousex, mousey, mousek;
	public int key;

	int maxXBall = 10;
	int maxYBall = 10;
	int maxXSchlaeger = 10;
	int maxXVel = 2;
	int maxYVel = 2;

	public double[][] q = new double[maxXBall * maxYBall * maxXSchlaeger * maxXVel * maxYVel][2]; // 5424

	//

	List<Integer> a = new ArrayList<Integer>();

	int lastState = 0;
	int lastAction = 0;

	public MainFrame(String[] args) {
		super("PingPong");

		getContentPane().setSize(imageWidth, imageHeight);
		setSize(imageWidth + 50, imageHeight + 50);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);

		canvas.img = createImage(imageWidth, imageHeight);
		add(canvas);

		run();
	}

	public int qLearning(int state, int reward) {
		double qLearning;
		// qLearning = this.q[lastState][action];
		int nextAction = selectAction(q[state]);
		if (nextAction > 1) {
			nextAction = 0;
		}
		// System.out.println("Next: " + this.q[lastState][lastAction]);
		// qLearning += 0.8 * (reward + 0.8 * getMaxQ(this.q[state+1]) - qLearning);

		this.q[lastState][lastAction] += 0.5
				* (reward + 0.5 * this.q[state][nextAction] - this.q[lastState][lastAction]);

		// this.q[lastState][lastAction] = qLearning;

		lastState = state;
		lastAction = nextAction;
		return nextAction;
	}

	public double getMaxQ(double[] q) {
		Double maxQ = null;
		for (int i = 0; i < q.length; i++) {
			if (maxQ == null) {
				maxQ = q[i];
			} else {
				if (q[i] > maxQ)
					maxQ = q[i];
			}
		}
		return maxQ;
	}

	public int getState(int xBall, int yBall, int xSchlaeger, int xV, int yV) {
		int x = 1;
		int y = 1;
		if (xV == -1) {
			x = 0;
		}
		if (yV == -1) {
			y = 0;
		}

		xV += 1;
		yV += 1;

		int state = y * 2 * 10 * 11 * 2 + x * 10 * 11 * 2 + xSchlaeger * 11 * 10 + 11 * yBall + xBall;

		return (xBall + yBall * maxYBall + xSchlaeger * (maxYBall * maxXSchlaeger)
				+ xV * (maxYBall * maxXSchlaeger * maxXVel) + yV * (maxYBall * maxXSchlaeger * maxXVel * maxYVel));
	}

	public int selectAction(double[] q) {
		int action = 0;
		Double value = null;
		for (int i = 0; i < q.length; i++) {
			if (value == null) {
				value = q[i];
				action = i;// (int) (Math.random() * 2);
			} else {
				if (q[i] > value) {
					value = q[i];
					action = i;
				}
			}
		}
		return action;
	}

	int lastrew = 0;

	public void run() {
		int xBall = 5, yBall = 6, xSchlaeger = 5, xV = 1, yV = 1;
		int score = 0;
		int fails = 0;

		while (!stop) {

			if (display) {
				inputOutput.fillRect(0, 0, imageWidth, imageHeight, Color.black);
				inputOutput.fillRect(xBall * 30, yBall * 30, 30, 30, Color.green);
				inputOutput.fillRect(xSchlaeger * 30, 11 * 30 + 20, 90, 10, Color.orange);
				inputOutput.fillText(0, 20, "Score: " + score, Color.white);
				inputOutput.fillText(0, 40, "Fails: " + fails, Color.white);
			}
			int s = getState(xBall, yBall, xSchlaeger, xV, yV);

			// int action = (int) (Math.random() * 2);
			int action = qLearning(s, lastrew);
			if (action == 0) {
				xSchlaeger--;
			}
			if (action == 1) {
				xSchlaeger++;
			}
			if (xSchlaeger < 0) {
				xSchlaeger = 0;
			}
			if (xSchlaeger > 10) {
				xSchlaeger = 10;
			}

			xBall += xV;
			yBall += yV;
			if (xBall > 9 || xBall < 1) {
				xV = -xV;
			}
			if (yBall > 10 || yBall < 1) {
				yV = -yV;
			}

			if (yBall == 11) {
				if (xSchlaeger == xBall || xSchlaeger == xBall - 1 || xSchlaeger == xBall - 2) {
					score++;
					lastrew = 1;
					// qLearning(s, action, 1);
					// positive reward
					// System.out.println("positive reward");
				} else {
					fails++;
					score = 0;
					lastrew = -1;
					// qLearning(s, action, -1);
					// negative reward
					// System.out.println("negative reward");
				}
			} else {
				lastrew = 0;
				// qLearning(s, action, 0);
			}

			try {
				Thread.sleep(sleepTime); // 1000 milliseconds is one second.
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}

			if (display)
				System.out.println(this.q[s][action]);
			repaint();
			validate();
		}

		setVisible(false);
		dispose();
	}

	public void mouseReleased(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
		mousek = e.getButton();
	}

	public void mousePressed(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
		mousek = e.getButton();
	}

	public void mouseExited(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
		mousek = e.getButton();
	}

	public void mouseEntered(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
		mousek = e.getButton();
	}

	public void mouseClicked(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
		mousek = e.getButton();
	}

	public void mouseMoved(MouseEvent e) {
		// System.out.println(e.toString());
		mousex = e.getX();
		mousey = e.getY();
		mousek = e.getButton();
	}

	public void mouseDragged(MouseEvent e) {
		mousex = e.getX();
		mousey = e.getY();
		mousek = e.getButton();
	}

	public void keyTyped(KeyEvent e) {
		key = e.getKeyCode();
	}

	public void keyReleased(KeyEvent e) {
		key = e.getKeyCode();
	}

	public void keyPressed(KeyEvent e) {
		// System.out.println(e.toString());
		if (e.getKeyCode() == 107) {
			if (sleepTime >= 10) {
				sleepTime -= 10;
			} else
				sleepTime = 1;
		} else if (e.getKeyCode() == 109) {
			sleepTime += 10;
		} else if (e.getKeyCode() == 32) {
			display = !display;
		} else {
			sleepTime = 100;
		}
	}

	/**
	 * Construct main frame
	 * 
	 * @param args passed to MainFrame
	 */
	public static void main(String[] args) {
		new MainFrame(args);
	}
}
