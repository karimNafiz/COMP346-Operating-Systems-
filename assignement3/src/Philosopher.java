import common.BaseThread;  

/**
 * Class Philosopher.
 * Outlines main subrutines of our virtual philosopher.  
 * 
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca  
 */
public class Philosopher extends BaseThread
{
	/**
	 * Max time an action can take (in milliseconds)   
	 */
	public static final long TIME_TO_WASTE = 1000; 

	/**
	 * The act of eating.
	 * - Print the fact that a given phil (their TID) has started eating. 
	 * - Then sleep() for a random interval.
	 * - The print that they are done eating.
	 */
	public void eat()
	{
		try
		{
			// Inform that this philosopher starts eating
			System.out.println("Philosopher " + getTID() + " starts eating.");
			// Simulate time spent eating
			sleep((long)(Math.random() * TIME_TO_WASTE));
			// Inform that this philosopher is done eating
			System.out.println("Philosopher " + getTID() + " finishes eating.");
		}
		catch(InterruptedException e)
		{
			System.err.println("Philosopher.eat():");
			DiningPhilosophers.reportException(e);
			System.exit(1);
		}
	}

	/**
	 * The act of thinking.
	 * - Print the fact that a given phil (their TID) has started thinking.
	 * - Then sleep() for a random interval.
	 * - The print that they are done thinking.
	 */
	public void think()
	{
		try
		{
			// Inform that this philosopher starts thinking
			System.out.println("Philosopher " + getTID() + " starts thinking.");
			// Simulate time spent thinking
			sleep((long)(Math.random() * TIME_TO_WASTE));
			// Inform that this philosopher is done thinking
			System.out.println("Philosopher " + getTID() + " finishes thinking.");
		}
		catch(InterruptedException e)
		{
			System.err.println("Philosopher.think():");
			DiningPhilosophers.reportException(e);
			System.exit(1);
		}
	}

	/**
	 * The act of talking.
	 * - Print the fact that a given phil (their TID) has started talking.
	 * - Say something brilliant at random
	 * - The print that they are done talking.
	 */
	public void talk()
	{
		// The monitor has already given us permission to talk at this point
		System.out.println("Philosopher " + getTID() + " starts talking.");

		saySomething();

		System.out.println("Philosopher " + getTID() + " finishes talking.");
	}

	/**
	 * No, this is not the act of running, just the overridden Thread.run()
	 */
	public void run()
	{
		for(int i = 0; i < DiningPhilosophers.DINING_STEPS; i++)
		{
			// Ask the monitor for permission to pick up chopsticks
			DiningPhilosophers.soMonitor.pickUp(getTID());

			eat();

			// Put down chopsticks through the monitor
			DiningPhilosophers.soMonitor.putDown(getTID());

			think();

			/*
			 * A decision is made at random whether this particular
			 * philosopher is about to say something terribly useful.
			 */
			// 50 50 chance 
			if(Math.random() < 0.5) // A random decision 
			{
				// Request permission to talk from the monitor
				DiningPhilosophers.soMonitor.requestTalk(getTID());

				// Actually talk
				talk();

				// Let others talk once we are done

				DiningPhilosophers.soMonitor.endTalk(getTID());
			}
		}
	} // run()

	/**
	 * Prints out a phrase from the array of phrases at random.
	 * Feel free to add your own phrases.
	 */
	public void saySomething()
	{
		String[] astrPhrases =
		{
			"Eh, it's not easy to be a philosopher: eat, think, talk, eat...",
			"You know, true is false and false is true if you think of it",
			"2 + 2 = 5 for extremely large values of 2...",
			"If thee cannot speak, thee must be silent",
			"My number is " + getTID() + ""
		};

		System.out.println
		(
			"Philosopher " + getTID() + " says: " +
			astrPhrases[(int)(Math.random() * astrPhrases.length)]
		);
	}
}

// EOF
