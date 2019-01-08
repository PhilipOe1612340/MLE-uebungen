package aufgabe05;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;

import static aufgabe05.Agent.X_BALL;
import static aufgabe05.Agent.Y_BALL;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

    private boolean fastMode = true;
    private int numOfSteps = 10;

    public static int scale = X_BALL; // 12

    public static final int imageWidth = 360;
    public static final int imageHeight = 360;
    public InputOutput inputOutput = new InputOutput(this);
    public boolean stop = false;
    ImagePanel canvas = new ImagePanel();
    ImageObserver imo = null;
    Image renderTarget = null;
    public int mousex, mousey, mousek;
    public int key;

    private int lastReward = 0;
    private int stepCounter = 0;
    private int treffer = 0;

    public MainFrame(String[] args) {
        super("PingPong");

        getContentPane().setSize(imageWidth, imageHeight);
        setSize(imageWidth + 50, imageHeight + 100);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        canvas.img = createImage(imageWidth, imageHeight);

        add(canvas);

        run();
    }

    public void run() {

        int xBall = 5, yBall = 6, xSchlaeger = 5, xV = 1, yV = 1;
        int score = 0;

        Agent agent = new Agent();

        while (!stop) {

            if (!fastMode) {
                inputOutput.fillRect(0, 0, imageWidth, imageHeight, Color.black);
                inputOutput.fillRect(xBall * 30, yBall * 30, 30, 30, Color.green);
                inputOutput.fillRect(xSchlaeger * 30, 11 * 30 + 20, 90, 10, Color.orange);
            }

            int action = agent.calcNextAction(xBall, yBall, xSchlaeger, xV, yV, lastReward);

            if (action == Agent.LEFT) {
                xSchlaeger--;
            } else if (action == Agent.RIGHT) {
                xSchlaeger++;
            } else if (action != 0) {
                System.err.println("Wrong Action = " + action);
            }

            if (xSchlaeger < 0) {
                xSchlaeger = 0;
            }
            if (xSchlaeger > X_BALL) {
                xSchlaeger = X_BALL;
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
                    // System.out.println("positive reward");
                    lastReward = 1;
                    if (!fastMode) {
                        System.out.println("TREFFER");
                    }

                    score++;
                    treffer++;
                } else {
                    // System.out.println("negative reward");
                    lastReward = -1;
                    score--;
                }

                stepCounter++;
                if (stepCounter % numOfSteps == 0) {
                    stepCounter = 0;
                    System.out.println("Score: " + score + " Treffer=" + treffer + " - " + (float) ((treffer * 100) / numOfSteps) + "%");
                    score = 0;
                    treffer = 0;
                }

            } else {
                lastReward = 0;
            }


            if (!fastMode) {
                try {
                    Thread.sleep(60);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                repaint();
            }

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

        fastMode = !fastMode;
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
        System.out.println(e.toString());
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
