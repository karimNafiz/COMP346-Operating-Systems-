using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

void Main()
{
    var table = new Table(totalCount: 5, joinCount: 4);

    var philosophers = Enumerable.Range(0, 5)
        .Select(i => new Philosopher(table, i))
        .ToArray();

    // Launch each philosopher in its own task
    var tasks = new List<Task>(philosophers.Length);
    foreach (var p in philosophers)
    {
        tasks.Add(Task.Run(() =>
        {
            while (true)
            {
                p.TryJoinTable();   // block until seated
                p.Think();          // think a bit
                p.TryEat();         // pick up both and eat
                // After eating, the philosopher leaves by itself,
                // but the Table might also randomly drop someone via the Eat event.
            }
        }));
    }

    Task.WaitAll(tasks.ToArray()); // (you probably won’t reach here in this demo)
}

// -------------------- Domain --------------------

class Philosopher
{
    public int Index { get; }
    public Chopstick LeftChopstick { get; private set; }
    public Chopstick RightChopstick { get; private set; }
    public Table DiningTable { get; }

    // Make event public so Table can subscribe/unsubscribe.
    public event EventHandler Eat;

    /*
        why does it have to be thread local?
    */
    private static readonly ThreadLocal<Random> _rng = new(() => new Random(unchecked(Environment.TickCount * 31 + Thread.CurrentThread.ManagedThreadId)));

    public Philosopher(Table table, int index)
    {
        DiningTable = table;
        Index = index;
    }

    public void Think()
    {
        int ms = _rng.Value!.Next(500, 1500);
        Task.Delay(ms).Wait();
    }

    public void TryJoinTable()
    {
        DiningTable.TryJoin(this);
    }

    public void TryEat()
    {
        // With only 4 seated out of 5 chopsticks, deadlock is prevented in this variant.
        TryPickUpLeftChopstick(DiningTable.GetLeftChopstick(this));
        TryPickUpRightChopstick(DiningTable.GetRightChopstick(this));

        // "Eat"
        int ms = _rng.Value!.Next(400, 1000);
        Console.WriteLine($"P{Index} is eating...");
        Task.Delay(ms).Wait();

        // Notify table we are eating (for “random drop” policy).
        Eat?.Invoke(this, EventArgs.Empty);

        // Finish eating: drop both, leave the table voluntarily.
        /*
            need to change the logic here
        */
        DropChopsticks();
        DiningTable.Leave(this);
    }

    public void DropChopsticks()
    {
        // Use try/finally pattern if you later restructure this.
        RightChopstick?.Drop();
        RightChopstick = null;
        LeftChopstick?.Drop();
        LeftChopstick = null;
    }

    private void TryPickUpLeftChopstick(Chopstick c)
    {
        c.PickUp();
        LeftChopstick = c;
    }

    private void TryPickUpRightChopstick(Chopstick c)
    {
        c.PickUp();
        RightChopstick = c;
    }
}

class Chopstick
{
    public int Index { get; }
    private readonly object _lock = new();

    public Chopstick(int index)
    {
        Index = index;
    }

    public void PickUp()
    {
        Monitor.Enter(_lock);
        Console.WriteLine($"chopstick {Index} picked up");
    }

    public void Drop()
    {
        Console.WriteLine($"chopstick {Index} dropped");
        Monitor.Exit(_lock);
    }
}

class Table
{
    private readonly HashSet<Philosopher> _seated = new();
    private readonly Chopstick[] _chopsticks;
    private readonly SemaphoreSlim _seats;
    private readonly object _lock = new();

    public int TotalCount { get; }
    public int JoinCount { get; }

    // RNG for random drop (protect with lock if accessed from multiple threads)
    private readonly Random _rand = new(Random.Shared.Next());

    public Table(int totalCount, int joinCount)
    {
        if (joinCount > totalCount || joinCount <= 0) throw new ArgumentException("Invalid joinCount");
        TotalCount = totalCount;
        JoinCount = joinCount;

        _chopsticks = Enumerable.Range(0, totalCount).Select(i => new Chopstick(i)).ToArray();
        _seats = new SemaphoreSlim(joinCount);
    }

    public void TryJoin(Philosopher p)
    {
        // Fast-path: already seated
        lock (_lock)
        {
            if (_seated.Contains(p)) return;
        }

        _seats.Wait(); // blocks until a seat is free

        lock (_lock)
        {
            // Re-check to avoid double-add
            if (_seated.Add(p))
            {
                // Subscribe table to philosopher's Eat event
                p.Eat += Philosopher_OnEatRice;
            }
            else
            {
                // If already added (rare race), return the seat
                _seats.Release();
            }
        }

        Console.WriteLine($"P{p.Index} joined the table");
    }

    public void Leave(Philosopher p)
    {
        bool removed = false;
        lock (_lock)
        {
            removed = _seated.Remove(p);
            if (removed)
            {
                // Unsubscribe from event to avoid leaks
                p.Eat -= Philosopher_OnEatRice;
                Console.WriteLine($"P{p.Index} left the table");
                _seats.Release();
            }
        }

    }

    public Chopstick GetLeftChopstick(Philosopher p)
    {
        // Left = (i + 1) % N (common convention)
        return _chopsticks[(p.Index + 1) % TotalCount];
    }

    public Chopstick GetRightChopstick(Philosopher p)
    {
        // Avoid negative modulo: (i + N - 1) % N
        return _chopsticks[(p.Index + TotalCount - 1) % TotalCount];
    }

    // ===== Completed callback =====
    // Randomly drop ONE currently-seated philosopher (could be the eater or someone else).
    // Unsubscribe from their event and free their seat.
    private void Philosopher_OnEatRice(object? sender, EventArgs e)
    {
        Philosopher[] snapshot;
        lock (_lock)
        {
            if (_seated.Count == 0) return;
            snapshot = _seated.ToArray();
        }

        Philosopher toDrop;
        lock (_lock)
        {
            // Re-evaluate under lock in case membership changed
            if (_seated.Count == 0) return;
            int idx = _rand.Next(_seated.Count);
            toDrop = _seated.ElementAt(idx);
        }

        // Ensure chopsticks are dropped before leaving.
        toDrop.DropChopsticks();
        Leave(toDrop);
    }
}
