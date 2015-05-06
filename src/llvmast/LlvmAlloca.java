package llvmast;
import java.util.*;
public  class LlvmAlloca extends LlvmInstruction{
    public LlvmValue lhs;
    public LlvmType type;
    public List<LlvmValue> numbers;
    String s = "";

    public LlvmAlloca(LlvmValue lhs, LlvmType type, List<LlvmValue> numbers){
    	for(LlvmValue v : numbers)
		    s = s + ", " + v.type + " " + v;
    	s = "  " + lhs + " = alloca " + type + s;
    }
    
    public LlvmAlloca(LlvmValue lhs, LlvmType type){
    	s = "  " + lhs + " = alloca " + type;
    }

    public String toString(){
		return s;
    }
}
