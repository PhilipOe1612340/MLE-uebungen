
public class Main {

	public static final int CITY_COUNT = 100;
	public static final double TEMPERATURE = 200;
	public static final double EPSILON = 0.001;
	
	public static void main(String[] args) {
		SimulatedAnnealing climber = 
				new SimulatedAnnealing(CITY_COUNT, TEMPERATURE, EPSILON);
		climber.climb();

	}

}
