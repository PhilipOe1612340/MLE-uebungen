public class VM {
	final int MAX = 1000;
	final byte LOAD  = 0; // Reg = #1234
	final byte PUSH  = 1; // push(Reg)
	final byte POP   = 2; // Reg = pop()
	final byte MUL   = 3; // Reg = reg*pop()
	final byte DIV   = 4; // Reg = Reg/pop()
	final byte ADD   = 5; // Reg = Reg+pop()
	final byte SUB   = 6; // Reg = Reg-pop()
	final byte JIH   = 7; // if Reg>0 then pc = pc + pop()
	
	short mem[] = new short[MAX];
	short stack[] = new short[MAX];
	short sp,reg;
	int pc;
	
	VM(){
		pc = 0;	sp = 0;	reg= 0;
	}
	void push(short x){
		if (sp>=0 && sp < 1000){
			stack[sp++]=x;
		}
	}
	short pop(){
		
		if (sp>=1){
			sp--;
			
		}
		return stack[sp];
	}
	void simulate(){
		int counter = 0;
		do{
			counter++;
			switch (mem[pc]&7){
				case LOAD:{reg = (short)(mem[pc]>>3); push(reg); pc++; break;}
				case PUSH:{push(reg);  pc++; break;}
				case POP: {reg = pop();pc++;break;}
				case MUL: {reg = (short) (reg*pop());push(reg);pc++;break;}
				case DIV: {short d = pop(); if (d!=0){reg = (short) (reg/d);push(reg);}pc++;break;}
				case ADD: {reg = (short)(reg+pop());push(reg);pc++;break;}
				case SUB: {reg = (short)(reg-pop());push(reg);pc++;break;}
				case JIH: {short d=pop(); if (pc+d>=0)pc = (pc+d)%MAX; else pc++; break;}
			}
			pc = pc%MAX;
		}while(sp>=0 && counter<=10000);	
	}
}
