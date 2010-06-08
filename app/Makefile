TOKENDEF=tokens.def
GRAMMARDEF=grammar.def
ifdef PREFIX
	FLAG = -p
endif

all: create compile

run: all
	java App

create:
	java -jar ssCC.jar $(FLAG) $(PREFIX) $(TOKENDEF) $(GRAMMARDEF)

compile:
	javac *.java

clean:
	rm *.class
