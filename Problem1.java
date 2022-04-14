import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Problem1
{
    final static int NUM_THREADS = 4;

    public static void main(String[] args)
    {
        CoarseList list = new CoarseList();
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
            catch (InterruptedException exception)
            {

            }
        }
    }
}

class CoarseList
{
    public Node head, tail;
    private Lock lock = new ReentrantLock();

    public CoarseList()
    {
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);
        head.next = tail;
    }

    public boolean add(int value)
    {
        lock.lock();

        try
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
                node.next = curr;
                pred.next = node;
                return true;    
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean remove(int value)
    {
        lock.lock();

        try
        {
            Window window = find(value);
            Node pred = window.pred;
            Node curr = window.curr;

            if (curr.value == value)
            {
                pred.next = curr.next;
                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean removeFirst()
    {
        return remove(head.next.value);
    }

    public boolean contains(int value)
    {
        lock.lock();

        try
        {
            Window window = find(value);
            Node pred = window.pred;
            Node curr = window.curr;

            if (curr.value == value)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void print()
    {
        String output = "";
        Node pred, curr;
        int value = Integer.MAX_VALUE-1;

        lock.lock();

        try
        {
            pred = head;
            curr = head.next;

            while (curr.value < value)
            {
                output += curr.value + " ";
                pred = curr;
                curr = curr.next;
            }

            System.out.println(output);
        }
        finally
        {
            lock.unlock();
        }
    }

    private class Node
    {
        volatile int value;
        volatile Node next;

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
        Node pred = head;
        Node curr = pred.next;

        while (curr.value < value)
        {
            pred = curr;
            curr = curr.next;
        }

        return new Window(pred, curr);
    }
}