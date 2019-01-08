import java.util.ArrayList;

public class Main {
	
	public static final Mode MODE = Mode.NORMAL;
	public static final int POPULATION_COUNT = 500;
	public static final double CROSSOVER_PERCENT = 0.8;
	public static final double MUTATIONRATE = 0.01;
	public static final String OPTIMUM = "101011110011001110101011110011000";
	
	public static void main(String[] args) {
		int cycles = 1;
		ArrayList<String> population = createPopulation(POPULATION_COUNT, OPTIMUM);
		ArrayList<Integer> fitness = calculateFitnessAll(population, OPTIMUM);
		int indexOfBestIndividuum = getIndexOfMaxValue(fitness);
		System.out.println("Generation: " + cycles + ", Best Individuum: " + population.get(indexOfBestIndividuum));
		while(fitness.get(indexOfBestIndividuum) < OPTIMUM.length()) {
			ArrayList<String> newPopulation = new ArrayList<String>();
			ArrayList<Double> probabilities = calculateProbabilityAll(fitness);
			
			selection(population, newPopulation, probabilities, indexOfBestIndividuum);
			crossover(population, newPopulation, probabilities);
			mutation(newPopulation);
			
			if(MODE == Mode.DEBUG) {
				System.out.println("Generation: " + cycles);
				System.out.println("Population: " + population);
				System.out.println("Fitness: " + fitness);
				System.out.println("Probabilities: " + probabilities);
				System.out.println("New Population: " + newPopulation);
				System.out.println("-----------------------------------");
			}
			
			population = newPopulation;
			fitness = calculateFitnessAll(population, OPTIMUM);
			indexOfBestIndividuum = getIndexOfMaxValue(fitness);
			cycles++;
			
			System.out.println("Generation: " + cycles + ", Best Individuum: " + population.get(indexOfBestIndividuum));
		}
	}
	
	public static void selection(ArrayList<String> population, ArrayList<String> newPopulation,
			ArrayList<Double> probabilities, int indexOfBestIndividuum) {
		boolean copiedBestIndividuum = false;
		int selectCount = (int) ((1-CROSSOVER_PERCENT) * POPULATION_COUNT);
		for(int i = 0; i < selectCount; i++) {
			int selectPopulationAtIndex = selectIndexOfPopulation(probabilities);
			if(selectPopulationAtIndex == indexOfBestIndividuum) {
				copiedBestIndividuum = true;
			}
			newPopulation.add(population.get(selectPopulationAtIndex));
		}
		if(copiedBestIndividuum) {
			newPopulation.add(population.get(selectIndexOfPopulation(probabilities)));
		} else {
			newPopulation.add(population.get(indexOfBestIndividuum));
		}
	}
	
	public static void crossover(ArrayList<String> population, ArrayList<String> newPopulation,
			ArrayList<Double> probabilities) {
		int selectCount = (int) (CROSSOVER_PERCENT * POPULATION_COUNT/2);
		for(int i = 0; i < selectCount; i++) {
			int index1 = selectIndexOfPopulation(probabilities);
			int index2 = selectIndexOfPopulation(probabilities);
			newPopulation.addAll(doCrossover(population.get(index1), 
					population.get(index2)));
		}
	}
	
	public static ArrayList<String> doCrossover(String mother, String father) {
		ArrayList<String> children = new ArrayList<String>();
		int crossoverPoint = (int) (Math.random() * mother.length());
		String child1 = mother.substring(0, crossoverPoint) + father.substring(crossoverPoint, father.length());
		String child2 = father.substring(0, crossoverPoint) + mother.substring(crossoverPoint, mother.length());
		
		children.add(child1);
		children.add(child2);
		
		if(MODE == Mode.DEBUGCROSSOVER) {
			System.out.println("Crossover: " + (crossoverPoint + 1));
			System.out.println("Mother: " + mother);
			System.out.println("Father: " + father);
			System.out.println("Child 1: " + children.get(0));
			System.out.println("Child 2: " + children.get(1));
		}
		return children;
	}
	
