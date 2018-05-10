import Ice
import sys

import bank

Ice.loadSlice('../bank/slice/bank.ice')

currencies = {
    'USD': bank.CurrencyType.USD,
    'EUR': bank.CurrencyType.EUR,
    'PLN': bank.CurrencyType.PLN
}


def account_create():
    try:
        global user_input, pesel, account
        first_name, last_name, pesel, monthly_income, initial_deposit = input(
            "specify account details in format :\nfirstName lastName pesel monthlyIncome initialDeposit\n").split(' ')

        monthly_income = float(monthly_income)
        initial_deposit = float(initial_deposit)
        return factory.create(first_name, last_name, pesel, monthly_income, initial_deposit)
    except Ice.Exception:
        print("Oops seems like your pesel number already exist in the system")


def account_login():
    try:
        global pesel, obj, account
        pesel = input("specify your pesel number\n")
        obj = communicator.stringToProxy("account/" + pesel + ":tcp -h localhost -p 10001:udp -h localhost -p 10001")
        return bank.AccountPrx.checkedCast(obj)

    except Ice.Exception:
        print("User not existing")


with Ice.initialize(sys.argv) as communicator:
    base = communicator.stringToProxy(
        "accountFactory/accountFactory1:tcp -h localhost -p 10001:udp -h localhost -p 10001")
    factory = bank.AccountFactoryPrx.checkedCast(base)

    user_input = input("create to create account \nlogin to login\n")
    account = None

    if user_input == 'create':
        account = account_create()

    else:
        account = account_login()

    while user_input != 'exit':
        user_input = input("create/login/credit/balance\n")

        if user_input == 'create':
            account = account_create()

        elif user_input == 'login':
            account = account_login()

        elif user_input == 'balance':
            print("Your balance " + str(account.accountBalance()))

        elif user_input == 'credit':
            try:
                premium_account = bank.PremiumAccountPrx.uncheckedCast(account)

                # d, m, y = input("Specify FROM date format:[dd mm yyyy]\n").split(' ')
                date_from = input("Specify FROM date format:[dd mm yyyy]\n")

                date_to = input("Specify TO date format:[dd mm yyyy]\n")

                print("available currencies: " + str(currencies.keys()))
                currency, value = input("Specify currency and value format:[currency value]\n").split(' ')

                value = float(value)
                currency = currencies[currency]

                credit_info = premium_account.getCreditInfo(date_from, date_to, currency, value)

                print(str(credit_info))

            except Ice.OperationNotExistException:
                print("Oops it seems like you don't qualify for premium account service")
            except bank.DateRangeError:
                print("End date should be after begin date")
            except bank.IllegalCurrencyException:
                print("This bank does not offer credit in specified currency")


