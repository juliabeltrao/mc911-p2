package llvmast;

public class LlvmPhi extends LlvmInstruction {
	LlvmType type;
	util.List<LlvmValue> valList;
	util.List<LlvmLabel> valLabel;
	
	LlvmPhi(LlvmType type, util.List<LlvmValue> valList, util.List<LlvmLabel> valLabel){
		this.type = type;
		this.valList = valList;
		this.valLabel = valLabel;
	}
	
	public String toString(){
		
		String s = "  " + "phi" + type;
		util.List<LlvmValue> i;
		util.List<LlvmLabel> j;
		
		for(i = valList, j = valLabel; i != null && j != null; i = i.tail, j = j.tail){
			s += ", [ " + i + ", " + j + " ]";
		}
		
		return s.replaceFirst(",", "");
	}

}
