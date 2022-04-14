import java.util.*;
import java.util.concurrent.atomic.*;

import javax.lang.model.util.ElementScanner6;

public class Problem1
{
    final static int NUM_THREADS = 8;
    final static int NUM_HOURS = 1;
    final static int MINUTES_PER_HOUR = 60;
    final static double MIN_TEMP = -100.0;
    final static double MAX_TEMP = 70.0;

    public static void main(String[] args)
    {
        AtomicDoubleArray highestTemps = new AtomicDoubleArray(5);
        AtomicDoubleArray lowestTemps = new AtomicDoubleArray(5);
        AtomicDoubleArray largestDifferenceInterval = new AtomicDoubleArray(3);

        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new Runnable()
            {
                public void run()
                {
                    double[] highestTemp = new double[5];
                    double[] lowestTemp = new double[5];
                    double[] largestDifferenceInterval = new double[3];
                    for (int minute = 0; minute < (NUM_HOURS * MINUTES_PER_HOUR); minute++)
                    {

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
    }
}
