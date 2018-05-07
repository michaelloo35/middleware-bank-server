package bank.account;

import bank.generated.bank.AccountFactory;
import bank.generated.bank.AccountPrx;
import bank.generated.bank.NoIncomeException;
import bank.generated.bank.PremiumAccountPrx;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import currency_service.proto.gen.CurrencyType;

import java.util.HashMap;

public class AccountFactoryI implements AccountFactory {

    private static final int HIGHEST_BASIC_INCOME = 10_000;
    private final HashMap<CurrencyType, Double> exchangeRates;
    private final ObjectAdapter objectAdapter;

    public AccountFactoryI(HashMap<CurrencyType, Double> exchangeRates, ObjectAdapter objectAdapter) {
        this.exchangeRates = exchangeRates;
        this.objectAdapter = objectAdapter;
    }

    @Override
    public AccountPrx create(String firstName, String lastName, String pesel, double monthlyIncome, double balance, Current current) throws NoIncomeException {

        if (monthlyIncome > HIGHEST_BASIC_INCOME)
            return PremiumAccountPrx.uncheckedCast(objectAdapter.add(
                    new PremiumAccountI(
                            exchangeRates,
                            firstName,
                            lastName,
                            pesel,
                            monthlyIncome,
                            balance), new Identity(pesel, "account")));

        else
            return AccountPrx.uncheckedCast(objectAdapter.add(
                    new AccountI(
                            firstName,
                            lastName,
                            pesel,
                            monthlyIncome,
                            balance), new Identity(pesel, "account")));

    }
}
