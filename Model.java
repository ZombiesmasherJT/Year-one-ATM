
// The model represents all the actual content and functionality of the app
// For the ATM, it keeps track of the information shown in the display
// (the title and two message boxes), and the interaction with the bank, executes
// commands provided by the controller and tells the view to update when
// something changes
/**
 * The model represents all the actual content and functionality of the app
 * For the ATM, it keeps track of the information shown in the display
 * (the title and two message boxes), and the interaction with the bank, executes
 * commands provided by the controller and tells the view to update when
 * something changes
 *
 * @author Josh Tite
 * @version 1.0.0
 */
public class Model 
{
    // the ATM model is always in one of three states - waiting for an account number, 
    // waiting for a password, or logged in and processing account requests. 
    // We use string values to represent each state:
    // (the word 'final' tells Java we won't ever change the values of these variables)
    final String ACCOUNT_NO = "account_no";
    final String PASSWORD = "password";
    final String LOGGED_IN = "logged_in";

    // variables representing the ATM model
    String state = ACCOUNT_NO;      // the state it is currently in
    int  number = 0;                // current number displayed in GUI (as a number, not a string)
    Bank  bank = null;              // The ATM talks to a bank, represented by the Bank object.
    int accNumber = -1;             // Account number typed in
    int accPasswd = -1;             // Password typed in
    // These three are what are shown on the View display
    String title = "Bank ATM";      // The contents of the title message
    String display1 = null;         // The contents of the Message 1 box (a single line)
    String display2 = null;         // The contents of the Message 2 box (may be multiple lines)

    // The other parts of the model-view-controller setup
    public View view;
    public Controller controller;
    
    // Here we create a new UserActions type variable called userAction which is used when asking the user for a yes no prompt
    UserActions userAction = UserActions.NO_ACTION;
    
    enum UserActions {
        NO_ACTION,
        COMPLETE_ACTION,
        PROMPT_FOR_BALANCE,
        PROMPT_FINISHED_BALANCE
    }
    
    // Model constructor - we pass it a Bank object representing the bank we want to talk to
    public Model(Bank b)
    {
        Debug.trace("Model::<constructor>");          
        bank = b;
    }

    // Initialising the ATM (or resetting after an error or logout)
    // set state to ACCOUNT_NO, number to zero, and display message 
    // provided as argument and standard instruction message
    public void initialise(String message) {
        setState(ACCOUNT_NO);
        number = 0;
        display1 = message; 
        display2 =  "Enter your account number\n" +
        "Followed by \"Ent\"";
    }

    // use this method to change state - mainly so we print a debugging message whenever 
    //the state changes
    public void setState(String newState) 
    {
        if ( !state.equals(newState) ) 
        {
            String oldState = state;
            state = newState;
            Debug.trace("Model::setState: changed state from "+ oldState + " to " + newState);
        }
    }

    // These methods are called by the Controller to change the Model
    // when particular buttons are pressed on the GUI
    
    // process a number key (the key is specified by the label argument)
    public void processNumber(String label)
    {
        //If the user clicks a number whilst in a yes or no menu, dont process the number inputted
        if(userAction != UserActions.NO_ACTION) return;
        
        // a little magic to turn the first char of the label into an int
        // and update the number variable with it
        char c = label.charAt(0);
        number = number * 10 + c-'0';           // Build number 
        // show the new number in the display
        display1 = "" + number;
        display();  // update the GUI
    }

    // process the Clear button - reset the number (and number display string)
    public void processClear()
    {
        // clear the number stored in the model
        number = 0;
        display1 = "";
        display();  // update the GUI
    }

    // process the Enter button
    // this is the most complex operation - the Enter key causes the ATM to change state
    // from account number, to password, to logged_in and back to account number
    // (when you log out)
    public void processEnter()
    {
        // Enter was pressed - what we do depends what state the ATM is already in
        switch ( state )
        {
            case ACCOUNT_NO:
                // we were waiting for a complete account number - save the number we have
                // reset the tyed in number to 0 and change to the state where we are expecting 
                // a password
                accNumber = number;
                number = 0;
                setState(PASSWORD);
                display1 = "";
                display2 = "Now enter your password\n" +
                "Followed by \"Ent\"";
                break;
            case PASSWORD:
                // we were waiting for a password - save the number we have as the password
                // and then cotnact the bank with accumber and accPasswd to try and login to
                // an account
                accPasswd = number;
                number = 0;
                display1 = "";
                // now check the account/password combination. If it's ok go into the LOGGED_IN
                // state, otherwise go back to the start (by re-initialsing)
                if ( bank.login(accNumber, accPasswd) )
                {
                    setState(LOGGED_IN);
                    display2 = "Accepted\n" +
                    "Now enter the transaction you require";
                } else {
                    initialise("Unknown account/password");
                }
                break;
                case LOGGED_IN:
                
            default: 
                // do nothing in any other state (ie logged in)
        }  
        display();  // update the GUI
    }
    
