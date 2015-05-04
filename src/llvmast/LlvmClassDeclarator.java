package llvmast;

import java.util.List;

public class LlvmClassDeclarator extends LlvmInstruction {

	public String name;
	public int methodCount;
    public List<LlvmType> typeList;
    
    public LlvmClassDeclarator(List<LlvmType> typeList, String name, int methodCount){
    	this.typeList = typeList;
    	this.name = name;
    	this.methodCount = methodCount;
    }
    
    public String toString() {
    	
    	String S = "%class." + name + " = type { ";
    	
    	if(methodCount > 0)
    		S += "[" + methodCount + " x i8 *]";
    	
    	if(!typeList.isEmpty())
    		S += ", " + typeList.get(0);
    	for (int i = 1; i < typeList.size(); i++){
			S += ", "+typeList.get(i).toString();
		}
    	S += " }";
    	
    	return S;
    }
	
}
