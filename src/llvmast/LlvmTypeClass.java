package llvmast;

public class LlvmTypeClass extends LlvmType {

	private String s;
	
	public LlvmTypeClass(String name){
		s = name;
	}
	
	public String toString(){
		return "%class." + s;
	}
	
}
