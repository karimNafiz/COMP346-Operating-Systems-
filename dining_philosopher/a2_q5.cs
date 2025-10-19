public class Monitor_FileControl
{


}


/*

    psuedocode according to textbook

Monitor FileControl{

    // locks
    condition writeCond, readCond;

    // bools
    bool isWriterActive;
    bool isWriterWaiting;
    
    // ints
    int activeReaders;



    void WriteEntry()
    {
        isWriterWaiting = true;

        while(isWriterActive || activeReaders > 0)
        {
            writeCond.Wait();
        }

        isWriterActive = true;
        isWriterWaitting = false;
    
    }

    void WriteExit()
    {
        if(isWriterWaiting){
            writerCond.Signal();
        }else{
            readCond.Signal();
        }
    }

    void ReadEntry()
    {
        while(isWriterActive){
            readCond.Wait();
        }

        activeReaders++;
    }

    void ReadExit()
    {
        if(isWriterWaiting){
            writeCond.Signal()
        }else{
            readCond.Signal()
        }

        activeReaders--;
    
    }


}




*/