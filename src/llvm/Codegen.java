/*****************************************************
Esta classe Codegen é a responsável por emitir LLVM-IR. 
Ela possui o mesmo método 'visit' sobrecarregado de
acordo com o tipo do parâmetro. Se o parâmentro for
do tipo 'While', o 'visit' emitirá código LLVM-IR que 
representa este comportamento. 
Alguns métodos 'visit' já estão prontos e, por isso,
a compilação do código abaixo já é possível.

class a{
    public static void main(String[] args){
    	System.out.println(1+2);
    }
}

O pacote 'llvmast' possui estruturas simples 
que auxiliam a geração de código em LLVM-IR. Quase todas 
as classes estão prontas; apenas as seguintes precisam ser 
implementadas: 

// llvmasm/LlvmBranch.java
// llvmasm/LlvmIcmp.java
// llvmasm/LlvmMinus.java
// llvmasm/LlvmTimes.java


Todas as assinaturas de métodos e construtores 
necessárias já estão lá. 


Observem todos os métodos e classes já implementados
e o manual do LLVM-IR (http://llvm.org/docs/LangRef.html) 
como guia no desenvolvimento deste projeto. 

****************************************************/
package llvm;

import semant.Env;
import syntaxtree.*;
import llvmast.*;
import java.util.*;

public class Codegen extends VisitorAdapter{
	private List<LlvmInstruction> assembler;
	private Codegen codeGenerator;

  	private SymTab symTab;
	private ClassNode classEnv; 	// Aponta para a classe atualmente em uso em symTab
	private MethodNode methodEnv; 	// Aponta para a metodo atualmente em uso em symTab
	private int labelCount = 0;		// contador para gerar labels unicas
	
	public Codegen(){
		assembler = new LinkedList<LlvmInstruction>();
	}

	// Método de entrada do Codegen
	public String translate(Program p, Env env){	
		codeGenerator = new Codegen();
		
		// Preenchendo a Tabela de Símbolos
		// Quem quiser usar 'env', apenas comente essa linha
		// codeGenerator.symTab.FillTabSymbol(p);
		
		// Formato da String para o System.out.printlnijava "%d\n"
		codeGenerator.assembler.add(new LlvmConstantDeclaration("@.formatting.string", "private constant [4 x i8] c\"%d\\0A\\00\""));	

		// NOTA: sempre que X.accept(Y), então Y.visit(X);
		// NOTA: Logo, o comando abaixo irá chamar codeGenerator.visit(Program), linha 75
		p.accept(codeGenerator);

		// Link do printf
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);
		codeGenerator.assembler.add(new LlvmExternalDeclaration("@printf", LlvmPrimitiveType.I32, pts)); 
		List<LlvmType> mallocpts = new LinkedList<LlvmType>();
		mallocpts.add(LlvmPrimitiveType.I32);
		codeGenerator.assembler.add(new LlvmExternalDeclaration("@malloc", new LlvmPointer(LlvmPrimitiveType.I8),mallocpts)); 


