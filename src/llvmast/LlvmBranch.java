package llvmast;
public  class LlvmBranch extends LlvmInstruction{

	String s = "";
	
    public LlvmBranch(LlvmLabelValue label){
    	s = "  " + "br label " + label;
    }
    
    public LlvmBranch(LlvmValue cond,  LlvmLabelValue brTrue, LlvmLabelValue brFalse){
    	s = "  " + "br i1 " + cond + ", label " + brTrue + ", label" + brFalse;
    }

    public String toString(){
		return s;
    }
}