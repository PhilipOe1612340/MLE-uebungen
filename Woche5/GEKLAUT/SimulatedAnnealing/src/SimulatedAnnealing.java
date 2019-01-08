
public class SimulatedAnnealing {

	private final int CITY_COUNT;
	private final double START_TEMPERATURE;
	private final double EPSILON;
	
	private int[][] table;
	private int[] hypothesis; 
	private int lastFitness;
	private int pos1;
	private int pos2;
	
	public SimulatedAnnealing(int cityCount, double temperatue, double epsilon) {
		this.CITY_COUNT = cityCount;
		this.START_TEMPERATURE = temperatue;
		this.EPSILON = epsilon;
		this.table = new int[CITY_COUNT][CITY_COUNT];
		this.hypothesis = new int[CITY_COUNT];
		initTable();
		initHypothesis();
	}
	
	public void climb() {
		this.lastFitness = fitness();
		double temperature = this.START_TEMPERATURE;
		do {
			moveOneStepAtRandom();
			if(fitness() < lastFitness)
			System.out.println("Wahrscheinlichkeit: " + probability(temperature));
			if(fitness() > this.lastFitness || (Math.random() < probability(temperature))) {
				this.lastFitness = fitness();
			} else {
				undoMove();
			}
			temperature = temperature - this.EPSILON;
			//System.out.println(printHypothesis());
			System.out.println("Fitness: " + this.fitness());
		} while(temperature > this.EPSILON);
	}
	
	public double probability(double temperature) {
		return Math.exp((fitness()-this.lastFitness)/temperature);
	}
	
	public void moveOneStepAtRandom() {
		this.pos1 = (int) (Math.random()*CITY_COUNT);
		this.pos2 = (int) (Math.random()*CITY_COUNT);
		int temp = this.hypothesis[pos1];
		this.hypothesis[pos1] = this.hypothesis[pos2];
		this.hypothesis[pos2] = temp;
	}
	
	public void undoMove() {
		int temp = this.hypothesis[pos1];
		this.hypothesis[pos1] = this.hypothesis[pos2];
		this.hypothesis[pos2] = temp;
	}
	
	public void initTable() {
		for(int i = 0; i < this.table.length; i++) {
			for(int j = 0; j < this.table[i].length; j++) {
				this.table[i][j] = (int) (Math.random()*9+1);
				this.table[j][i] = this.table[i][j];
				this.table[i][i] = 0;
			}
		}
	}
	
	public void initHypothesis() {
		for(int i = 0; i < this.hypothesis.length; i++) {
			this.hypothesis[i] = i+1;
		}
	}
	
	public int fitness() {
		int distance = 0;
		int city1 = 0;
		int city2 = 0;
		for(int i = 0; i < this.hypothesis.length; i++) {
			city1 = hypothesis[i] - 1;
			if(i == hypothesis.length-1) {
				city2 = hypothesis[0] - 1;
			} else {
				city2 = hypothesis[i+1] - 1;
			}
			distance += this.table[city1][city2];
		}
		distance -= (2 * distance);
		return distance;
	}
	
	public String printTable() {
		String tableString = "";
		tableString += "  ";
		
		for(int i = 0; i < CITY_COUNT; i++) {
			tableString += (i+1) + " "; 
		}
		tableString += "\n";
		
		for(int i = 0; i < this.table.length; i++) {
			tableString += (i+1) + " ";
			for(int j = 0; j < this.table[i].length; j++) {
				tableString += this.table[i][j] + " ";
			}
			tableString += "\n";
		}
		return tableString;
	}
	
	public String printHypothesis() {
		String hypothesisString = "";
		for(int i = 0; i < this.hypothesis.length; i++) {
			hypothesisString += this.hypothesis[i] + " ";
		}
		hypothesisString += " : " + Math.abs(lastFitness) + " meter";
		return hypothesisString;
	}
	
}
