import java.io.*;

import java.awt.*;
import javax.swing.*;

public class MNISTReader extends JFrame {
	static int m_z = 12345, m_w = 45678;
	static final int NEURONS = 28 * 28 + 10;
	static final int INPUTS = 28 * 28;
	static final int MAX_PATTERNS = 1000;
	static final String path = "C:/Users/phili/OneDrive/Documents/Github/MLE/Woche5/GEKLAUT/RestrBolzmMachine_Aufgabe6_JAVA/RestrictedBolzmann/";
	int numLabels;
	int numImages;
	int numRows;
	int numCols;

	int trainLabel[] = new int[MAX_PATTERNS];
	double trainImage[][] = new double[MAX_PATTERNS][28 * 28];
	double weights[][] = new double[NEURONS][NEURONS];
	double output[] = new double[NEURONS];
	double input[] = new double[NEURONS];
	double reconstructed_input[] = new double[NEURONS];

	int randomGen() {
		m_z = Math.abs(36969 * (m_z & 65535) + (m_z >> 16));
		m_w = Math.abs(18000 * (m_w & 65535) + (m_w >> 16));
		return Math.abs((m_z << 16) + m_w);
	}

	public void paint(Graphics g) {
		printImage(0, input, g);
		printImage(300, output, g);
		printImage(600, reconstructed_input, g);
	}

	public void printImage(int x, double[] image, Graphics g) {
		final int blockSize = 10;
		for (int colIdx = 0; colIdx < 28; colIdx++) {
			for (int rowIdx = 0; rowIdx < 28; rowIdx++) {
				int c = (int) (image[rowIdx + colIdx * 28] + 0.5);
				if (c > 0.0) {
					g.setColor(Color.black);
				} else {
					g.setColor(Color.white);
				}
				g.fillRect(x + blockSize + rowIdx * blockSize, blockSize + colIdx * blockSize, blockSize, blockSize);
			}
		}
		for (int t = 28 * 28; t < image.length; t++) {
			if (image[t] > 0.9) {
				g.setColor(Color.white);
				g.fillRect(x + 50, 29 * blockSize, 15, 15);
				g.setColor(Color.black);
				g.drawString(t - 28 * 28 + "", x + 50, blockSize + 29 * blockSize);
				return;
			}
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		MNISTReader frame = new MNISTReader();

		frame.readMnistDatabase();
		frame.setSize(900, 350);
		System.out.println("Learning step:");
		frame.trainOrTestNet(true, 1000, frame);

		System.out.println("Teststep:");
		frame.trainOrTestNet(false, 1000, frame);
	}

	/**
	 * set random weights
	 */
	public void init(double weights[][]) {
		for (int t = 0; t < NEURONS; t++) {
			for (int neuron = 0; neuron < NEURONS; neuron++) {
				weights[neuron][t] = randomGen() % 2000 / 1000.0 - 1.0;
			}
		}
	}

	public void activateForward(double in[], double w[][], double out[]) {
		for (int i = 0; i < NEURONS; i++) {
			double summe = 0;
			for (int j = 0; j < NEURONS; j++) {
				summe += in[j] * w[i][j];
			}
			out[i] = 1 / (1 + Math.exp(-summe));
		}
	}

	public void activateReconstruction(double rec[], double w[][], double out[]) {
		for (int i = 0; i < NEURONS; i++) {
			double summe = 0;
			for (int j = 0; j < NEURONS; j++) {
				summe += out[j] * w[j][i];
			}
			rec[i] = 1 / (1 + Math.exp(-summe));
		}
	}

	public void contrastiveDivergence(double inp[], double out[], double rec[], double w[][]) {
		for (int i = 0; i < NEURONS; i++) {
			for (int j = 0; j < NEURONS; j++) {
				w[i][j] = w[i][j] - 0.5 * (rec[j] - inp[j]) * out[i];
			}
		}
	}

	void trainOrTestNet(boolean train, int maxCount, MNISTReader frame) {
		int correct = 0;

		if (train)
			init(weights);

		int pattern = 0;
		for (int count = 0; count < maxCount; count++) {

			// set image inputs
			for (int t = 0; t < INPUTS; t++) {
				input[t] = trainImage[pattern][t];
			}

			// reset outputs
			for (int t = INPUTS; t < NEURONS; t++) {
				input[t] = 0;
			}

			// set label inputs for training
			if (train) {
				input[INPUTS + (int) trainLabel[pattern]] = 1.0;
			}

			// Contrastive divergence
			// TODO: explain
			activateForward(input, weights, output);

			// negative phase reconstruction
			// TODO: explain
			activateReconstruction(reconstructed_input, weights, output);

			// adjust weights 
			if (train)
				contrastiveDivergence(input, output, reconstructed_input, weights);

			// show progress
			if (count % 211 == 0 && train) {
				String rate = (float) (correct) / (float) (count) * 100 + "%";
				System.out.println("Nr: " + count + "   Rate: " + rate);

				frame.validate();
				frame.setVisible(true);
				frame.repaint();

				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}

			// search for the larges output
			int recognizedNumber = 0;
			for (int t = INPUTS; t < NEURONS; t++) {
				if (reconstructed_input[t] > reconstructed_input[INPUTS + recognizedNumber]) {
					recognizedNumber = t - INPUTS;
				}
			}

			// check if recognized number is correct
			if (frame.trainLabel[pattern] == recognizedNumber) {
				correct++;
			} else {
				System.out.println("falsch: " + recognizedNumber + " statt " + frame.trainLabel[pattern]);
			}

			pattern = (pattern + 1) % 100;
		}

		if (!train)
			System.out.println("korrekt: " + correct + " von " + maxCount);

	}

	public void readMnistDatabase() throws IOException {
		{
			DataInputStream labels = new DataInputStream(new FileInputStream(path + "train-labels-idx1-ubyte"));
			DataInputStream images = new DataInputStream(new FileInputStream(path + "train-images-idx3-ubyte"));
			int magicNumber = labels.readInt();
			if (magicNumber != 2049) {
				System.err.println("Label file has wrong magic number: " + magicNumber + " (should be 2049)");
				System.exit(0);
			}
			magicNumber = images.readInt();
			if (magicNumber != 2051) {
				System.err.println("Image file has wrong magic number: " + magicNumber + " (should be 2051)");
				System.exit(0);
			}
			numLabels = labels.readInt();
			numImages = images.readInt();
			numRows = images.readInt();
			numCols = images.readInt();

			int numLabelsRead = 0;
			int numImagesRead = 0;

			while (labels.available() > 0 && numLabelsRead < MAX_PATTERNS) { // numLabels

				byte label = labels.readByte();
				numLabelsRead++;
				trainLabel[numImagesRead] = label;
				int i = 0;
				for (int colIdx = 0; colIdx < numCols; colIdx++) {
					for (int rowIdx = 0; rowIdx < numRows; rowIdx++) {
						if (images.readUnsignedByte() > 0) {
							trainImage[numImagesRead][i++] = 1.0;
						} else {
							trainImage[numImagesRead][i++] = 0;
						}

					}
				}

				numImagesRead++;
			}

			labels.close();
			images.close();
		}

	}
}
