# COP4520 Programming Assignment 3

### How to run

Problem 1: The Birthday Presents Party
```
make problem1
```
or
```
javac Problem1.java
java Problem1
```
Expected output:

`All the thank you notes have been written!`

---

## Summary of approach

For Problem 1, the chain of gifts is represented with a non-blocking concurrent linked list, following the implementation of 'LinkedList' from the class textbook.

Each thread alternates between adding presents to the chain and writing thank you notes, with a 10% chance of checking whether a randomly chosen gift tag is currently stored on the chain, per the Minotaur's request.

---
 
