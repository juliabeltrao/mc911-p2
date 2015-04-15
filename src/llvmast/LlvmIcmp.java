package llvmast;
public  class LlvmIcmp extends LlvmInstruction{
	public LlvmRegister lhs;
    public LlvmType type;
    public LlvmValue op1, op2;
    public int conditionCode;
    
    public static final int NE  = 0;
    
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
				return "  " +lhs + " = icmp ne " + type + " " + op1 + ", 0";
			default:
				return null;
		}
    }
}