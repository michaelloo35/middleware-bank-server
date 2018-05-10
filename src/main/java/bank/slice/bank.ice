
#ifndef CALC_ICE
#define CALC_ICE

module bank
{
  enum CurrencyType {USD = 0,EUR = 1,PLN = 2};

  class CreditInfo{
  CurrencyType baseCreditCurrency;
  double baseCost;
  CurrencyType actualCreditCurrency;
  double actualCost;
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
    CreditInfo getCreditInfo(string from, string to, CurrencyType currency, double value ) throws DateRangeError,IllegalCurrencyException;
  };

  interface AccountFactory{
    Account* create(string firstName, string lastName, string pesel, double monthlyIncome,double balance) ;
  };


};

#endif
