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

  	private SymTab symTab = new SymTab();
	private ClassNode classEnv; 	// Aponta para a classe atualmente em uso em symTab
	private MethodNode methodEnv; 	// Aponta para a metodo atualmente em uso em symTab
	private int labelCount = 0;		// contador para gerar labels unicas
	private LlvmRegister lastElement;
	
	public Codegen(){
		assembler = new LinkedList<LlvmInstruction>();
	}

	// Método de entrada do Codegen
	public String translate(Program p, Env env){	
		codeGenerator = new Codegen();
		
		// Preenchendo a Tabela de Símbolos
		// Quem quiser usar 'env', apenas comente essa linha
		codeGenerator.symTab.FillTabSymbol(p);
		
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
		
		LlvmValue lhs = new LlvmNamedValue("%_" + n.name.s, n.type.accept(this).type);
		assembler.add(new LlvmAlloca(lhs, lhs.type));
		return lhs;
	}
	
	//TODO Teste
	public LlvmValue visit(Formal n){
		return new LlvmNamedValue("%_" + n.name.s, n.type.accept(this).type);
	}
	
	//TODO Teste
	public LlvmValue visit(IntArrayType n){
		return new LlvmNamedValue("PTR", new LlvmPointer(LlvmPrimitiveType.I32));
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
	
	//TODO
	public LlvmValue visit(Assign n){
		
		LlvmValue v = n.var.accept(this);
		LlvmValue exp = n.exp.accept(this);
		
		exp.type = new LlvmPointer(exp.type);
		
		assembler.add(new LlvmStore(exp, lastElement));
		
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

	public LlvmValue visit(ClassDeclSimple n){
		
		classEnv = symTab.getClass(n.name.s);
		
		List<LlvmType> types = new LinkedList<LlvmType>();
		
		for(util.List<VarDecl> i = n.varList; i != null; i = i.tail){
			types.add(i.head.type.accept(this).type);
		}
		
		for(util.List<MethodDecl> i = n.methodList; i != null; i = i.tail){
			i.head.accept(this);
		}
		
		LlvmClassDeclarator decl = new LlvmClassDeclarator(types, n.name.s, n.methodList.size());
		assembler.add(0, decl);
		
		
		return null;
	}
	
	//TODO
	public LlvmValue visit(MethodDecl n){
		
		methodEnv = classEnv.getMethod(n.name.s);
	
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		
		//args.add(new LlvmNamedValue("%class." + classEnv.getName() + " * %this", LlvmPrimitiveType.CUSTOM));
		args.add(new LlvmNamedValue("%this", new LlvmPointer(new LlvmTypeClass(classEnv.getName()))));
		for(util.List<syntaxtree.Formal> i = n.formals; i != null; i = i.tail ){
			args.add(i.head.accept(this));
		}
		
		assembler.add(new LlvmDefine("@__" + classEnv.getName() + "__" + n.name.s, n.returnType.accept(this).type, args));
		
		for(util.List<VarDecl> i = n.locals; i != null; i = i.tail){
			LlvmValue v = i.head.accept(this);
		}
		
		for(util.List<Statement> i = n.body; i != null; i = i.tail){
			LlvmValue v = i.head.accept(this);
		}
		
		assembler.add(new LlvmRet(n.returnExp.accept(this)));
		assembler.add(new LlvmCloseDefinition());
		
		return null;
	}
	
	//TODO
	public LlvmValue visit(IdentifierExp n){
		return n.name.accept(this);
	}
	
	//TODO
	public LlvmValue visit(Identifier n){
		
		LlvmType t;
		LlvmRegister lhs;
		
		t = methodEnv.getVarType(n.s);
		if(t != null){
			LlvmValue val = new LlvmNamedValue("%_" + n.s, new LlvmPointer(t));
			lhs = new LlvmRegister(t);
			
			assembler.add(new LlvmLoad(lhs, val));
		}
		else{
			LlvmRegister reg = new LlvmRegister(new LlvmPointer(classEnv.getAttrType(n.s)));
			t = classEnv.getAttrType(n.s);
			lhs = new LlvmRegister(t);
			int off = classEnv.getAttrOffset(n.s);
			List<LlvmValue> offset = new LinkedList<LlvmValue>();
			offset.add(new LlvmNamedValue("0", LlvmPrimitiveType.I8));
			offset.add(new LlvmNamedValue(String.valueOf(off), LlvmPrimitiveType.I32));
			assembler.add(new LlvmGetElementPointer(reg, new LlvmNamedValue("%this", new LlvmPointer(new LlvmTypeClass(classEnv.getName()))), offset));
			assembler.add(new LlvmLoad(lhs, reg));
			lastElement = reg;
		}

		return lhs;
	}
	
	//TODO
	public LlvmValue visit(NewArray n){
	
		LlvmValue size = n.size.accept(this);
		LlvmRegister newSize = new LlvmRegister(LlvmPrimitiveType.I32);
		List<LlvmValue> offset = new LinkedList<LlvmValue>();
		
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmPlus(newSize, newSize.type, size, new LlvmNamedValue("1", LlvmPrimitiveType.I32)));
		assembler.add(new LlvmMalloc(lhs, LlvmPrimitiveType.I32, newSize));
		
		LlvmRegister reg = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I32));
		offset.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
		assembler.add(new LlvmGetElementPointer(reg, lhs, offset));
		assembler.add(new LlvmStore(newSize, reg));
		
		return lhs;
	}
	
	//TODO
	public LlvmValue visit(This n){
		return new LlvmRegister("%this", new LlvmPointer(new LlvmTypeClass(classEnv.getName())));
	}
	
	//TODO
	public LlvmValue visit(ArrayAssign n){
	
		List<LlvmValue> offset = new LinkedList<LlvmValue>();
		LlvmRegister lhs = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I32));
		LlvmValue var = n.var.accept(this);
		LlvmValue value = n.value.accept(this);
		LlvmValue i = n.index.accept(this);
		LlvmRegister newI = new LlvmRegister(i.type);
		
		assembler.add(new LlvmPlus(newI, newI.type, i, new LlvmNamedValue("1", LlvmPrimitiveType.I32)));
		offset.add(new LlvmNamedValue(newI.toString(), LlvmPrimitiveType.I32));
		assembler.add(new LlvmGetElementPointer(lhs, var, offset));
		assembler.add(new LlvmStore(value, lhs));
		
		return null;
	}
	
	//TODO
	public LlvmValue visit(NewObject n){
	
		LlvmRegister obj = new LlvmRegister(new LlvmPointer(new LlvmTypeClass(n.className.s)));
		assembler.add(new LlvmMalloc(obj, obj.type, new LlvmTypeClass(n.className.s).toString()));
		
		return obj;
	}
	
	//TODO
	public LlvmValue visit(ArrayLookup n){
	
		LlvmValue v = n.array.accept(this);
		LlvmValue i = n.index.accept(this);
		List<LlvmValue> off = new LinkedList<LlvmValue>();
		
		LlvmRegister newI = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmPlus(newI, newI.type, i, new LlvmNamedValue("1", LlvmPrimitiveType.I32)));
		
		off.add(newI);
		LlvmRegister addr = new LlvmRegister(v.type);
		assembler.add(new LlvmGetElementPointer(addr, v, off));
		LlvmRegister ret = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmLoad(ret, addr));
		
		return ret;
	}
	
	//TODO
	public LlvmValue visit(Call n){
		
		LlvmValue obj = n.object.accept(this);
		String name = obj.type.toString().replace("%class.", "");
		name = name.replace(" *", "");
		LlvmType retType = symTab.getClass(name).getMethod(n.method.s).methodType;
		
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		LlvmRegister ret = new LlvmRegister(retType);
		
		args.add(obj);
		for(util.List<Exp> i = n.actuals; i != null; i = i.tail){
			args.add(i.head.accept(this));
		}
		
	
		assembler.add(new LlvmCall(ret, retType, "@__"+name+"__"+n.method.s, args));
		
		return ret;
	}
	
	//TODO
	public LlvmValue visit(ArrayLength n){
	
		LlvmValue v = n.array.accept(this);
		LlvmNamedValue i = new LlvmNamedValue("0", LlvmPrimitiveType.I32);
		List<LlvmValue> off = new LinkedList<LlvmValue>();
		LlvmRegister addr = new LlvmRegister(v.type);
		LlvmRegister length = new LlvmRegister(LlvmPrimitiveType.I32);
		LlvmRegister ret = new LlvmRegister(LlvmPrimitiveType.I32);
		
		off.add(i);
		assembler.add(new LlvmGetElementPointer(addr, v, off));
		assembler.add(new LlvmLoad(length, addr));
		assembler.add(new LlvmMinus(ret, ret.type, length, new LlvmNamedValue("1", LlvmPrimitiveType.I32)));
		
		return ret;
	}
	