	public static void mutation(ArrayList<String> newPopulation) {
		ArrayList<Integer> fitness = calculateFitnessAll(newPopulation, OPTIMUM);
		int indexOfBestIndividuum = getIndexOfMaxValue(fitness);
		int selectCount = (int) (MUTATIONRATE * newPopulation.size());
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for(int i = 0; i < selectCount; i++) {
			int index = (int) (Math.random() * newPopulation.size());
			if(!indices.contains(index)) {
				indices.add(index);
				String tiltedBit = tiltBit(newPopulation.get(index));
				if(index == indexOfBestIndividuum) {
					int individuumFitness = calculateFitness(newPopulation.get(indexOfBestIndividuum), OPTIMUM);
					int tiltedFitness = calculateFitness(tiltedBit, OPTIMUM);
					if(individuumFitness < tiltedFitness) {
						newPopulation.set(index, tiltedBit);
					}
				} else {
					newPopulation.set(index, tiltedBit);
				}
			} else {
				i--;
			}
		}
	}
	
	public static String tiltBit(String bitString) {
		int tiltPos = (int) (Math.random() * bitString.length());
		StringBuilder tiltedBit = new StringBuilder(bitString);
		if(tiltedBit.charAt(tiltPos) == '0') {
			tiltedBit.setCharAt(tiltPos, '1');
		} else if(tiltedBit.charAt(tiltPos) == '1') {
			tiltedBit.setCharAt(tiltPos, '0');
		}
		return tiltedBit.toString();
	}
	
	public static ArrayList<String> createPopulation(int populationCount, String optimum) {
		ArrayList<String> population = new ArrayList<String>();
		for(int i = 0; i < populationCount; i++) {
			String individuum = "";
			for(int pos = 0; pos < optimum.length(); pos++) {
				individuum += (int) (Math.round(Math.random()));
			}
			population.add(individuum);
		}
		return population;
	}
	
	public static ArrayList<Integer> calculateFitnessAll(ArrayList<String> population, String optimum) {
		ArrayList<Integer> fitness = new ArrayList<Integer>();
		for(String individuum : population) {
			fitness.add(calculateFitness(individuum, optimum));
		}
		return fitness;
	}
	
	public static int calculateFitness(String individuum, String optimum) {
		int fitness = 0;
		for(int i = 0; i < individuum.length(); i++) {
			if(individuum.charAt(i) == optimum.charAt(i)) {
				fitness++;
			}
		}
		return fitness;
	}
	
	public static int getIndexOfMaxValue(ArrayList<Integer> list) {
		int index = -1;
		int max = 0;
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i) > max) {
				max = list.get(i);
				index = i;
			}
		}
		return index;
	}
	
	public static ArrayList<Double> calculateProbabilityAll(ArrayList<Integer> fitness) {
		ArrayList<Double> probabilities = new ArrayList<Double>();
		int allFitness = calculateAllFitness(fitness);
		for(int i = 0; i < fitness.size(); i++) {
			probabilities.add(calculateProbability(fitness.get(i), allFitness));
		}
		return probabilities;
	}
	
	public static int calculateAllFitness(ArrayList<Integer> fitness) {
		int allFitness = 0;
		for(Integer fitnessValue : fitness) {
			allFitness += fitnessValue;
		}
		return allFitness;
	}
	
	public static double calculateProbability(int fitness, int allFitness) {
		return (fitness * 1. / allFitness);
	}
	
	public static int selectIndexOfPopulation(ArrayList<Double> probabilities) {
		int index = (int) (Math.random() * probabilities.size());
		double randNum = Math.random();
		double summe = 0;
		do {
			index++;
			index = index % probabilities.size();
			summe += probabilities.get(index);
		} while(summe < randNum);
		return index;
	}
	
}
