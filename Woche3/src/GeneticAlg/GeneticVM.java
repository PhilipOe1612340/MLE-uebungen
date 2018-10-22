package GeneticAlg;

public class GeneticVM extends VM {
    static final int programLength = 10;
    public int fitness = 0;

    public GeneticVM() {
        super(generateProgram());
    }

    public GeneticVM(int[] mem) {
        super(mem);
    }

    public void evaluate() {
        fitness = run();
    }

    static int[] generateProgram() {
        int[] mem = new int[programLength];
        mem[0] = 13 << 3;
        for (int i = 1; i < mem.length; i++) {
            // int nextInstruction = (int) Math.random() * 8;
            // int bullshit = 13 << 3;
            // mem[i] = bullshit ^ nextInstruction;
            mem[i] = 1;
        }
        return mem;
    }
}