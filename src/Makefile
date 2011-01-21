
sources=$(wildcard *.java)

classes=$(sources:.java=.class)

ssCC.jar:  $(classes)
	jar cvf ssCC.jar *.class

%.class : %.java
	javac $< 

clean :
	rm -f ssCC.jar *.class

