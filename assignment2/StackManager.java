

public class StackManager
{
    // The Stack
    private static CharStack stack;
    private static final int NUM_ACQREL = 4; // Number of Producer/Consumer threads
    private static final int NUM_PROBERS = 1; // Number of threads dumping stack
    private static int iThreadSteps = 3; // Number of steps they take
    // Semaphore declarations. Insert your code in the following:
    //...
    //...
    // The main()

    /*
     * Declaring the necessary semaphores
     */
    private static Semaphore stackFull;
    private static Semaphore stackEmpty;
    private static Semaphore stackLock;





    public static void main(String[] argv)
    {
        // Some initial stats...
        try
        {
            stack = new CharStack(20);
            System.out.println("Main thread starts executing.");
            System.out.println("Initial value of top = " + stack.getTop() + ".");
            System.out.println("Initial value of stack top = " + stack.pick() + ".");
            System.out.println("Main thread will now fork several threads.");
            
            System.out.println("stack.getTop()+1 "+(stack.getTop()+1));

            System.out.println("getSize() - getTop() "+(stack.getSize() - (stack.getTop()+1)));

            stackFull = new Semaphore(stack.getTop()+1);
            stackEmpty = new Semaphore(stack.getSize() - stack.getTop());
            stackLock = new Semaphore(1);
        }
        catch(CharStackInvalidSizeException e){
            System.out.println(e.getMessage());
        }
        catch(CharStackEmptyException e)
        {
            System.out.println("Caught exception: StackCharEmptyException");
            System.out.println("Message : " + e.getMessage());
            System.out.println("Stack Trace : ");
            e.printStackTrace();
        }
        /*
        * The birth of threads
        */
        Consumer ab1 = new Consumer();
        Consumer ab2 = new Consumer();
        System.out.println ("Two Consumer threads have been created.");
        Thread rb1 = new Producer();
        Producer rb2 = new Producer();
        System.out.println ("Two Producer threads have been created.");
        CharStackProber csp = new CharStackProber();
        System.out.println ("One CharStackProber thread has been created.");
        /*
        * start executing
        */
        ab1.start();
        rb1.start();
        ab2.start();
        rb2.start();
        csp.start(); 
        /*
        * Wait by here for all forked threads to die
        */
        try
        {
            ab1.join();
            ab2.join();
            rb1.join();
            rb2.join();
            csp.join();
            // Some final stats after all the child threads terminated...
            System.out.println("System terminates normally.");
            System.out.println("Final value of top = " + stack.getTop() + ".");
            System.out.println("Final value of stack top = " + stack.pick() + ".");
            System.out.println("Final value of stack top-1 = " + stack.getAt(stack.getTop() - 1) + ".");
            /*
             * need to fix this shit later
             */
            //System.out.println("Stack access count = " + stack.getAccessCounter());
        }
        catch(InterruptedException e)
        {
                System.out.println("Caught InterruptedException: " + e.getMessage());
                    System.exit(1);
        }
        catch(Exception e)
        {
                    System.out.println("Caught exception: " + e.getClass().getName());
                    System.out.println("Message : " + e.getMessage());
                    System.out.println("Stack Trace : ");
                    e.printStackTrace();
        }
    } // main()
        /*
        * Inner Consumer thread class
        */
        static class Consumer extends BaseThread
        {
            private char copy; // A copy of a block returned by pop()
            public void run()
            {
                System.out.println ("Consumer thread [TID=" + this.iTID + "] starts executing.");
                for (int i = 0; i < StackManager.iThreadSteps; i++)  {
                        try{
                            stackFull.P();
                            stackLock.P();
                            System.out.println("Consumer thread [TID=" + this.iTID + "] acquired lock");
                            copy = CharStack.pop();
                            System.out.println("Consumer thread [TID=" + this.iTID + "] pops character =" + this.copy);
                        
                        }catch(Exception e){
                            // try again maybe
                            System.out.println("exception e"+e.getMessage());
                            i--;
                        }finally{
                            System.out.println("Consumer thread [TID=" + this.iTID + "] released lock");
                            stackEmpty.V();
                            stackLock.V();
                        }
                }
                System.out.println ("Consumer thread [TID=" + this.iTID + "] terminates.");
            }
          } // class Consumer
           /*
          * Inner class Producer
           */
          static class Producer extends BaseThread
          {
            private char block; // block to be returned
            public void run()
            {
                
                System.out.println ("Producer thread [TID=" + this.iTID + "] starts executing.");
                for (int i = 0; i < StackManager.iThreadSteps; i++)  {
                    try{
                        stackEmpty.P();
                        stackLock.P();
                        System.out.println ("Producer thread [TID=" + this.iTID + "] acquired lock.");
                        char top = CharStack.pick();
                        block = (char) (stack.pick() + 1);
                        CharStack.push(block);
                        System.out.println("Producer thread [TID=" + this.iTID + "] pushes character =" + this.block);


                    }catch(Exception e){
                        // we just need to try 
                        // not sure what to do 
                        System.out.println("exception e"+e.getMessage());
                        i--;
                    }finally{
                        System.out.println ("Producer thread [TID=" + this.iTID + "] released lock");
                        stackLock.V();
                        stackFull.V();
                    }


                }
                System.out.println("Producer thread [TID=" + this.iTID + "] terminates.");
                

            }
          } // class Producer
            /*
           * Inner class CharStackProber to dump stack contents
            */
           static class CharStackProber extends BaseThread
           {
                public void run()
                {
                    System.out.println("CharStackProber thread [TID=" + this.iTID + "] starts executing.");
                    for (int i = 0; i < 2 * StackManager.iThreadSteps; i++)
                    {
                        try{
                            stackLock.P();
                            for(int j = 0; j < stack.getSize();j++){
                                System.out.print("["+stack.getAt(j)+"]");
                            }
                            System.out.println();
                        }catch(Exception e){
                            System.out.println("exception e"+e.getMessage());
                            continue;
                        }finally{
                            stackLock.V();
                        }

                    }
                }
           } // class CharStackProber
} // class StackManager


/*
 * different situation
 * the stack can be full
 * if a producer tries to produce something, and the stack is full, it should wait for someone to consume something
 * if a consumer tries to consumer something, and the stack is full, it should wait for a producer to produce something
 * need a semaphore, called stackFull
 * need a semaphore, called stackEmpty
 * need a semaphore, called a lock (shared among others)
 * 
 * 
 * 
 * 
 */


/*


    Semaphore semFull = new Semaphore(iTop+1)
    Semaphore semEmpty = new Semaphore(iSize - iTop)
    Semaphore stackLock = new Semaphore(1)


    producer loop{
        wait(semFull)
        wait(stackLock)

        char top = stack[iTop]
        char next_top = top + 1

        stack.push(next_top)

        signal(semEmpty)
        signal(stackLock)


    }

    consumer loop{
        wait(semEmpty)
        wait(stackLock)

        char top = stack.pop
        print it out

        signal(semFull)
        signal(stackLock)
    
    
    }

    

    


 */