		String r = new String();
		for(LlvmInstruction instr : codeGenerator.assembler)
			r += instr+"\n";
		return r;
	}

	public LlvmValue visit(Program n){
		n.mainClass.accept(this);

		for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
			c.head.accept(this);

		return null;
	}

	public LlvmValue visit(MainClass n){
		
		// definicao do main 
		assembler.add(new LlvmDefine("@main", LlvmPrimitiveType.I32, new LinkedList<LlvmValue>()));
		assembler.add(new LlvmLabel(new LlvmLabelValue("entry")));
		LlvmRegister R1 = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I32));
		assembler.add(new LlvmAlloca(R1, LlvmPrimitiveType.I32, new LinkedList<LlvmValue>()));
		assembler.add(new LlvmStore(new LlvmIntegerLiteral(0), R1));

		// Statement é uma classe abstrata
		// Portanto, o accept chamado é da classe que implementa Statement, por exemplo,  a classe "Print". 
		n.stm.accept(this);  

		// Final do Main
		LlvmRegister R2 = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmLoad(R2,R1));
		assembler.add(new LlvmRet(R2));
		assembler.add(new LlvmCloseDefinition());
		return null;
	}
	
	public LlvmValue visit(Plus n){
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmPlus(lhs,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	
	public LlvmValue visit(Print n){

		LlvmValue v =  n.exp.accept(this);

		// getelementptr:
		LlvmRegister lhs = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I8));
		LlvmRegister src = new LlvmNamedValue("@.formatting.string",new LlvmPointer(new LlvmArray(4,LlvmPrimitiveType.I8)));
		List<LlvmValue> offsets = new LinkedList<LlvmValue>();
		offsets.add(new LlvmIntegerLiteral(0));
		offsets.add(new LlvmIntegerLiteral(0));
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		args.add(lhs);
		args.add(v);
		assembler.add(new LlvmGetElementPointer(lhs,src,offsets));

		pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);
		
		// printf:
		assembler.add(new LlvmCall(new LlvmRegister(LlvmPrimitiveType.I32),
				LlvmPrimitiveType.I32,
				pts,				 
				"@printf",
				args
				));
		return null;
	}
	
	public LlvmValue visit(IntegerLiteral n){
		return new LlvmIntegerLiteral(n.value);
	};
		
	//TODO Teste
	public LlvmValue visit(VarDecl n){
		return new LlvmNamedValue("%_" + n.name.s, n.type.accept(this).type);
	}
	
	//TODO Teste
	public LlvmValue visit(Formal n){
		return new LlvmNamedValue("%_" + n.name.s, n.type.accept(this).type);
	}
	
	//TODO Teste
	public LlvmValue visit(IntArrayType n){
		return new LlvmNamedValue("PTR", LlvmPrimitiveType.I32);
	}
	
	//TODO Teste
	public LlvmValue visit(BooleanType n){
		return new LlvmNamedValue("BOOL", LlvmPrimitiveType.I1);
	}
	
	//TODO Teste
	public LlvmValue visit(IntegerType n){
		return new LlvmNamedValue("INT", LlvmPrimitiveType.I32);
	}
	
	//TODO Teste
	public LlvmValue visit(IdentifierType n){
		return new LlvmNamedValue("ID", LlvmPrimitiveType.LABEL);
		//return n.accept(this);
	}
	
	public LlvmValue visit(Block n){
	
		util.List<Statement> i = n.body;
		
		while(i != null){
			i.head.accept(this);
			i = i.tail;
		}
		
		return null;
	}
	
	public LlvmValue visit(If n){
		
		LlvmValue cond = n.condition.accept(this);
		LlvmLabelValue beginThen = new LlvmLabelValue("_then" + labelCount++);
		
		if(n.elseClause != null){
			LlvmLabelValue beginElse = new LlvmLabelValue("_else" + labelCount++);
			LlvmLabelValue endIf = new LlvmLabelValue("_endif" + labelCount++);
			//IF
			assembler.add(new LlvmComment("IF STATEMENT"));
			assembler.add(new LlvmBranch(cond, beginThen, beginElse));
			//THEN
			assembler.add(new LlvmComment("THEN STATEMENT"));
			assembler.add(new LlvmLabel(beginThen));
			n.thenClause.accept(this);
			assembler.add(new LlvmBranch(endIf));
			//ELSE
			assembler.add(new LlvmComment("ELSE STATEMENT"));
			assembler.add(new LlvmLabel(beginElse));
			n.elseClause.accept(this);
			assembler.add(new LlvmBranch(endIf));
			//ENDIF
			assembler.add(new LlvmComment("END POINT"));
			assembler.add(new LlvmLabel(endIf));
		}
		else{
			LlvmLabelValue endIf = new LlvmLabelValue("_endif" + labelCount++);
			//IF
			assembler.add(new LlvmComment("IF STATEMENT"));
			assembler.add(new LlvmBranch(cond, beginThen, endIf));
			//THEN
			assembler.add(new LlvmComment("THEN STATEMENT"));
			assembler.add(new LlvmLabel(beginThen));
			n.thenClause.accept(this);
			assembler.add(new LlvmBranch(endIf));
			//ENDIF
			assembler.add(new LlvmComment("END POINT"));
			assembler.add(new LlvmLabel(endIf));
		}
		
		return null;
	}
	
	public LlvmValue visit(While n){
	
		LlvmLabelValue whileBranch = new LlvmLabelValue("_while" + labelCount++);
		LlvmLabelValue doBranch = new LlvmLabelValue("_do" + labelCount++);
		LlvmLabelValue endBranch = new LlvmLabelValue("_endwhile" + labelCount++);
		
		assembler.add(new LlvmBranch(whileBranch));
		assembler.add(new LlvmComment("WHILE STATEMENT"));
		assembler.add(new LlvmLabel(whileBranch));
		LlvmValue cond = n.condition.accept(this);
		assembler.add(new LlvmBranch(cond, doBranch, endBranch));
		
		assembler.add(new LlvmComment("DO STATEMENT"));
		assembler.add(new LlvmLabel(doBranch));
		n.body.accept(this);
		assembler.add(new LlvmBranch(whileBranch));
		
		assembler.add(new LlvmComment("END POINT"));
		assembler.add(new LlvmLabel(endBranch));
			
		return null;
	}
	
	//TODO Teste
	public LlvmValue visit(Assign n){
		LlvmValue lhs =	new LlvmNamedValue( "%_" + n.var.s, new LlvmPointer(n.var.accept(this).type));
		assembler.add(new LlvmStore(n.exp.accept(this), lhs));
		return null;
	}
	
	public LlvmValue visit(And n){
	
		LlvmLabelValue trueBranch = new LlvmLabelValue("_true" + labelCount++);
		LlvmLabelValue falseBranch = new LlvmLabelValue("_false" + labelCount++);
		LlvmLabelValue closingBranch = new LlvmLabelValue("_closing" + labelCount++);
		LlvmRegister falseCond = new LlvmRegister(LlvmPrimitiveType.I1);
		LlvmRegister result = new LlvmRegister(LlvmPrimitiveType.I1);
		
		LlvmValue v1 = n.lhs.accept(this);
		assembler.add(new LlvmComment("test LHS and do conditional branch"));
		assembler.add(new LlvmBranch(v1, trueBranch, falseBranch));
		
		assembler.add(new LlvmComment("test RHS and jump to the closing"));
		assembler.add(new LlvmLabel(trueBranch));
		LlvmValue v2 = n.rhs.accept(this);
		assembler.add(new LlvmBranch(v2, closingBranch, falseBranch));
		
		assembler.add(new LlvmLabel(falseBranch));
		assembler.add(new LlvmComment("tested false, making it so"));
		assembler.add(new LlvmIcmp(falseCond, LlvmIcmp.LT, LlvmPrimitiveType.I32, new LlvmIntegerLiteral(1), new LlvmIntegerLiteral(0)));
		assembler.add(new LlvmBranch(closingBranch));
		
		assembler.add(new LlvmComment("phi, my lord, phi"));
		assembler.add(new LlvmLabel(closingBranch));
		assembler.add(new LlvmPhi(result, LlvmPrimitiveType.I1, falseCond, falseBranch, v2, trueBranch));			
		
		return result;
	}
	
	public LlvmValue visit(LessThan n){
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1); 
		
		assembler.add(new LlvmIcmp(lhs, LlvmIcmp.LT, LlvmPrimitiveType.I32, v1, v2));
		
		return lhs;
	}
	public LlvmValue visit(Equal n){
	
		LlvmValue lhs = n.lhs.accept(this);
		LlvmValue rhs = n.rhs.accept(this);
		LlvmRegister result = new LlvmRegister(LlvmPrimitiveType.I1);
		
		assembler.add(new LlvmIcmp(result, LlvmIcmp.EQ, LlvmPrimitiveType.I32, lhs, rhs));
		
		return result;
	}
	
	public LlvmValue visit(Minus n){
		
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		
		assembler.add(new LlvmMinus(lhs,LlvmPrimitiveType.I32,v1,v2));
		
		return lhs;
	}
	
	public LlvmValue visit(Times n){
	
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		
		assembler.add(new LlvmTimes(lhs,LlvmPrimitiveType.I32,v1,v2));
		
		return lhs;
	}
	
	public LlvmValue visit(True n){
		return new LlvmBool(LlvmBool.TRUE);
	}
	
	public LlvmValue visit(False n){
		return new LlvmBool(LlvmBool.FALSE);
	}
	
	public LlvmValue visit(Not n){
		
		LlvmValue v = n.exp.accept(this);
		
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmXor(lhs, LlvmPrimitiveType.I1, v, new LlvmBool(LlvmBool.TRUE)));
		
		return lhs;
	}

	
