import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Learner {
    private Map<State, double[]> states;
    private List<Event> lastActions = new LinkedList<>();
    private final double LEARNING = 0.4;
    private final double EPSILON = 0.001;

    public Learner() {
        states = new HashMap<>();
    }

    public Action getBestAction(State state) {
        double random = Math.random();
        Action action;
        if (EPSILON > random) {
            int randomIndex = (int) (Math.random() * 2.999);
            action = Action.values()[randomIndex];
        } else {
            int index = getMax(getEvaluation(state));
            action = Action.values()[index];
        }
        lastActions.add(new Event(state, action));
        return action;

    }

    private int getMax(double[] array) {
        int max = 0;
        for (int i = 0; i < array.length; i++) {
            max = array[max] < array[i] ? i : max;
        }
        return max;
    }

    public void reward(int r) {
        for (Event event : lastActions) {
            double[] evaluation = getEvaluation(event.getSTATE());
            int index = event.getACTION().ordinal();
            evaluation[index] += r * LEARNING;
            states.put(event.getSTATE(), evaluation);
        }
        lastActions.clear();
    }

    private double[] getEvaluation(State state){
        double[] defaultValues = { Math.random(), Math.random(), Math.random() };
        return states.getOrDefault(state, defaultValues);
    }

}
