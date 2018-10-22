package GeneticAlg;

import java.lang.Math;

public class GeneticMonster {
    static final int populationSize = 100;
    // static final int populationSize = 100;

    public static void main(String[] args){

        while(true){
            GeneticVM[] vms = new GeneticVM[populationSize];
            for (int i = 0; i < vms.length; i++) {
                vms[i] = new GeneticVM();
                vms[i].evaluate();
                System.out.println(vms[i].fitness);
            }

        }

    }






}
