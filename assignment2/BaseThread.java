public class BaseThread extends Thread{
    /*
        * Data members
    */
    public static int iNextTID = 1; // Preserves value across all instances
    protected int iTID;
    public BaseThread()
    {
        /*
         * potential for race condition
         */
        this.iTID = iNextTID;
        iNextTID++;
    }
    public BaseThread(int piTID)
    {
        this.iTID = piTID;
    }
    
}
