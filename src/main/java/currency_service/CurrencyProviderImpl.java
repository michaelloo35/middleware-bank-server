package currency_service;

import currency_service.proto.gen.Currencies;
import currency_service.proto.gen.CurrencyProviderGrpc;
import currency_service.proto.gen.CurrencyType;
import currency_service.proto.gen.ExchangeRate;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

//TODO inform about rates onConnect
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
//        scheduler.scheduleAtFixedRate(this::notifyBanks, 0, CurrencyConstants.UPDATE_PERIOD_SECONDS, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::simulateFluctuation, 0, CurrencyConstants.FLUCTUATION_PERIOD_SECONDS, TimeUnit.SECONDS);
    }


    @Override
    public void getExchangeRates(Currencies request, StreamObserver<ExchangeRate> responseObserver) {
        request.getCurrencyList().forEach(c -> banksByCurrencies.get(c).add(responseObserver));
    }

    private void notifyBanks() {
        logger.debug("Notifying banks..");

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
        logger.debug("Fluctuation began..");
        for (CurrencyType currencyType : CurrencyType.values()) {

            // simulate that some currencies may not change at all within given fluctuation
            if (new Random().nextInt(2) == 1) {
                // program flow should not allow missing values since they are assigned in constructor just in case set default to negative value to indicate error
                double fluctuation = exchangeRates.getOrDefault(currencyType, -1.0) * ((new Random().nextDouble() / 2.0) + 0.75);
                exchangeRates.replace(currencyType, fluctuation);

                banksByCurrencies
                        .get(currencyType)
                        .forEach(bank -> bank.onNext(ExchangeRate
                                .newBuilder()
                                .setCurrency(currencyType)
                                .setRate(exchangeRates.get(currencyType))
                                .build()));

            }
        }
    }
}
