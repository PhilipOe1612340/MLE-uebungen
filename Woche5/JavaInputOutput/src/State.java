import java.util.Objects;

public class State {

    private int X_BALL;
    private int Y_BALL;
    private int X_SCHLAEGER;
    private int X_BALL_VEL;
    private int Y_BALL_VEL;

    public int getX_BALL() {
        return X_BALL;
    }

    public int getY_BALL() {
        return Y_BALL;
    }

    public int getX_SCHLAEGER() {
        return X_SCHLAEGER;
    }

    public int getX_BALL_VEL() {
        return X_BALL_VEL;
    }

    public int getY_BALL_VEL() {
        return Y_BALL_VEL;
    }

    public State(int xBall, int yBall, int xSchlaeger, int xBallVel, int yBallVel) {
        this.X_BALL = xBall;
        this.Y_BALL = yBall;
        this.X_SCHLAEGER = xSchlaeger;
        this.X_BALL_VEL = xBallVel;
        this.Y_BALL_VEL = yBallVel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(X_BALL, Y_BALL, X_SCHLAEGER, X_BALL_VEL, Y_BALL_VEL);
    }
}
