package bank;

import currency_service.CurrencyConstants;
import currency_service.proto.gen.Currency;
import currency_service.proto.gen.CurrencyProviderGrpc;
import currency_service.proto.gen.CurrencyType;
import currency_service.proto.gen.ExchangeRate;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Bank {

    private static final Logger logger = Logger.getLogger(Bank.class);

    private final ManagedChannel channel;
    private final CurrencyProviderGrpc.CurrencyProviderStub currencyProviderStub;

    private final HashMap<CurrencyType, Double> exchangeRates = new HashMap<>();

    public Bank(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid needing certificates.
                .usePlaintext(true)
                .build();

        currencyProviderStub = CurrencyProviderGrpc.newStub(channel);
        //TODO to delete
        exchangeRates.put(CurrencyType.PLN, CurrencyConstants.PLN_TO_PLN_BASE);
        exchangeRates.put(CurrencyType.EUR, CurrencyConstants.EUR_TO_PLN_BASE);

    }

    public static void main(String[] args) throws Exception {
        Bank client = new Bank("localhost", 50051);
        client.start();
    }

    private void start() throws InterruptedException {

        shutdown();
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void subscribeToCurrenciesService() {

        currencyProviderStub.getExchangeRates(new StreamObserver<ExchangeRate>() {
            @Override
            public void onNext(ExchangeRate exchangeRate) {
                exchangeRates.put(exchangeRate.getCurrency(), exchangeRate.getRate());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.warn(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("Stream completed");
            }
        });

    }

    private void printRates() {
        exchangeRates.entrySet().forEach(System.out::println);
        System.out.println("==================");
    }

}
