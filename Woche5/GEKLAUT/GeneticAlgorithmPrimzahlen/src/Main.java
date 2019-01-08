import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {

	public static final int PROGRAM_COUNT = 100;
	public static final double CROSSOVER_PERCENT = 0.0;
	public static final double MUTATIONRATE = 0.2;
	
	public static void main(String[] args) {
		ArrayList<VM> programs = initAll();
		int cycles = 1;
		int indexOfBestProgram = 0;
		
		do {
			simulateAll(programs);
			ArrayList<Integer> fitness = fitnessAll(programs);
			indexOfBestProgram = getIndexOfBestProgram(fitness);
			ArrayList<Double> probabilities = calculateProbabilityAll(fitness);
			ArrayList<VM> newPrograms = new ArrayList<VM>();
			selection(programs, newPrograms, probabilities, indexOfBestProgram);
			mutation(newPrograms);
			
			programs = newPrograms;
			cycles++;
		} while(cycles < 1000);
		
		writeToFile(programs.get(indexOfBestProgram), 
				fitness(programs.get(indexOfBestProgram)));
		System.out.println(getStack(programs.get(indexOfBestProgram)));
		System.out.println("Fitness: " + fitness(programs.get(indexOfBestProgram)));
	}
	
	public static int primeCount(int end) {
		int primeCount = 0;
		for(int i = 0; i <= end; i++) {
			if(isPrime(i)) {
				primeCount++;
			}
		}
		return primeCount;
	}
	
	public static void selection(ArrayList<VM> population, ArrayList<VM> newPopulation,
			ArrayList<Double> probabilities, int indexOfBestIndividuum) {
		boolean copiedBestIndividuum = false;
		int selectCount = (int) ((1-CROSSOVER_PERCENT) * PROGRAM_COUNT);
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
	
	public static ArrayList<Double> calculateProbabilityAll(ArrayList<Integer> fitness) {
		ArrayList<Double> probabilities = new ArrayList<Double>();
		int allFitness = calculateAllFitness(fitness);
		for(int i = 0; i < fitness.size(); i++) {
			probabilities.add(calculateProbability(fitness.get(i), allFitness));
		}
		return probabilities;
	}
	
	public static void mutation(ArrayList<VM> newPrograms) {
		ArrayList<Integer> fitness = fitnessAll(newPrograms);
		int indexOfBestProgram = getIndexOfBestProgram(fitness);
		int selectCount = (int) (MUTATIONRATE * newPrograms.size());
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for(int i = 0; i < selectCount; i++) {
			int index = (int) (Math.random() * newPrograms.size());
			if(!indices.contains(index)) {
				indices.add(index);
				VM mutatedProgram = mutateProgram(newPrograms.get(index));
				if(index == indexOfBestProgram) {
					int individuumFitness = fitness(newPrograms.get(indexOfBestProgram));
					int mutatedFitness = fitness(mutatedProgram);
					if(individuumFitness < mutatedFitness) {
						newPrograms.set(index, mutatedProgram);
					}
				} else {
					newPrograms.set(index, mutatedProgram);
				}
			} else {
				i--;
			}
		}
	}
	
	public static VM mutateProgram(VM program) {
		VM mutatedProgram = new VM();
		mutatedProgram.mem = program.mem.clone();
		int index = (int) (Math.random() * mutatedProgram.mem.length);
		mutatedProgram.mem[index] = (short) (Math.random() * 1000);
		return mutatedProgram;
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
	
	private static ArrayList<VM> initAll() {
		ArrayList<VM> programs = new ArrayList<VM>();
		for(int i = 0; i < 1000; i++) {
			VM vm = new VM();
			init(vm);
			programs.add(vm);
		}
		return programs;
	}
	
	private static void init(VM vm) {
		short n = (short) (Math.random()*1000);
		for (int i = 0; i < vm.mem.length; i++) {
			vm.mem[i] = (short) (Math.random() * 1000);
		}
		for(int i = 0; i < vm.stack.length; i++) {
			vm.stack[i] = n;
		}
	}

	private static void simulateAll(ArrayList<VM> vms) {
		for(VM vm : vms) {
			vm.simulate();
		}
	}
	
	private static ArrayList<Integer> fitnessAll(ArrayList<VM> vms) {
		ArrayList<Integer> fitness = new ArrayList<Integer>();
		for(VM vm : vms) {
			fitness.add(fitness(vm));
		}
		return fitness;
	}
	
	private static int fitness(VM vm) {
		Set<Integer> primeNumbers = new HashSet<Integer>();
		for (int number : vm.stack) {
			if (isPrime(number))
				number = Math.abs(number);
				primeNumbers.add(number);
		}
		return primeNumbers.size();
	}

	private static boolean isPrime(int n) {
		if (n % 2 == 0)
			return false;
		for (int i = 3; i * i <= n; i += 2) {
			if (n % i == 0)
				return false;
		}
		return true;
	}

	private static int getIndexOfBestProgram(ArrayList<Integer> fitness) {
		int index = 0;
		int max = 0;
		for(int i = 0; i < fitness.size(); i++) {
			if(max < fitness.get(i)) {
				max = fitness.get(i);
				index = i;
			}
		}
		return index;
	}
	
	private static String getStack(VM vm) {
		String s = "";
		for (int number : vm.stack) {
			s += number + " ";
		}
		return s;
	}

	private static void writeToFile(VM vm, int fitness) {
		try {
			File file = new File("fitness/program-" + fitness + ".txt");
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			String program = "";
			for(int statement : vm.mem) {
				int value = statement >> 3;
				switch(statement&7) {
					case 0:
						program += "LOAD ";
						break;
					case 1:
						program += "PUSH ";
						break;
					case 2:
						program += "POP ";
						break;
					case 3:
						program += "MUL ";
						break;
					case 4:
						program += "DIV ";
						break;
					case 5:
						program += "ADD ";
						break;
					case 6:
						program += "SUB ";
						break;
					case 7: 
						program += "JIH ";
						break;
					default:
						program += "ERROR";
				}
				program += value + System.getProperty("line.separator");
			}
			writer.write(program);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
