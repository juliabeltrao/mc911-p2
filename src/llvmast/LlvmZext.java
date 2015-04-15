package llvmast;

public class LlvmZext extends LlvmInstruction {
	public LlvmRegister lhs;
    public LlvmType type1, type2;
    public LlvmValue op;
    
    public LlvmZext(LlvmRegister lhs, LlvmType type1, LlvmValue op, LlvmType type2){
    	this.lhs = lhs;
    	this.type1 = type1;
    	this.op = op;
    	this.type2 = type2;
    }

    public String toString(){
		return "  " + lhs + " =  zext " + type1 + " " + op + " to " + type2;
    }
}
