package currency_service;

import currency_service.proto.gen.Currency;
import currency_service.proto.gen.CurrencyProviderGrpc;
import currency_service.proto.gen.CurrencyType;
import currency_service.proto.gen.ExchangeRate;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

public class CurrencyProviderImpl extends CurrencyProviderGrpc.CurrencyProviderImplBase {

    private final static Logger logger = Logger.getLogger(CurrencyProviderImpl.class);
    private final ConcurrentMap<CurrencyType, Double> exchangeRates = new ConcurrentHashMap<>();
    private final ConcurrentMap<CurrencyType, Set<StreamObserver<ExchangeRate>>> banksByCurrencies = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;

    public CurrencyProviderImpl() {

        exchangeRates.putIfAbsent(CurrencyType.EUR, CurrencyConstants.EUR_TO_PLN_BASE);
        exchangeRates.putIfAbsent(CurrencyType.USD, CurrencyConstants.USD_TO_PLN_BASE);
        exchangeRates.putIfAbsent(CurrencyType.PLN, CurrencyConstants.PLN_TO_PLN_BASE);


        for (CurrencyType currencyType : CurrencyType.values()) {
            banksByCurrencies.put(currencyType, new HashSet<>());
        }
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(this::notifyBanks, CurrencyConstants.UPDATE_PERIOD_SECONDS, TimeUnit.SECONDS);
        scheduler.schedule(this::simulateFluctuation, CurrencyConstants.FLUCTUATION_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public StreamObserver<Currency> getExchangeRates(StreamObserver<ExchangeRate> responseObserver) {

        return new StreamObserver<Currency>() {

            @Override
            public void onNext(Currency currency) {
                banksByCurrencies.get(currency.getCurrency()).add(responseObserver);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.warn(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("Stream completed");
            }
        };
    }


    private void notifyBanks() {
        banksByCurrencies
                .keySet()
                .forEach(currencyType -> banksByCurrencies
                        .get(currencyType)
                        .forEach(bank -> bank.onNext(ExchangeRate
                                .newBuilder()
                                .setCurrency(currencyType)
                                .setRate(exchangeRates.get(currencyType))
                                .build())));
    }

    private void simulateFluctuation() {
        for (CurrencyType currencyType : CurrencyType.values()) {

            // simulate that some currencies may not change at all within given fluctuation
            if (new Random().nextInt(2) == 1) {
                // program flow should not allow missing values since they are assigned in constructor just in case set default to negative value to indicate error
                double fluctuation = exchangeRates.getOrDefault(currencyType, -1.0) * ((new Random().nextDouble() / 2.0) + 0.75);
                exchangeRates.replace(currencyType, fluctuation);
            }
        }
    }
}
