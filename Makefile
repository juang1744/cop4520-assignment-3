all: problem2

problem1:
	javac Problem1.java && java Problem1

problem2:
	javac Problem2.java && java Problem2

clean:
	rm -fv *.class

.PHONY: all problem1 problem2 clean
.SILENT: