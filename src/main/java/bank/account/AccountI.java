package bank.account;

import bank.generated.bank.Account;
import com.zeroc.Ice.Current;

public class AccountI implements Account {

    private final String firstName;
    private final String lastName;
    private final String pesel;
    private double monthlyIncome;
    private double balance;

    public AccountI(String firstName, String lastName, String pesel, double monthlyIncome, double balance) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.pesel = pesel;
        this.monthlyIncome = monthlyIncome;
        this.balance = balance;
    }

    @Override
    public double accountBalance(Current current) {
        return balance;
    }
}
