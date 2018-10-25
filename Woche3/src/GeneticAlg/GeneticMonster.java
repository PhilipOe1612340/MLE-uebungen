package GeneticAlg;

import java.lang.Math;
import java.util.Arrays;

public class GeneticMonster {
    static final int populationSize = 100;
    static final int crossOverRate = 80;
    static final double mutationRate = 0.1;

    static int bestFitness = 0;

    public static void main(String[] args) {
        GeneticVM[] vms = new GeneticVM[populationSize];
        for (int i = 0; i < vms.length; i++) {
            vms[i] = new GeneticVM();
        }

        for (int j = 0; j < 5000; j++) {
            // run all vms
            for (int i = 0; i < vms.length; i++) {
                vms[i].evaluate();
            }
            vms = makeNewGeneration(vms);
        }
        
        GeneticVM best = sortByFitness(vms)[0];
        best.reset();
        best.debug = true;
        best.run();
        for (int instruction : best.mem) {
            System.out.println(instruction);
        }
        System.out.println("----> " + best.fitness);
    }

    private static GeneticVM[] makeNewGeneration(GeneticVM[] vms) {
        vms = sortByFitness(vms);
        if(vms[0].fitness > bestFitness){
            System.out.println(vms[0].fitness + ": " + vms[0].primeNumbers);
            bestFitness = vms[0].fitness;
        }
        
        int fitnessSum = 0;
        for (GeneticVM vm : vms) {
            fitnessSum += vm.fitness;
        }

        if (fitnessSum > 0) {
            GeneticVM[] best = Arrays.copyOfRange(vms, 0, populationSize - crossOverRate);
            for (GeneticVM goodBoy : best) {
                goodBoy.reset();
            }

            GeneticVM[] nextGen = new GeneticVM[crossOverRate];

            for (int i = 0; i < nextGen.length; i += 2) {
                GeneticVM mama = pick(vms, fitnessSum);
                GeneticVM papa = pick(vms, fitnessSum);

                nextGen[i] = new GeneticVM(mama.cross(papa));
                nextGen[i + 1] = new GeneticVM(papa.cross(mama));
            }

            return concat(mutate(nextGen), best);
        }

        for (int i = 0; i < vms.length; i++) {
            vms[i] = new GeneticVM();
        }
        return vms;
    }

    private static GeneticVM[] sortByFitness(GeneticVM[] vms) {
        Arrays.sort(vms, (GeneticVM vm1, GeneticVM vm2) -> vm2.fitness - vm1.fitness);
        return vms;
    }

    private static GeneticVM pick(GeneticVM[] vms, int fitnessSum) {
        double rand = Math.random();
        double sum = 0;

        for (int i = 0; i < vms.length; i++) {
            GeneticVM vm = vms[i];

            sum += vm.fitness / fitnessSum;
            if (sum > rand)
                return vm;
        }
        return vms[0];
    }

    private static GeneticVM[] mutate(GeneticVM[] vms) {
        for (int i = 0; i < vms.length; i++) {
            if (Math.random() < mutationRate) {
                for (int j = 0; j < 10; j++) {
                    int randIndex = (int) (Math.random() * GeneticVM.programLength);
                    vms[i].mem[randIndex] = GeneticVM.randomInstruction();
                }
            }
        }
        return vms;
    }

    private static GeneticVM[] concat(GeneticVM[] vm1, GeneticVM[] vm2) {
        GeneticVM[] concat = new GeneticVM[vm1.length + vm2.length];
        for (int i = 0; i < concat.length; i++) {
            if (i < vm1.length) {
                concat[i] = vm1[i];
            } else {
                concat[i] = vm2[i - vm1.length];
            }
        }
        return concat;
    }

}
