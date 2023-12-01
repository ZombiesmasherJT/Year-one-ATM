
/**
 * OverdraftAccount inherits from BankAccount and utilises an overdraft
 *
 * @author Josh Tite
 * @version 1.0.0
 */
public class OverdraftAccount extends BankAccount
{
    /**
     * The amount of overdraft in this account
     */
    public int overdraftAmount = 1000;
    
    public OverdraftAccount(int a, int p, int b, int o)
    {
        accNumber = a;
        accPasswd = p;
        balance = b;
        overdraftAmount = o;
    }
    
    /**
     * A method to withdraw that overrides the withdraw superclass method
     * @param amount to withdraw
     * @return Returns a negative balance within the overdraft if needed unlike the superclass method
     */
    @Override
    public boolean withdraw( int amount ) 
    { 
        Debug.trace( "BankAccount::withdraw: amount =" + amount );
        //Check if the balance after the withdraw is within the overdraft
        if ((balance - amount) < 0 - overdraftAmount) {
            return false;
        } else {
            balance = balance - amount;
            return true; 
        }
    }
    
    /**
     * A method to return the overdraft overdraft
     */
    public int getOverdraft() {
        return overdraftAmount;
    }

    
}
