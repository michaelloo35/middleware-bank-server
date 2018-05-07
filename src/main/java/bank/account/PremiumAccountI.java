package bank.account;

import bank.generated.bank.*;
import com.zeroc.Ice.Current;

import java.util.HashMap;

public class PremiumAccountI extends AccountI implements PremiumAccount {

    private static final double INTEREST_RATE = 1.5;
    private final HashMap<currency_service.proto.gen.CurrencyType, Double> exchangeRates;

    public PremiumAccountI(HashMap<currency_service.proto.gen.CurrencyType, Double> exchangeRates,
                           String firstName,
                           String lastName,
                           String pesel,
                           double monthlyIncome,
                           double balance) {

        super(firstName, lastName, pesel, monthlyIncome, balance);
        this.exchangeRates = exchangeRates;
    }

    @Override
    public CreditInfo getCreditInfo(Date from, Date to, CurrencyType currency, double value, Current current) throws DateRangeError, IllegalCurrencyException, NotAuthrorizedException {

        checkDate(from, to);

        // save the base value
        double baseValue = value;

        // calculate cost
        value *= INTEREST_RATE;

        Double exchangeRate = exchangeRates.get(currency);

        // make sure that bank offers desired currency
        if (exchangeRate == null)
            throw new IllegalCurrencyException();

        // return creditInfo
        return new CreditInfo(CurrencyType.PLN, baseValue, currency, value * (1 / exchangeRate));

    }

    @Override
    public double accountBalance(Current current) {
        return super.accountBalance(current);
    }

    private void checkDate(Date from, Date to) throws DateRangeError {
        java.util.Date convertedFrom = new java.util.Date(from.year, from.month, from.day);
        java.util.Date convertedTo = new java.util.Date(to.year, to.month, to.day);

        if (convertedTo.before(convertedFrom))
            throw new DateRangeError();
    }
}
