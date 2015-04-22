#
# pacote v04
# 
# Como compilar:
# $ make
#
# Como executar o compilador:
# $ make run INPUT=test/teste.java OUTPUT=teste.s
#
# Como executar:
# $ lli teste.s
#

SOURCES = $(wildcard src/**/*.java)
CLASSES = $(SOURCES:.java=.class)

all: $(CLASSES)

%.class: %.java
	javac -classpath src:lib/projeto2.jar $<

run:
	java -classpath src:lib/projeto2.jar main/Main $(INPUT) $(OUTPUT)

rt:
	java -classpath src:lib/projeto2.jar main/Main test/test.java test.ll

teste:
	clang learn.c -S -emit-llvm -o learn.ll
	lli learn.ll

clean:
	rm -f src/llvm/*.class src/llvmast/*.class learn.ll