/**********************************************************************************/
/* === Restando ==== 
* 
* 
*/
/**********************************************************************************/

	//TODO
	public LlvmValue visit(ClassDeclExtends n){return null;}
	

}


/**********************************************************************************/
/* === Tabela de Símbolos ==== 
 * 
 * 
 */
/**********************************************************************************/

class SymTab extends VisitorAdapter{
    public Map<String, ClassNode> classes = new HashMap<String, ClassNode>();
    private ClassNode classEnv;    //aponta para a classe em uso
    private MethodNode methodEnv;  //aponta para o método em uso
    //private Map<String, LlvmType> variables;
    
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
    	classes.put(n.className.s, new ClassNode(n.className.s, null, null));
    	return null;
    }

    public LlvmValue visit(ClassDeclSimple n){
    	
    	List<LlvmType> typeList = new LinkedList<LlvmType>();
    	List<LlvmValue> varList = new LinkedList<LlvmValue>();
    	LlvmType t;
    	    	
    	for(util.List<VarDecl> l = n.varList; l != null; l = l.tail){
    		t = l.head.type.accept(this).type;
    		// Constroi TypeList com os tipos das variáveis da Classe (vai formar a Struct da classe)
    		typeList.add(t);
    		// Constroi VarList com as Variáveis da Classe
    		varList.add(new LlvmNamedValue(l.head.name.s, t));
    	}
    	
    	classEnv = new ClassNode(n.name.s, new LlvmStructure(typeList), varList);
    	
    	// Percorre n.methodList visitando cada método
    	for(util.List<MethodDecl> l = n.methodList; l != null; l = l.tail){
    		l.head.accept(this);
    		classEnv.addMethod(methodEnv);
    	}
    	
    	classes.put(n.name.s, classEnv);
      		    	
    	return null;
    }

	public LlvmValue visit(ClassDeclExtends n){
		
		List<LlvmType> typeList = new LinkedList<LlvmType>();
    	List<LlvmValue> varList = new LinkedList<LlvmValue>();
    	LlvmType t;
    	
    	for(util.List<VarDecl> l = n.varList; l != null; l = l.tail){
    		t = l.head.type.accept(this).type;
    		// Constroi TypeList com os tipos das variáveis da Classe (vai formar a Struct da classe)
    		typeList.add(t);
    		// Constroi VarList com as Variáveis da Classe
    		varList.add(new LlvmNamedValue(l.head.name.s, t));
    	}
    	
    	classEnv = new ClassNode(n.name.s, new LlvmStructure(typeList), varList);
    	
    	classEnv.extend(n.superClass.s);
    	
    	// Percorre n.methodList visitando cada método
    	for(util.List<MethodDecl> l = n.methodList; l != null; l = l.tail){
    		l.head.accept(this);
    		classEnv.addMethod(methodEnv);
    	}
    	
    	classes.put(n.name.s, classEnv);
		
		return null;
}
	
	public LlvmValue visit(VarDecl n){
		return new LlvmNamedValue(n.name.s, n.type.accept(this).type);
	}
	
	public LlvmValue visit(Formal n){
		return new LlvmNamedValue(n.name.s, n.type.accept(this).type);
	}
	
	public LlvmValue visit(MethodDecl n){
		
		//List<LlvmType> formalTypes = new LinkedList<LlvmType>();
		List<LlvmValue> formalNames = new LinkedList<LlvmValue>();
		LlvmValue form;
		
		for(util.List<Formal> l = n.formals; l != null; l = l.tail){
    		form = l.head.accept(this);
			//formalTypes.add(form.type);
    		formalNames.add(form);
    	}
		
		methodEnv = new MethodNode(n.name.s, n.returnType.accept(this).type, formalNames);
		
		for(util.List<VarDecl> l = n.locals; l != null; l = l.tail)
    		//methodEnv.addVar(l.head.name.s, l.head.accept(this).type);
			methodEnv.addVar(l.head.accept(this));
			
		return null;
	}
	
	public LlvmValue visit(IdentifierType n){
		return new LlvmNamedValue("ID", LlvmPrimitiveType.LABEL);
	}
	
	public LlvmValue visit(IntArrayType n){
		return new LlvmNamedValue("PTR", new LlvmPointer(LlvmPrimitiveType.I32));
	}
	
	public LlvmValue visit(BooleanType n){
		return new LlvmNamedValue("BOOL", LlvmPrimitiveType.I1);
	}
	
	public LlvmValue visit(IntegerType n){
		return new LlvmNamedValue("INT", LlvmPrimitiveType.I32);
	}
	
	public ClassNode getClass(String className){
		return classes.get(className);
	}
	
}

