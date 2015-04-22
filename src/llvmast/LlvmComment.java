package llvmast;

public class LlvmComment extends LlvmInstruction {

	String comment = "";
	
	public LlvmComment(String s){
		if(s.startsWith("  ;"))
			comment = "\n  " + s;
		else
			comment = "\n  ;" + s;
	}
	
	public String toString(){
		return comment;
	}
	
}