/**********************************************************************************/
/* === Restando ==== 
* 
* 
*/
/**********************************************************************************/

	//TODO
	public LlvmValue visit(ClassDeclSimple n){return null;}
	//TODO
	public LlvmValue visit(ClassDeclExtends n){return null;}
	//TODO
	public LlvmValue visit(MethodDecl n){
	
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		
		for(util.List<syntaxtree.Formal> i = n.formals; i != null; i = i.tail ){
			args.add(i.head.accept(this));
		}
		assembler.add(new LlvmDefine(n.name.s, n.returnType.accept(this).type, args));
		
		return null;
	}
	//TODO
	public LlvmValue visit(ArrayAssign n){
	
		/*LlvmType type = n.var.accept(this).type;
		LlvmRegister lhs;
		LlvmRegister addr = new LlvmRegister(new LlvmPointer(type));
		//TODO insert array dimension
		LlvmRegister src = new LlvmNamedValue("%_" + n.var.s, new LlvmPointer(new LlvmArray(, type)));
		List<LlvmValue> offsets = new LinkedList<LlvmValue>();
		offsets.add(new LlvmIntegerLiteral(0));
		//TODO insert array offset
		offsets.add(new LlvmIntegerLiteral());
		assembler.add(new LlvmGetElementPointer(lhs,src,offsets));
		assembler.add(LlvmStore);
		n.var.accept(this)*/
		
		
		return null;
	}
	//TODO
	public LlvmValue visit(ArrayLookup n){return null;}
	//TODO
	public LlvmValue visit(ArrayLength n){return null;}
	//TODO
	public LlvmValue visit(Call n){return null;}
	//TODO
	public LlvmValue visit(IdentifierExp n){return null;}
	//TODO
	public LlvmValue visit(This n){return null;}
	//TODO
	public LlvmValue visit(NewArray n){return null;}
	//TODO
	public LlvmValue visit(NewObject n){return null;}
	//TODO
	public LlvmValue visit(Identifier n){return null;}

}


