
#ifndef CALC_ICE
#define CALC_ICE

module bank
{
  enum AccountType {STANDARD = 0, PREMIUM = 1};
  enum CurrencyType {USD = 0,EUR = 1,PLN = 2};

  class CreditInfo{
  CurrencyType baseCreditCurrency;
  double baseCost;
  CurrencyType actualCreditCurrency;
  double actualCost;
  };

  class Date{
    byte day;
    byte month;
    short year;
  };

  exception NotAuthrorizedException{
    string reason;
  };
  exception NoIncomeException{};
  exception IllegalCurrencyException{};
  exception DateRangeError{
    string reason;
  };

  interface Account{
  double accountBalance();
  };

  interface PremiumAccount extends Account{
    CreditInfo getCreditInfo(Date from, Date to, CurrencyType currency, double value ) throws NotAuthrorizedException,DateRangeError,IllegalCurrencyException;
  };

  interface AccountFactory{
    Account* create(string firstName, string lastName, string pesel, double monthlyIncome,double balance) throws NoIncomeException;
  };


};

#endif
