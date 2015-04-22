package llvmast;

public class LlvmPhi extends LlvmInstruction {
	LlvmType type;
	LlvmRegister v1, lhs;
	LlvmValue v2;
	LlvmLabelValue l1, l2;
	
	public LlvmPhi(LlvmRegister lhs, LlvmType type, LlvmRegister v1, LlvmLabelValue l1, LlvmValue v2, LlvmLabelValue l2){
		this.type = type;
		this.v1 = v1;
		this.v2 = v2;
		this.l1 = l1;
		this.l2 = l2;
		this.lhs = lhs;
	}
	
	public String toString(){
		
		return "  " + lhs + " = phi " + type + " [ " + v1 + ", %" + l1 + " ], [ " + v2 + ", %" + l2 + " ]";
	}

}
