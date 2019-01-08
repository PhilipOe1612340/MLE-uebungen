package aufgabe05;

public class Agent {

    private final static double LEARNING_RATE = 0.5;
    private final static double RATE = 0.5;
    private final static double EPSILON = 0.0;


    private final static int NUM_OF_ACTIONS = 3;
    public final static int STAY = 0;
    public final static int LEFT = 1;
    public final static int RIGHT = 2;

    public final static int X_BALL = 10;
    public final static int Y_BALL = X_BALL;
    public final static int Y_SCHLAEGER = Y_BALL;
    public final static int X_VEL = 2;
    public final static int Y_VEL = 2;

    private double[][] Q;

    private int currentindex = 0;
    private int currentAction = 0;

    private int lastindex = 0;
    private int lastAction = 0;

    public Agent() {
        Q = new double[X_BALL * Y_BALL * Y_SCHLAEGER * X_VEL * Y_VEL][NUM_OF_ACTIONS]; // [xBall, yBall, xSchläger, xVel, yVel][Aktion]
        // Q = new double[(X_BALL + 1) * (Y_BALL + 1) * (Y_SCHLAEGER + 1) * (X_VEL + 1) * (Y_VEL + 1)][NUM_OF_ACTIONS]; // [xBall, yBall, xSchläger, xVel, yVel][Aktion]
        initActionArray();
    }

    private void initActionArray() {
        for (int i = 0; i < Q.length; i++) {
            Q[i][STAY] = Math.random(); 
            Q[i][LEFT] = Math.random(); 
            Q[i][RIGHT] = Math.random(); 
        }
    }

    public int calcNextAction(int xBall, int yBall, int xSchlaeger, int xVel, int yVel, int lastReward) {
        xVel += 1; // no neg values
        yVel += 1; // no neg values

        currentindex = xBall + yBall * Y_BALL + xSchlaeger * Y_BALL * Y_SCHLAEGER + xVel * Y_BALL * Y_SCHLAEGER * X_VEL + yVel * Y_BALL * Y_SCHLAEGER * X_VEL * Y_VEL;
        
        if (Math.random() < EPSILON) {
            currentAction = STAY;
        } else {
            currentAction = getMaxAction(currentindex);
        }

        setRewardFromLastResult(lastReward);

        lastindex = currentindex;
        lastAction = currentAction;

        return currentAction;
    }

    private int getMaxAction(int index) {
        double max = Math.max(Math.max(Q[index][STAY], Q[index][LEFT]), Q[index][RIGHT]);

        if (max == Q[index][STAY]) {
            return STAY;
        } else if (max == Q[index][LEFT]) {
            return LEFT;
        } else if (max == Q[index][RIGHT]) {
            return RIGHT;
        } else {
            System.err.println("Wrong MAX = " + max);
            return -1;
        }
    }

    public void setRewardFromLastResult(int rewardFromLastResult) {
        Q[lastindex][lastAction] += LEARNING_RATE * (rewardFromLastResult + RATE * Q[currentindex][currentAction] - Q[lastindex][lastAction]);
        // System.out.println(Q[lastindex][lastAction]);
    }
}
