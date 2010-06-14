TOKENDEF=tokens.def
GRAMMARDEF=grammar.def
ifdef PREFIX
	FLAG = -p
endif

all: clean create compile

run: all
	java App

create:
	java -jar ssCC.jar $(FLAG) $(PREFIX) $(TOKENDEF) $(GRAMMARDEF)

compile:
	javac *.java

clean:
	rm -f *.class

clean-all: clean
	rm -f AST* Parser.javaf Tokenizer.java Token.java Visitor.java
