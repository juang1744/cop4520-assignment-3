import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class Problem1
{
    final static int NUM_THREADS = 4;

    public static void main(String[] args)
    {
        LockFreeList list = new LockFreeList();
        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new Runnable()
            {
                public void run()
                {
                    list.add((int) (Math.random() * 100));
                    list.print();
                    list.add((int) (Math.random() * 100));
                    list.print();
                    list.removeFirst();
                    list.print();
                    list.contains((int) (Math.random() * 100));
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

    public void print()
    {
        String output = "";
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
                
                if (curr.value >= Integer.MAX_VALUE-1)
                {
                    break retry;
                }

                output += curr.value + " ";
                pred = curr;
                curr = succ;
            }
        }

        System.out.println(output);
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