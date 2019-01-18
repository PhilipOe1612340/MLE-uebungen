import java.io.*;

import java.awt.*;
import javax.swing.*;

public class MNISTReader extends JFrame {
	private static final long serialVersionUID = 1L;
	static int m_z = 12345, m_w = 45678;
	static int INPUTS = 28 * 28 + 10;
	static int PIXEL = 28 * 28;
	static int PATTERNS = 1000;
	static final String path = "C:/Users/philip/OneDrive/Documents/Github/MLE/Woche5/GEKLAUT/RestrBolzmMachine_Aufgabe6_JAVA/RestrictedBolzmann/";
	int numLabels;
	int numImages;
	int numRows;
	int numCols;

	int trainLabel[];
	double trainImage[][];
	double weights[][];
	double output[];
	double input[];
	double reconstructed_input[];

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
				int c = (int) (image[rowIdx + colIdx * 28] * 255);
				g.setColor(new Color(c, c, c));
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
		frame.trainOrTestNet(true, 10000, frame);

		System.out.println("Teststep:");
		frame.trainOrTestNet(false, 1000, frame);
	}

	/**
	 * set random weights
	 */
	public void init(double weights[][]) {
		for (int t = 0; t < INPUTS; t++) {
			for (int neuron = 0; neuron < INPUTS; neuron++) {
				weights[neuron][t] = randomGen() % 2000 / 1000.0 - 1.0;
			}
		}
	}

	public void activateForward(double in[], double w[][], double out[]) {
		for (int i = 0; i < INPUTS; i++) {
			double summe = 0;
			for (int j = 0; j < INPUTS; j++) {
				summe += in[j] * w[i][j];
			}
			out[i] = 1 / (1 + Math.exp(-summe));
		}
	}

	public void activateReconstruction(double rec[], double w[][], double out[]) {
		for (int i = 0; i < INPUTS; i++) {
			double summe = 0;
			for (int j = 0; j < INPUTS; j++) {
				summe += out[j] * w[j][i];
			}
			rec[i] = 1 / (1 + Math.exp(-summe));
		}
	}

	public void contrastiveDivergence(double inp[], double out[], double rec[], double w[][]) {
		for (int i = 0; i < INPUTS; i++) {
			for (int j = 0; j < INPUTS; j++) {
				w[i][j] = w[i][j] - 0.1 * (rec[j] - inp[j]) * out[i];
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
			for (int t = 0; t < PIXEL; t++) {
				input[t] = trainImage[pattern][t];
			}

			// reset outputs
			for (int t = PIXEL; t < INPUTS; t++) {
				input[t] = 0;
			}

			// set label inputs for training
			if (train) {
				input[PIXEL + (int) trainLabel[pattern]] = 1.0;
			}

			// Contrastive divergence
			activateForward(input, weights, output);

			// negative phase reconstruction
			activateReconstruction(reconstructed_input, weights, output);

			// adjust weights
			if (train)
				contrastiveDivergence(input, output, reconstructed_input, weights);

			// show progress

			if (pattern % 211 == 0 && train) {
				String rate = (float) (correct) / (float) (count) * 100 + "%";
				System.out.println("Nr: " + count + "   Rate: " + rate);
			}

			// search for the larges output
			int recognizedNumber = 0;
			for (int t = PIXEL; t < INPUTS; t++) {
				if (reconstructed_input[t] > reconstructed_input[PIXEL + recognizedNumber]) {
					recognizedNumber = t - PIXEL;
				}
			}

			// check if recognized number is correct
			if (frame.trainLabel[pattern] == recognizedNumber) {
				correct++;
			} else {
				System.out.println("falsch: " + recognizedNumber + " statt " + frame.trainLabel[pattern]);
				if (train && frame.trainLabel[pattern] == 3)
					display(frame);
			}

			pattern = (pattern + 1) % PATTERNS;
		}

		if (!train)
			System.out.println("korrekt: " + correct + " von " + maxCount);

	}

	public void display(MNISTReader frame) {
		frame.validate();
		frame.setVisible(true);
		frame.repaint();

		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	public void readMnistDatabase() throws IOException {
		{
			DataInputStream labels = new DataInputStream(new FileInputStream(path + "train-labels-idx1-ubyte"));
			DataInputStream images = new DataInputStream(new FileInputStream(path + "train-images-idx3-ubyte"));

			numLabels = labels.readInt();

			numImages = images.readInt();
			numRows = images.readInt();
			numCols = images.readInt();

			PATTERNS = numImages < numLabels ? numLabels : numLabels;
			PIXEL = numCols * numCols;
			INPUTS = PIXEL + 10;

			trainLabel = new int[PATTERNS];
			trainImage = new double[PATTERNS][28 * 28];
			weights = new double[INPUTS][INPUTS];
			output = new double[INPUTS];
			input = new double[INPUTS];
			reconstructed_input = new double[INPUTS];

			byte[] byteArray = new byte[PATTERNS];
			labels.read(byteArray);

			int numImagesRead = 0;
			while (images.available() > 0 && numImagesRead < PATTERNS - 5) {
				for (int colIdx = 0; colIdx < numCols; colIdx++) {
					for (int rowIdx = 0; rowIdx < numCols; rowIdx++) {
						int value = images.readUnsignedByte();
						int index = colIdx * numCols + rowIdx;
						trainImage[numImagesRead][index] = ((double) (value & 0xff)) / 255;
					}
				}
				trainLabel[numImagesRead] = byteArray[numImagesRead + 4];
				numImagesRead++;
			}

			System.out.println("reading done");
			labels.close();
			images.close();
		}

	}
}
