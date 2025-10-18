
/*
Semaphore phaseOneEvent = new Semaphore(0)
Semaphore phaseTwoEndEvent = new Semaphore(0)

Semaphore[] phaseTwoEvents = new Semaphore[N]


for(Semaphore phaseTwoEvent in phaseTwoEvents){
    phaseTwoEvent = new Semaphore(0)
}



process 0 {

    int counter = 0;
    while(counter < N){
        phaseOneEvent.Wait()
        counter++
    }


    for(int i = N -1; i >= 0;i--){
        phaseTwoEvents[i].Signal()
        phaseTwoEndEvent.Wait()
    }



}


process i{

    Phase 1
    phaseOneEvent.Signal()

    phaseTwoEvents[i].Wait()
    Phase 2
    phaseTwoEndEvent.Signal()

}





*/

