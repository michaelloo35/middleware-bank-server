package bank.account;

import bank.generated.bank.*;
import com.zeroc.Ice.Current;

import java.util.Date;
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
    public CreditInfo getCreditInfo(String from, String to, CurrencyType currency, double value, Current current) throws DateRangeError, IllegalCurrencyException, NotAuthrorizedException {

        checkDate(from, to);

        // save the base value
        double baseValue = value;

        // calculate cost
        value *= INTEREST_RATE;

        Double exchangeRate = exchangeRates.get(convertCurrencyType(currency));

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

    private void checkDate(String from, String to) throws DateRangeError {

        String[] dateF = from.split(" ");
        String[] dateT = to.split(" ");

        java.util.Date convertedFrom = new java.util.Date(new Integer(dateF[2]), new Integer(dateF[1]), new Integer(dateF[0]));
        java.util.Date convertedTo = new java.util.Date(new Integer(dateT[2]), new Integer(dateT[1]), new Integer(dateT[0]));

        if (convertedTo.before(convertedFrom))
            throw new DateRangeError();
    }

    private currency_service.proto.gen.CurrencyType convertCurrencyType(CurrencyType currencyType) {

        switch (currencyType) {
            case USD:
                return currency_service.proto.gen.CurrencyType.USD;
            case EUR:
                return currency_service.proto.gen.CurrencyType.EUR;
            case PLN:
                return currency_service.proto.gen.CurrencyType.PLN;
            default:
                return currency_service.proto.gen.CurrencyType.PLN;
        }
    }
}
