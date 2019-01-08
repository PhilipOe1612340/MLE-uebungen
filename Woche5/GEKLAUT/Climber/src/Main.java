
public class Main {

	public static final int CITY_COUNT = 100;
	public static final int CYCLES = 1000;
	
	public static void main(String[] args) {
		Climber climber = new Climber(CITY_COUNT, CYCLES);
		climber.climb();
		System.out.println();
		System.out.println(climber.printTable());
	}

}
