class CharStackEmptyException extends Exception
{
           public CharStackEmptyException()
           {
                    super ("Char Stack is empty.");
           }
}

class CharStackFullException extends Exception
{
           public CharStackFullException()
          {
                  super ("Char Stack has reached its capacity of CharStack.MAX_SIZE.");
           }
}

class CharStackInvalidSizeException extends Exception
{
          public CharStackInvalidSizeException()
          {
                  super("Invalid stack size specified.");
          }
          public CharStackInvalidSizeException (int piStackSize)
          {
                  super ("Invalid stack size specified: " + piStackSize);
           }
}

class CharStackInvalidAceessException extends Exception
{
           // Fill it up yourself
}
