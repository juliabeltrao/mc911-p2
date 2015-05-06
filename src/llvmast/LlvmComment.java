package llvmast;

public class LlvmComment extends LlvmInstruction {

	String comment = "";
	
	public LlvmComment(String s){
		if(s.startsWith("  ;"))
			comment = "  " + s;
		else
			comment = "  ;" + s;
	}
	
	public String toString(){
		return comment;
	}
	
}
