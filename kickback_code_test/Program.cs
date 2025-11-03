using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

public class CollectEngineEventConcurrentlyTest
{
    private readonly SemaphoreSlim _signalEventCreation;
    private readonly SemaphoreSlim _signalEventConsumption; // kept for parity (not strictly needed)
    private readonly ConcurrentQueue<Action> _eventQueue;

    public CollectEngineEventConcurrentlyTest()
    {
        _signalEventCreation = new SemaphoreSlim(0);
        _signalEventConsumption = new SemaphoreSlim(0);
        _eventQueue = new ConcurrentQueue<Action>();
    }

    // NOTE: requires C# 7.1+ for async Main; C# 8+ for IAsyncEnumerable
    public static async Task Main(string[] args)
    {
        var test = new CollectEngineEventConcurrentlyTest();

        // Kick off the collector (fire-and-forget for this POC)
        _ = test.CollectEngineEvents();

        int x = 0;
        // Simple "game loop"
        while (true)
        {
            await Task.Delay(1000);
            Console.WriteLine($"loop count {x}");
            test.Update();
            test.LateUpdate();
            x++;
        }
    }

    public void Update()
    {
        // Signal that it's okay to create some events
        _signalEventCreation.Release();
        _signalEventCreation.Release();
    }

    public void LateUpdate()
    {
        // Drain and invoke any collected actions
        while (_eventQueue.TryDequeue(out var action))
        {
            action?.Invoke();
        }

        // Optional: signal "consumed" for anyone waiting
        _signalEventConsumption.Release();
    }

    // Produces 10 actions each time _signalEventCreation is released (twice per Update in this POC)
    public async IAsyncEnumerable<Action> GenerateEvents()
    {
        while (true)
        {
            // Wait asynchronously for a signal to generate a batch
            await _signalEventCreation.WaitAsync();

            for (int i = 0; i < 10; i++)
            {
                await Task.Delay(20); // simulate work
                int capture = i; // capture loop var safely
                yield return () => Console.WriteLine($"I am task {capture}");
            }
        }
    }

    // Collects the async stream of actions and enqueues them for LateUpdate to execute
    public async Task CollectEngineEvents()
    {
        await foreach (var action in GenerateEvents())
        {
            _eventQueue.Enqueue(action);
        }
    }
}
