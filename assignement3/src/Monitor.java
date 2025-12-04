import java.util.*;

/** 
 * Class Monitor 
 * To synchronize dining philosophers. 
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca  
 */
public class Monitor   
{
	/*
	 * ------------    
	 * Data members 
	 * ------------
	 */

	// Number of philosophers sitting at the table
	private int philosopherCount;

	// Possible states of a philosopher
	private static final int THINKING = 0;
	private static final int HUNGRY   = 1;
	private static final int EATING   = 2;

	// Current state of each philosopher
	private int[] philosopherStates;

	// Queue of philosophers waiting to eat needed for starvation
	private LinkedList<Integer> hungryPhilosopherQueue = new LinkedList<Integer>();


	// Talking control: at most one philosopher may talk at a time
	private boolean isAnyPhilosopherTalking = false;

	private boolean isAnyPhilosopherEating = false;

	// need a queue to avoid starvation of threads who want to speak
	private LinkedList<Integer> requestTalkPhilosopherQueue = new LinkedList<Integer>();

	/**
	 * Constructor
	 */
	public Monitor(int philosopherCount) throws Exception
	{
		/*
		!!! must handle exceptions here
		the number of philosophers can't be below 0 or over a certain limit
		*/
		if(philosopherCount < 0){
			throw new Exception("philosopher count can't be below zero");
		}

		this.philosopherCount = philosopherCount;

		// set every philosopher to thinking 
		philosopherStates = new int[philosopherCount];
		for(int i = 0; i < philosopherCount; i++)
		{
			philosopherStates[i] = THINKING;
		}
	}


	/**
	 * Helper to convert philosopher ids to zero based
	 */
	private int tidToIndex(final int piTID)
	{
		// BaseThread guarantees TIDs are assigned starting from 1 in order.
		return (piTID - 1) % philosopherCount;
	}

	/**
	 * Index of the left neighbour of i.
	 */
	private int leftOf(final int i)
	{
		return (i + philosopherCount - 1) % philosopherCount;
	}

	/**
	 * Index of the right neighbour of i.
	 */
	private int rightOf(final int i)
	{
		return (i + 1) % philosopherCount;
	}

	/*
		we first convert the threadID to zero based index called currentPhilosopherIndex
		we then check if it is thinking or not, if it is thinking, we change the state to HUNGRY
		and we add it to the queue
		we then check if any neighbour is eating or not
		and we also check if it is in the top of the queue which stored hungry philosopohers

		if no philosopher is eating and the current philosopher is at the top of the queue, we set isAnyPhilosopherEating to true
	
	*/
	public synchronized void pickUp(final int piTID)
	{
		int currentPhilosopherIndex = tidToIndex(piTID);
		//System.out.println(piTID+" entered pickup ");

		// Mark this philosopher as hungry and remember the order of arrival
		if(philosopherStates[currentPhilosopherIndex] == THINKING)
		{
			philosopherStates[currentPhilosopherIndex] = HUNGRY;
			hungryPhilosopherQueue.add(currentPhilosopherIndex);
		}

		// Wait until:
		//   1) no neighbour is eating, and
		//   2) this philosopher is at the head of the hungry queue
		while(true)
		{
			int left  = leftOf(currentPhilosopherIndex);
			int right = rightOf(currentPhilosopherIndex);

			boolean areNeighboursEating =
				(philosopherStates[left]  == EATING) ||
				(philosopherStates[right] == EATING);

			boolean iscurrentPhilosopherIndexHeadOfQueue =
				!hungryPhilosopherQueue.isEmpty() && hungryPhilosopherQueue.peek() == currentPhilosopherIndex;

			if(!areNeighboursEating && iscurrentPhilosopherIndexHeadOfQueue && !isAnyPhilosopherEating)
			{
				// Safe to eat now: remove from queue and switch to EATING
				
				//System.out.println(piTID + " state set to eating ");
				isAnyPhilosopherEating = true;
				philosopherStates[currentPhilosopherIndex] = EATING;
				hungryPhilosopherQueue.removeFirst();
				// for debugging
				// if(!hungryPhilosopherQueue.isEmpty()){
				// 	System.out.println(hungryPhilosopherQueue);
				// }
				break;
			}

			try
			{
				// Wait until someone puts down chopsticks or leaves the queue
				wait();
			}
			catch(InterruptedException e)
			{
				System.err.println("Monitor.pickUp():");
				DiningPhilosophers.reportException(e);
				System.exit(1);
			}
		}
	}

	/**
	 * When a given philosopher is done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		int currentPhilosopherIndex = tidToIndex(piTID);

		// when you put down your chopsticks, you start thiking, others can eat
		isAnyPhilosopherEating = false;
		philosopherStates[currentPhilosopherIndex] = THINKING;

		
		/*
			must use notify all, multiple philosophers might be hungry
			its important
		*/

		
		notifyAll();
	}

	/**
	 * Only one philosopher at a time is allowed to talk
	 * (while they are not eating).
	 * I am not explicitely checking if a philosopher is eating or not in this function, because in the philosopher code we only wish to speak after putting down our chopsticks 
	 */
	public synchronized void requestTalk(final int piTID)
	{
		// Thread oSelf = Thread.currentThread();
		int currentPhilosopherIndexIndex = tidToIndex(piTID);

		// Add this thread to the talk queue if it is not there yet
		if(!requestTalkPhilosopherQueue.contains(currentPhilosopherIndexIndex))
		{
			requestTalkPhilosopherQueue.add(currentPhilosopherIndexIndex);
		}

		// Wait until no other philosopher is talking and
		// this philosopher is at the head of the talk queue.
		while(isAnyPhilosopherTalking || requestTalkPhilosopherQueue.peek() != currentPhilosopherIndexIndex)
		{
			try
			{
				wait();
			}
			catch(InterruptedException e)
			{
				System.err.println("Monitor.requestTalk():");
				DiningPhilosophers.reportException(e);
				System.exit(1);
			}
		}

		// This philosopher may now talk
		isAnyPhilosopherTalking = true;
		// Remove from the head of the queue
		requestTalkPhilosopherQueue.removeFirst();
	}

	/**
	 * When one philosopher is done talking, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk(final int piTID)
	{
		// Free the talking resource and wake up waiting philosophers
		isAnyPhilosopherTalking = false;
		notifyAll();
	}
}

// EOF