class ClassNode extends LlvmType {
		private String className;
		private LlvmStructure classType;
		private String upperClass = "";
		private List<LlvmValue> attrList; 
		private List<MethodNode> methodList = new LinkedList<MethodNode>();
		private Map<String, Integer> methodIndexes = new HashMap<String, Integer>();
		private Integer index = 0;
		
		ClassNode (String nameClass, LlvmStructure classType, List<LlvmValue> varList){
			this.className = nameClass;
			this.classType = classType;
			this.attrList = varList;
		}
		
		public void addMethod(MethodNode m){
			this.methodList.add(m);
			this.methodIndexes.put(m.getName(), this.index++);
		}
		
		public void extend(String className){
			this.upperClass = className;
		}
		
		public boolean hasUpper(){
			return !(this.upperClass.isEmpty());
		}
		
		public String getName(){
			return this.className;
		}
		
		public int getSize(){
			return this.classType.sizeByte;
		}
		
		public MethodNode getMethod(String methodName){
			int i = this.methodIndexes.get(methodName);
			return this.methodList.get(i);
			
		}
		
		public int getMethodIndex(String methodName){
			return this.methodIndexes.get(methodName);
		}
		
		public int getAttrOffset(String name){
			// TODO considerar caso de herança
			for(int i = 0; i < this.attrList.size(); i++){
				if(this.attrList.get(i).toString().equals(name))
					return i+1;
			}
			
			return -1;
		}
		
