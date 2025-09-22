package Task4;

/**
 * Class Withdrawer 
 * Implements Withdrawer thread class.
 *
 * @author Malek Barhoush;
 * 
 * $Revision: 1.0 $  
 * 
 */

public class Withdrawer extends Thread { 
	private Account account ;
	public Withdrawer(Account account){
		this.account = account;
	}
	
	public void run(){
	
		// Withdraw 10 CAD into instance variable account
		for (int i=0;i<10000000;i++)
		{
			account.withdraw(10);
			/*
			try {
				sleep(10);
			} catch (InterruptedException e) { 
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
	}

}
