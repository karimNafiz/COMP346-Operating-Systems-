

class Philosopher
{

    public int Index { get; private set; }

    public Philosopher(int index)
    {
        Index = index;
    }

    public void PickUpLeftChopstick(Chopstick chopstick)
    {
        PickUpChopstick(chopstick);
    }
    public void PickUpRightChopstick(Chopstick chopstick)
    {
        PickUpChopstick(chopstick);
    }

    private void PickUpChopstick(Chopstick chopstick)
    {
        
    }

}


class Chopstick
{
    
    public int Index { get; private set; }
    private object monitorObject;

    public Chopstick(int index)
    {
        Index = index;
        monitorObject = new object();
    }

    public void PickUp()
    {
        Monitor.Enter(monitorObject);
        Console.WriteLine($"chopstick {Index} picked up ");
    }

    public void Drop()
    {
        Console.WriteLine($"chopstick {Index} droppped ");
        Monitor.Exit(monitorObject);
    }

}


class Table
{
    private Philosopher[] philosophers;
    private Chopstick[] chopsticks;

    public int TotalCount { get; private set; }
    public int JoinCount { get; private set; }

    public Table(int totalCount, int JoinCount)
    {
        if (JoinCount > totalCount || JoinCount <= 0)
        {
            throw new Exception("Invalid JoinCount");
        }

        TotalCount = totalCount;
        philosophers = new Philosopher[TotalCount];
        chopsticks = new Chopstick[TotalCount];
    }

    


}
