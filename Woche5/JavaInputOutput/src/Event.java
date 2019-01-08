public class Event {
    private State STATE;
    private Action ACTION;


    public State getSTATE() {
        return STATE;
    }

    public Action getACTION() {
        return ACTION;
    }

    public Event(State state, Action action) {
        this.STATE = state;
        this.ACTION = action;
    }
}
