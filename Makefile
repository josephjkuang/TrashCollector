default: build

clean:
	rm -f *.class

build: clean
	javac *.java 

test: build
	java TrashCollector