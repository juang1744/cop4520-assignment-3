import java.util.*;
import java.util.concurrent.atomic.*;

import javax.lang.model.util.ElementScanner6;

public class Problem1
{
    final static int NUM_THREADS = 4;
    final static int NUM_PRESENTS = 500_000;
    final static double CONTAINS_CHANCE = 0.10;

    public static void main(String[] args)
    {
        // Create and shuffle bag of presents
        ArrayList<Integer> bag = new ArrayList<Integer>(NUM_PRESENTS);
        AtomicInteger currentPresent = new AtomicInteger(0); 

        for (int i = 0; i < NUM_PRESENTS; i++)
        {
            bag.add(i);
        }

        Collections.shuffle(bag);

        // Create chain and servant (thread) objects
        LockFreeList chain = new LockFreeList();
        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new Runnable()
            {
                public void run()
                {
                    int currentTask = 0;
                    while (currentPresent.get() < NUM_PRESENTS)
                    {
                        boolean checkContains = Math.random() < CONTAINS_CHANCE;

                        // Each thread randomly checks if a particular gift is in the chain
                        if (checkContains)
                        {
                            chain.contains((int) Math.random() * NUM_PRESENTS);
                        }
                        // Or alternates between adding a gift to the chain from the bag
                        else if (currentTask % 2 == 0)
                        {
                            int nextPresent = currentPresent.getAndIncrement();

                            if (nextPresent >= NUM_PRESENTS)
                            {
                                break;
                            }

                            chain.add(bag.get(nextPresent));
                            currentTask++;
                        }
                        // Or writing a thank you note from a gift in the chain
                        else
                        {
                            chain.removeFirst();
                            currentTask++;
                        }
                    }
                }
            });
        }

        for (Thread thread : threads)
        {
            thread.start();
        }

        for (Thread thread : threads)
        {
            try
            {
                thread.join();
            }
            catch (InterruptedException exception) {}
        }

        System.out.println("All the thank you notes have been written!");
    }
}

class LockFreeList
{
    public Node head, tail;

    public LockFreeList()
    {
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);
        head.next = new AtomicMarkableReference<Node>(tail, false);
        tail.next = new AtomicMarkableReference<Node>(null, false);
    }

    public boolean add(int value)
    {
        while (true)
        {
            Window window = find(value);
            Node pred = window.pred;
            Node curr = window.curr;

            if (curr.value == value)
            {
                return false;
            }
            else
            {
                Node node = new Node(value);
                node.next = new AtomicMarkableReference<Node>(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false))
                {
                    return true;
                }
            }
        }
    }

    public boolean remove(int value)
    {
        boolean snip;

        while (true)
        {
            Window window = find(value);
            Node pred = window.pred;
            Node curr = window.curr;

            if (curr.value != value)
            {
                return false;
            }
            else
            {
                Node succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);

                if (!snip)
                {
                    continue;
                }

                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }

    public boolean removeFirst()
    {
        return remove(head.next.getReference().value);
    }

    public boolean contains(int value)
    {
        boolean[] marked = {false};
        Node curr = head;

        while (curr.value < value)
        {
            curr = curr.next.getReference();
            Node succ = curr.next.get(marked);
        }

        return (curr.value == value && !marked[0]);
    }

    private class Node
    {
        volatile int value;
        AtomicMarkableReference<Node> next;

        Node(int value)
        {
            this.value = value;
        }
    }

    private class Window
    {
        Node pred, curr;

        Window(Node pred, Node curr)
        {
            this.pred = pred;
            this.curr = curr;
        }
    }   

    private Window find(int value)
    {
        Node pred = null, curr = null, succ = null;
        boolean[] marked = {false};
        boolean snip;

        retry: while (true)
        {
            pred = head;
            curr = pred.next.getReference();

            while (true)
            {
                succ = curr.next.get(marked);
                
                while (marked[0])
                {
                    snip = pred.next.compareAndSet(curr, succ, false, false);

                    if (!snip)
                    {
                        continue retry;
                    }

                    curr = succ;
                    succ = curr.next.get(marked);
                }
                
                if (curr.value >= value)
                {
                    return new Window(pred, curr);
                }

                pred = curr;
                curr = succ;
            }
        }
    }
}