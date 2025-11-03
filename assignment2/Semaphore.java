public class Semaphore {
    private int value;
    public Semaphore(int value)
    {
            this.value = value;
    }
    public Semaphore()
    {
                this(0);
    }
    public synchronized void Wait()
    {
        /*
         * first fix, the following line was at the end of the function
         * brought it at the top of the function
         */
        this.value--;    

        /*
         * second fix, the following intially was this.value <= 0
         * changed it to this.value < 0
         */
        while (this.value < 0)
        {
            try
            {
                wait();
            }
            catch(InterruptedException e)
            {
                System.out.println ("Semaphore::Wait() - caught InterruptedException: " + e.getMessage() );
                e.printStackTrace();
            }
        }
    }
    public synchronized void Signal()
    {
        ++this.value;
        notify();
    }
    public synchronized void P()
    {
        this.Wait();
    }
    public synchronized void V()
    {
        this.Signal();
    }
    
}
