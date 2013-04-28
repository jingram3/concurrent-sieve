/**Joseph Ingram
 * Concurrent Prime Sieve
 * Fall 2012 CS493E
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class ConcurrentSieve {
	
	public int sieve(int last) {
		int count = 0;
		try {
			final List<Callable<Integer>> partitions = new ArrayList<Callable<Integer>>(); 
			final ExecutorService executorPool = Executors.newFixedThreadPool(4);
			int sliceSize = last/4;
			for(int i=2;i<=last;i+=sliceSize) {
				final int from = i;
				int to = i + sliceSize;
				if(to>last) //don't go out of range
					to=last;
				final int end = to;
				partitions.add(new Callable<Integer>() { //partition each thread
					public Integer call() {
						return sieveInRange(from, end);
					}        
				});
			}
			final List<Future<Integer>> resultFromParts = executorPool.invokeAll(partitions, 10000, TimeUnit.SECONDS);
			executorPool.shutdown(); 
			for(final Future<Integer> result : resultFromParts)  
				count += result.get(); 
		} 
		catch(Exception ex) { 
			throw new RuntimeException(ex); 
		}
		
		return count;
	}

	private int sieveInRange(int from, int to) {
		int size = (to-from+1)/2; //divide by 2 since we're only testing the odd numbers
		boolean[] isPrime = new boolean[size];
		for(int i=0;i<size;i++) {
			isPrime[i]=true; //assume all prime at start
		}
		
		//only look at odd values for i and where i^2 is less than to
		for(int i=3;i*i<=to;i+=2) { 
			
			//skip a few obviously not prime numbers
			if(i>=9 && i%3==0)
				continue;
			if(i>=25 && i%5==0)
				continue;
			if(i>=49 && i%7==0)
				continue;
			
			int first = ((from+i-1)/i)*i; //first multiple of i above from
			if(first<i*i) 
				first = i*i; //can skip to square of i, otherwise the first multiple is faster

			if(first%2==0) 
				first += i; //first must be odd
			
			for(int j=first;j<=to;j+=i*2) { //i*2 so you're only adding even numbers to the initially odd j
				isPrime[(j-from)/2] = false;
			}
		}
		
		int count = 0;
		for(int i=0;i<size;i++) { //count the primes
			if(isPrime[i])
				count++;
		}
		if(from==2) //2 is prime
			count++;
		return count;
	}
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		int count = new ConcurrentSieve().sieve(10000000);
		System.out.println("Primes: " + count);
		System.out.println(System.currentTimeMillis()-startTime);
	}
}
