
#ifndef CALC_ICE
#define CALC_ICE

module BankClient
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
  exception DateRangeError{
    string reason;
  };

  interface Account{
  double accountBalance();
  void deposit(double value);
  };

  interface PremiumAccount extends Account{
    CreditInfo applyForCredit(double value, CurrencyType currency, Date from, Date to ) throws NotAuthrorizedException,DateRangeError;
  };

  interface AccountFactory{
    Account* create(string firstName, string lastName, string pesel, double monthlyIncome) throws NoIncomeException;
  };

};

#endif
