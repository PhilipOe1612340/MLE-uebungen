package MLE;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class Frame extends JFrame {
	private static final long serialVersionUID = 1L;
	double[] input;
	double[] reconstructed_input;
	int expected;
	int actual;
	double[] bias;

	boolean fastMode = false;

	Frame() {
		super("Learner");
		setSize(900, 350);
		JButton fast = new JButton("fastMode");
		add(fast, BorderLayout.PAGE_END);
		fast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggle();
			}
		});
	}

	public void paint(Graphics g) {
		printImage(0, input, expected, g);
		printImage(300, reconstructed_input, actual, g);
		printImage(600, bias, 0, g);
	}

	public void printImage(int x, double[] image, int result, Graphics g) {
		final int blockSize = 10;
		for (int colIdx = 0; colIdx < 28; colIdx++) {
			for (int rowIdx = 0; rowIdx < 28; rowIdx++) {
				int c = (int) (image[rowIdx + colIdx * 28] * 255);
				if (c > 255)
					c = 255;
				if (c < 0)
					c = 0;
				g.setColor(new Color(c, c, c));
				g.fillRect(x + blockSize + rowIdx * blockSize, blockSize + colIdx * blockSize, blockSize, blockSize);
			}
		}
		g.setColor(Color.white);
		g.fillRect(x + 50, 29 * blockSize, 15, 15);
		g.setColor(Color.black);
		g.drawString(result + "", x + 50, blockSize + 29 * blockSize);
		return;
	}

	public void display(double[] input, int expected, double[] reconstructed_input, int actual, double[] bias) {
		if (fastMode)
			return;

		this.input = input;
		this.expected = expected;
		this.reconstructed_input = reconstructed_input;
		this.actual = actual;
		this.bias = bias;

		validate();
		setVisible(true);
		repaint();

		try {
			Thread.sleep(200);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	public void toggle() {
		fastMode = !fastMode;
	}

}
