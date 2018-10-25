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

        // load 0
        mem[0] = 0;

        /* 
         * Instruction          
         * LOAD = 0     000  |  13 bit number
         * PUSH = 1     001  |  push reg to stack           
         * POP = 2      010  |  pop from stack into reg           
         * MUL = 3      011  |  reg * stack[0]           
         * DIV = 4      100  |  reg / stack[0]           
         * ADD = 5      101  |  reg + stack[0]           
         * SUB = 6      110  |  reg - stack[0]           
         * JIH = 7      111  |  if (reg > 0): set pc forward by stack[0]      
         */

        for (int i = 1; i < mem.length; i++) {
            // int nextInstruction = (int) Math.random() * 8;
            // int bullshit = 13 << 3;
            // mem[i] = bullshit ^ nextInstruction;
            mem[i] = 1;
        }
        return mem;
    }
}