    //Handler for the yes button press
    public void processYes()
    {
        switch(userAction) {
            //This case the user pressed yes to seeing their balance
            case PROMPT_FOR_BALANCE:
                userAction = UserActions.PROMPT_FINISHED_BALANCE;
                processBalance("Press 'Yes' to continue to Withdraw");
                break;
            //This case the user pressed yes into continuing with the withdraw transaction
            case PROMPT_FINISHED_BALANCE:
                userAction = UserActions.COMPLETE_ACTION;
                processWithdraw();
                break;
            default:
        }
        display();  // update the GUI
    }
    
    //Handler for the no button press
    public void processNo()
    {
        switch(userAction) {
            //This case the user pressed no to seeing their balance
            case PROMPT_FOR_BALANCE:
                userAction = UserActions.COMPLETE_ACTION;
                processWithdraw();
                break;
            default:
        }
        display();  // update the GUI
    }

    // Withdraw button - check we are logged in and if so try and withdraw some money from
    // the bank (number is the amount showing in the interface display)
    public void processWithdraw()
    {
        if (state.equals(LOGGED_IN) ) {
            //User presses the withdraw for the first time
            if(userAction == UserActions.NO_ACTION) {
                userAction = UserActions.PROMPT_FOR_BALANCE;
                display2 = "Would you like to see your Balance? Yes/No";
            //This is activated if the user has said yes or no to seeing their balance
            } else if(userAction == UserActions.COMPLETE_ACTION) {
                if ( bank.withdraw( number ) )
                {
                    display2 =   "Withdrawn: " + number;
                } else {
                    display2 =   "You do not have sufficient funds";
                }
                number = 0;
                display1 = "";     
                userAction = UserActions.NO_ACTION;
            } else {
                display2 = "You have not selected Yes or No";
            }
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }

    // Deposit button - check we are logged in and if so try and deposit some money into
    // the bank (number is the amount showing in the interface display)
    public void processDeposit()
    {
        if (state.equals(LOGGED_IN) ) {
            bank.deposit( number );
            display1 = "";
            display2 = "Deposited: " + number;
            number = 0;
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }

    // Balance button - check we are logged in and if so access the current balance
    // and display it
    // Takes an extra output to concatinate to the end of the balance output for a yes or no menu
    public void processBalance(String extraOutput)
    {
        if (state.equals(LOGGED_IN) ) {
            //Check if the bank account has an overdraft
            if(bank.hasOverdraft()) {
                int displayBalance = bank.getBalance();
                int displayOverdraft = bank.getOverdraft();
                //If the balance is less than 0, display a different message
                if(displayBalance < 0) {
                    display2 = "Account has an Overdraft\nYou are �" + Math.abs(displayBalance) 
                    + " into your �" + displayOverdraft + " Overdraft"
                    + "\nFinal balance is " + displayBalance + " Pounds\n" + extraOutput; 
                } else {
                    display2 = "Account has an Overdraft of " + displayOverdraft
                    + "\n Current balance is �" + displayBalance  + "\n" + extraOutput; 
                }
                
            } else {
                display2 = "Your balance is: �" + bank.getBalance() + "\n" + extraOutput;
            }
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }

    // Finish button - check we are logged in and if so log out
    public void processFinish()
    {
        if (state.equals(LOGGED_IN) ) {
            // go back to the log in state
            setState(ACCOUNT_NO);
            number = 0;
            display2 = "Welcome: Enter your account number";
            bank.logout();
        } else {
            initialise("You are not logged in");
        }
        display();  // update the GUI
    }

    // Any other key results in an error message and a reset of the GUI
    public void processUnknownKey(String action)
    {
        // unknown button, or invalid for this state - reset everything
        Debug.trace("Model::processUnknownKey: unknown button \"" + action + "\", re-initialising");
        // go back to initial state
        initialise("Invalid command");
        display();
    }

    // This is where the Model talks to the View, by calling the View's update method
    // The view will call back to the model to get new information to display on the screen
    public void display()
    {
        Debug.trace("Model::display");
        view.update();
    }
    
    public void processHelp()
    {
        //Display the help menu
        display2 = "Welcome to the ATM Help menu!\n"
        + "+-----------------------------\n"
        + "| 1 | Enter your account      \n"
        + "| 1 | number with the keypad  \n"
        + "|-----------------------------\n"
        + "| 2 | Enter the password      \n"
        + "|-----------------------------\n"
        + "| 3 | Enter the ammount then  \n"
        + "| 3 | action. e.g. 15 W/D to  \n"
        + "| 3 | withdraw �15            \n"
        + "|-----------------------------\n"
        + "| 4 | Press Fin to Finish     \n"
        + "+-----------------------------\n";
        view.update();
    }
}
