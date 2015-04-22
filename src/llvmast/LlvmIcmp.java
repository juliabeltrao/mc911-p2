package llvmast;
public  class LlvmIcmp extends LlvmInstruction{
	public LlvmRegister lhs;
    public LlvmType type;
    public LlvmValue op1, op2;
    public int conditionCode;
    
    public static final int NE  = 0;
    public static final int LT  = 1;
    public static final int EQ  = 2;
    
    public LlvmIcmp(LlvmRegister lhs,  int conditionCode, LlvmType type, LlvmValue op1, LlvmValue op2){
    	this.lhs = lhs;
    	this.type = type;
    	this.op1 = op1;
    	this.op2 = op2;
    	this.conditionCode = conditionCode;
    }

    public String toString(){
		switch(conditionCode){
			case NE :
				return "  " + lhs + " = icmp ne " + type + " " + op1 + ", 0";
			case LT :
				return "  " + lhs + " = icmp slt " + type + " " + op1 + ", " + op2;
			case EQ :
				return "  " + lhs + " = icmp eq " + type + " " + op1 + ", " + op2;
			default :
				return null;
		}
    }
}