		public LlvmType getAttrType(String name){
			// TODO considerar caso de herança
			if(this.hasUpper()){
				//ClassNode upper = getClass();
				//upper.getAttrType(name);
			}
				
			for(int i = 0; i < this.attrList.size(); i++){
				if(this.attrList.get(i).toString().equals(name))
					return this.attrList.get(i).type;
			}
			
			return null;
		}
}

class MethodNode {
	String methodName;
	//LlvmStructure methodType;
	LlvmType methodType;
	List<LlvmValue> paramList = new LinkedList<LlvmValue>();
	List<LlvmValue> variables = new LinkedList<LlvmValue>();
	//Map<String, LlvmType> variables;
	
	MethodNode(String methodName, LlvmType methodType, List<LlvmValue> paramList){
		this.methodName = methodName;
		this.methodType = methodType;
		this.paramList = paramList;
	}	
	
	public void addVar(LlvmValue var){
		this.variables.add(var);
	}
	
	public String getName(){
		return this.methodName;
	}
	
/*	public int getVarOffset(String name){
		// TODO considerar caso de herança
		for(int i = 0; i < this.attrList.size(); i++){
			if(this.attrList.get(i).toString().equals(name))
				return i;
		}
		
		return -1;
	}
*/	
	public LlvmType getVarType(String name){
		// TODO considerar caso de herança
		for(int i = 0; i < this.paramList.size(); i++){
			if(this.paramList.get(i).toString().equals(name))
				return this.paramList.get(i).type;
		}
		
		for(int i = 0; i < this.variables.size(); i++){
			if(this.variables.get(i).toString().equals(name))
				return this.variables.get(i).type;
		}
		
		return null;
	}
	
/*	public void addVar(String name, LlvmType type){
		variables.put(name, type);
	}
*/	
}
