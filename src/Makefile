
sources=$(wildcard *.java)

classes=$(sources:.java=.class)

SiCC.jar:  $(classes)
	jar cvf SiCC.jar *.class

%.class : %.java
	javac $< 

clean :
	rm -f SiCC.jar *.class

