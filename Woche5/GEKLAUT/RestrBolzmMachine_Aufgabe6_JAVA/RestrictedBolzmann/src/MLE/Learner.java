package MLE;

import java.io.*;

public class Learner {
    static final double LEARN = 0.01;

    static final String path = "C:/Users/philip/OneDrive/Documents/Github/MLE/Woche5/GEKLAUT/RestrBolzmMachine_Aufgabe6_JAVA/RestrictedBolzmann/";

    static int INPUTS;
    static int PIXEL;
    static int PATTERNS;
    static int TEST_OFFSET = 200;

    static int trainLabel[];

    static double trainImage[][];
    static double weights[][];

    public static void main(String[] args) throws IOException, InterruptedException {
        Frame frame = new Frame();

        readMnistDatabase();

        String amend = "  --------  ";

        System.out.println(amend + "learn" + amend);
        train(10000, frame);

        System.out.println(amend + "test with new data" + amend);
        test(0, TEST_OFFSET, frame);

        System.out.println(amend + "test with learned data" + amend);
        test(TEST_OFFSET, TEST_OFFSET + 200, frame);

        System.exit(0);
    }

    public static void init(double weights[][]) {
        for (int t = 0; t < INPUTS; t++) {
            for (int neuron = 0; neuron < INPUTS; neuron++) {
                weights[neuron][t] = Math.random() * 2 - 1;
            }
        }
    }

    public static double[] activate(double in[], double w[][], boolean forward) {
        double out[] = new double[in.length];

        for (int i = 0; i < INPUTS; i++) {
            double sum = 0;
            for (int j = 0; j < INPUTS; j++) {
                int startNeuron = forward ? i : j;
                int targetNeuron = forward ? j : i;

                double inputValue = in[j];
                double weight = w[startNeuron][targetNeuron];

                // add all weighted connections together
                sum += inputValue * weight;
            }
            out[i] = sigmoid(-sum);
        }
        return out;
    }

    public static double sigmoid(double val) {
        return 1 / (1 + Math.exp(val));
    }

    public static int getResult(double[] outputs) {
        int recognizedNumber = 0;
        for (int t = 0; t < 10; t++) {
            if (outputs[PIXEL + t] > outputs[PIXEL + recognizedNumber]) {
                recognizedNumber = t;
            }
        }
        return recognizedNumber;
    }

    public static void contrastiveDivergence(double inp[], double out[], double rec[], double w[][]) {
        for (int i = 0; i < INPUTS; i++) {
            for (int j = 0; j < INPUTS; j++) {
                w[i][j] = w[i][j] - LEARN * (rec[j] - inp[j]) * out[i];
            }
        }
    }

    public static void train(int maxCount, Frame frame) {
        int correct = 0;

        init(weights);

        double[] output = new double[INPUTS];
        double[] input = new double[INPUTS];
        double[] reconstructed_input = new double[INPUTS];

        int pattern = TEST_OFFSET;
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
            input[PIXEL + (int) trainLabel[pattern]] = 1.0;

            // Contrastive divergence
            output = activate(input, weights, true);

            // negative phase reconstruction
            reconstructed_input = activate(output, weights, false);

            // adjust weights
            contrastiveDivergence(input, output, reconstructed_input, weights);

            // show progress
            if (pattern % 10 == 0) {
                String rate = (int) ((float) correct / (count + 1) * 100) + "% ";
                System.out.print("\r Nr: " + count + "   Rate: " + rate);
            }

            // search for the larges output
            int recognizedNumber = getResult(output);

            // check if recognized number is correct
            if (trainLabel[pattern] == recognizedNumber) {
                correct++;
            } else {
                if (trainLabel[pattern] == 3)
                    frame.display(input, trainLabel[pattern], reconstructed_input, recognizedNumber);
            }

            pattern = ((pattern + 1 - TEST_OFFSET) % (PATTERNS - TEST_OFFSET)) + TEST_OFFSET;
        }
        System.out.println();
    }

    public static void test(int minCount, int maxCount, Frame frame) {
        int correct = 0;

        double[] output = new double[INPUTS];
        double[] input = new double[INPUTS];

        for (int pattern = minCount; pattern < maxCount; pattern++) {

            // set image inputs
            for (int t = 0; t < PIXEL; t++) {
                input[t] = trainImage[pattern][t];
            }

            // reset outputs
            for (int t = PIXEL; t < INPUTS; t++) {
                input[t] = 0;
            }

            output = activate(input, weights, true);

            // search for the largest output
            int recognizedNumber = getResult(output);

            // check if recognized number is correct
            if (trainLabel[pattern] == recognizedNumber) {
                correct++;
            }
        }
        System.out.println("korrekt: " + correct + " von " + (maxCount - minCount));
    }

    public static void readMnistDatabase() throws IOException {
        DataInputStream labels = new DataInputStream(new FileInputStream(path + "train-labels-idx1-ubyte"));
        DataInputStream images = new DataInputStream(new FileInputStream(path + "train-images-idx3-ubyte"));

        int numImages = images.readInt();
        images.readInt();
        int numCols = images.readInt();

        PATTERNS = numImages;
        PIXEL = numCols * numCols;
        INPUTS = PIXEL + 10;

        trainLabel = new int[PATTERNS];
        trainImage = new double[PATTERNS][28 * 28];
        weights = new double[INPUTS][INPUTS];

        byte[] byteArray = new byte[PATTERNS];
        labels.read(byteArray);

        int numImagesRead = 0;
        while (images.available() > 0 && numImagesRead < PATTERNS - 9) {
            for (int colIdx = 0; colIdx < numCols; colIdx++) {
                for (int rowIdx = 0; rowIdx < numCols; rowIdx++) {
                    int value = images.readUnsignedByte();
                    int index = colIdx * numCols + rowIdx;
                    trainImage[numImagesRead][index] = ((double) (value & 0xff)) / 255;
                }
            }
            trainLabel[numImagesRead] = byteArray[numImagesRead + 8];
            numImagesRead++;
        }

        System.out.println("reading done");
        labels.close();
        images.close();
    }
}