/**********************************************************************************/
/* === Tabela de Símbolos ==== 
 * 
 * 
 */
/**********************************************************************************/

class SymTab extends VisitorAdapter{
    public Map<String, ClassNode> classes;
    private ClassNode classEnv;    //aponta para a classe em uso
    private MethodNode methodEnv;
    private Map<String, LlvmType> variables;
    
    public LlvmValue FillTabSymbol(Program n){
    	n.accept(this);
    	return null;
    }

    public LlvmValue visit(Program n){
    	n.mainClass.accept(this);

    	for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
    		c.head.accept(this);

    	return null;
    }

    public LlvmValue visit(MainClass n){
    	classes.put(n.className.s, new ClassNode(n.className.s, null, null, null));
    	return null;
    }

    public LlvmValue visit(ClassDeclSimple n){
    	
    	List<LlvmType> typeList = null;
    	List<LlvmValue> varList = null;
    	List<MethodDecl> methodList = null;
    	
    	
    	for(util.List<VarDecl> l = n.varList; l != null; l = l.tail){
    		// Constroi TypeList com os tipos das variáveis da Classe (vai formar a Struct da classe)
    		typeList.add(l.head.type.accept(this).type);
    		// Constroi VarList com as Variáveis da Classe
    		varList.add(l.head.name.accept(this));
    	}
    	
    	// Percorre n.methodList visitando cada método
    	// TODO precorrer methodList;
    	for(util.List<MethodDecl> l = n.methodList; l != null; l = l.tail){
    		//methodList.add(l.head);
    		methodList.add(l.head);
    	}
    	
    	classEnv = new ClassNode(n.name.s, new LlvmStructure(typeList), varList, methodList);
    	
    	classes.put(n.name.s, classEnv);
      		    	
    	return null;
    }

	public LlvmValue visit(ClassDeclExtends n){return null;}
	
	public LlvmValue visit(VarDecl n){
		n.name.s; //String
		n.type.accept(this).type //LlvmValue
		variables.put(n.name.s, n.type.accept(this));
		//n.name.accept(this);
		//n.type.accept(this);
		
		return null;
	}
	
	public LlvmValue visit(Formal n){
		
		n.name.accept(this);
		n.type.accept(this);
		
		return null;
	}
	
	public LlvmValue visit(MethodDecl n){
		
		for(util.List<Formal> l = n.formals; l != null; l = l.tail)
    		l.head.accept(this);
		
		for(util.List<VarDecl> l = n.locals; l != null; l = l.tail)
    		l.head.accept(this);
		
		return null;
	}
	
	public LlvmValue visit(IdentifierType n){return null;}
	public LlvmValue visit(IntArrayType n){
		
		LlvmArray a;
		
		return a;
	}
	
	public LlvmValue visit(BooleanType n){
		
		LlvmBool b;
		
		return b;
	}
	public LlvmValue visit(IntegerType n){return null;}
}

class ClassNode extends LlvmType {
		private String className;
		//String upperClass;
		private LlvmStructure classType;
		private List<LlvmValue> attrList;
		private List<MethodDecl> methodList;
		private Map<Integer, String> methodIndexes;
		
		ClassNode (String nameClass, LlvmStructure classType, List<LlvmValue> varList, List<MethodDecl> methodList){
			this.className = nameClass;
			this.classType = classType;
			this.attrList = varList;
			this.methodList = methodList;
			
		}
		
		public void addVar(String name, LlvmType type){
			
		}
		
}

class MethodNode {
	String methodName;
	LlvmStructure methodType;
	List<LlvmValue> paramList;
	public Map<String, LlvmType> variables;

	MethodNode(String methodName, LlvmStructure methodType, List<LlvmValue> paramList){
		this.methodName = methodName;
		this.methodType = methodType;
		this.paramList = paramList;
	}	
	
}

/*class VarNode{
	String varName;
	LlvmType type;
	Integer offset;
		
	VarNode(String varName, LlvmType type){
		this.varName = varName;
		this.type = type;
	}
}*/


