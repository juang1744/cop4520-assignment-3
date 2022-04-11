all: problem1

problem1:
	javac Problem1.java && java Problem1

clean:
	rm -fv *.class

.PHONY: all problem1 clean
.SILENT: