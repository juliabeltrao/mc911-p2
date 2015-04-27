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
	private LlvmLabelValue lastLabel = new LlvmLabelValue("0");	// apontador para a ultima label que foi saltada(label do bloco)


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
	
	// Todos os visit's que devem ser implementados	
	public LlvmValue visit(ClassDeclSimple n){return null;}
	public LlvmValue visit(ClassDeclExtends n){return null;}
	public LlvmValue visit(VarDecl n){return null;}
	public LlvmValue visit(MethodDecl n){return null;}
	public LlvmValue visit(Formal n){return null;}
	public LlvmValue visit(IntArrayType n){return null;}
	public LlvmValue visit(BooleanType n){return null;}
	public LlvmValue visit(IntegerType n){return null;}
	public LlvmValue visit(IdentifierType n){return null;}
	
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
	public LlvmValue visit(While n){return null;}
	public LlvmValue visit(Assign n){return null;}
	public LlvmValue visit(ArrayAssign n){return null;}
	
	public LlvmValue visit(And n){
	
		/*LlvmLabelValue l1 = new LlvmLabelValue();
		LlvmLabelValue l2 = new LlvmLabelValue();
		
		// TODO: create label
		assembler.add(new LlvmLabel(l1));
		LlvmValue v1 = n.lhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmIcmp(lhs, LlvmIcmp.NE, LlvmPrimitiveType.I32, v1, null));
		//TODO branch condcional end
		assembler.add(new LlvmBranch());
		
		// TODO : create label
		assembler.add(new LlvmLabel(l2));
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister rhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmIcmp(rhs, LlvmIcmp.NE, LlvmPrimitiveType.I32, v2, null));
		assembler.add(new LlvmBranch(l2));
		
		// TODO branch end
		// TODO create label end
		// TODO create phi
		LlvmRegister z = new LlvmRegister(LlvmPrimitiveType.I32);*/
		//assembler.add(new LlvmZext(z, LlvmPrimitiveType.I1, /*a completar*/, LlvmPrimitiveType.I32));				
		
		//return z;
		return null;
	}
	
	public LlvmValue visit(LessThan n){
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1); 
		
		assembler.add(new LlvmIcmp(lhs, LlvmIcmp.LT, LlvmPrimitiveType.I32, v1, v2));
		
		return lhs;
	}
	public LlvmValue visit(Equal n){return null;}
	
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
	
	public LlvmValue visit(ArrayLookup n){return null;}
	public LlvmValue visit(ArrayLength n){return null;}
	public LlvmValue visit(Call n){return null;}
	
	public LlvmValue visit(True n){
		return new LlvmBool(LlvmBool.TRUE);
	}
	
	public LlvmValue visit(False n){
		return new LlvmBool(LlvmBool.FALSE);
	}
	
	public LlvmValue visit(IdentifierExp n){return null;}
	public LlvmValue visit(This n){return null;}
	public LlvmValue visit(NewArray n){return null;}
	public LlvmValue visit(NewObject n){return null;}
	
	public LlvmValue visit(Not n){
		
		LlvmValue v = n.exp.accept(this);
		
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmXor(lhs, LlvmPrimitiveType.I1, v, new LlvmBool(LlvmBool.TRUE)));
		
		return lhs;
	}
	
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
    	List<MethodNode> methodList = null;
    	
    	for(util.List<VarDecl> l = n.varList; l != null; l = l.tail){
    		// Constroi TypeList com os tipos das variáveis da Classe (vai formar a Struct da classe)
    		typeList.add(l.head.type.accept(this).type);
    		// Constroi VarList com as Variáveis da Classe
    		varList.add(l.head.name.accept(this));
    	}
    	
    	classEnv = new ClassNode(n.name.s, new LlvmStructure(typeList), varList, methodList);
    	
    	// Percorre n.methodList visitando cada método
    	// TODO precorrer methodList;
    	for(util.List<MethodDecl> l = n.methodList; l != null; l = l.tail){
    		l.head.accept(this);
    		//methodList.add(l.head);
    	}
    	
    	classes.put(n.name.s, classEnv);
      		    	
    	return null;
    }

	public LlvmValue visit(ClassDeclExtends n){return null;}
	
	public LlvmValue visit(VarDecl n){
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
		
		
		
		return null;}
	
	public LlvmValue visit(BooleanType n){return null;}
	public LlvmValue visit(IntegerType n){return null;}
}

class ClassNode extends LlvmType {
		String className;
		//String upperClass;
		LlvmStructure classType;
		List<LlvmValue> attrList;
		List<MethodNode> methodList;
		Map<Integer, String> methodIndexes;
		
		ClassNode (String nameClass, LlvmStructure classType, List<LlvmValue> varList, List<MethodNode> methodList){
			this.className = nameClass;
			this.classType = classType;
			this.attrList = varList;
			this.methodList = methodList;
			
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




