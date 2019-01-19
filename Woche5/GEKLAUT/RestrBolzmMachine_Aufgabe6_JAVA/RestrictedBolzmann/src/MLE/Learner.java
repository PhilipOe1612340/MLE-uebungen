package MLE;

import java.io.*;

public class Learner {
    static final double LEARN = 0.01;

    static final String path = "C:/Users/philip/OneDrive/Documents/Github/MLE/Woche5/GEKLAUT/RestrBolzmMachine_Aufgabe6_JAVA/RestrictedBolzmann/";
    static final double MIN = Double.NEGATIVE_INFINITY;

    static int INPUTS;
    static int PIXEL;
    static int PATTERNS;
    static int TEST_OFFSET;

    static int trainLabel[];

    static double trainImage[][];
    static double weights[][];
    static double bias[];

    public static void main(String[] args) throws IOException, InterruptedException {
        Frame frame = new Frame();

        readMnistDatabase();

        String amend = "  --------  ";

        System.out.println(amend + "learn" + amend);
        train(10000, frame);

        System.out.println(amend + "test with new data" + amend);
        test(0, TEST_OFFSET, frame);

        System.out.println(amend + "test with learned data" + amend);
        test(TEST_OFFSET, TEST_OFFSET * 2, frame);

        System.exit(0);
    }

    public static void init(double weights[][]) {
        for (int i = 0; i < INPUTS; i++) {
            for (int j = 0; j < INPUTS; j++) {
                setWeight(i, j, Math.random() * 0.6 - 0.3);
            }
        }
    }

    public static double[] activate(double in[]) {
        double out[] = new double[in.length];

        for (int i = 0; i < INPUTS; i++) {
            double sum = 0;
            for (int j = 0; j < INPUTS; j++) {
                double inputValue = in[j];
                double weight = getWeight(i, j);

                // add all weighted connections together
                sum += inputValue * weight;
            }
            // sum += bias[i];
            out[i] = sigmoid(-sum);
        }
        return out;
    }

    public static double sigmoid(double val) {
        return 1 / (1 + Math.exp(val));
    }

    public static double getWeight(int i, int j) {
        if (i == j)
            return 1;
        int from = i > j ? i : j;
        int to = i > j ? j : i;
        return weights[from][to];
    }

    public static void adjustWeight(int i, int j, double val) {
        int from = i > j ? i : j;
        int to = i > j ? j : i;
        weights[from][to] -= val;
    }

    public static void setWeight(int i, int j, double val) {
        int from = i > j ? i : j;
        int to = i > j ? j : i;
        weights[from][to] = val;
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

    public static void contrastiveDivergence(double inp[], double out[], double rec[]) {
        for (int i = 0; i < INPUTS; i++) {
            double b = 0;
            for (int j = 0; j < INPUTS; j++) {
                adjustWeight(i, j, LEARN * (rec[j] - inp[j]) * out[i]);
            }
            bias[i] = bias[i] + 0.1 * (b / INPUTS);
        }
    }

    public static void reset(double[] input, double[] image) {
        input[0] = 1;
        for (int t = 1; t < PIXEL + 1; t++) {
            input[t] = image[t - 1];
        }

        // reset outputs
        for (int t = PIXEL + 1; t < INPUTS; t++) {
            input[t] = 0;
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

            // setup
            reset(input, trainImage[pattern]);
            input[PIXEL + (int) trainLabel[pattern]] = 1.0;

            // learn
            output = activate(input);
            reconstructed_input = activate(output);
            contrastiveDivergence(input, output, reconstructed_input);

            // check result
            int recognizedNumber = getResult(output);

            if (trainLabel[pattern] == recognizedNumber) {
                correct++;
            } else {
                // if (trainLabel[pattern] == 3)
                frame.display(input, trainLabel[pattern], reconstructed_input, recognizedNumber, weights, output);
            }

            // show progress
            if (pattern % 10 == 0) {
                String rate = (int) ((float) correct / (float) (count + 1) * 100) + "% ";
                System.out.print("\r " + correct + "/" + count + "   Rate: " + rate);
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
            reset(input, trainImage[pattern]);
            output = activate(input);
            int recognizedNumber = getResult(output);
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
        INPUTS = PIXEL + 11;
        TEST_OFFSET = PATTERNS / 10;

        trainLabel = new int[PATTERNS];
        trainImage = new double[PATTERNS][28 * 28];
        weights = new double[INPUTS][INPUTS];
        bias = new double[INPUTS];

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