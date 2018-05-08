package bank;

import bank.account.AccountFactoryI;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import currency_service.CurrencyConstants;
import currency_service.proto.gen.Currencies;
import currency_service.proto.gen.CurrencyProviderGrpc;
import currency_service.proto.gen.CurrencyType;
import currency_service.proto.gen.ExchangeRate;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Bank {

    private static final Logger logger = Logger.getLogger(Bank.class);
    private static final DecimalFormat df2 = new DecimalFormat("#.##");

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

        int status = 0;
        Communicator communicator = null;

        try {
            // grpc part
            subscribeToCurrenciesService();

            // 1. Inicjalizacja ICE - utworzenie communicatora
            communicator = Util.initialize();

            // 2. Konfiguracja adaptera
            // METODA 2 (niepolecana, dopuszczalna testowo): Konfiguracja adaptera Adapter1 jest w kodzie ?r?d?owym
            ObjectAdapter objectAdapter = communicator.createObjectAdapterWithEndpoints("Adapter1", "tcp -h localhost -p 10000:udp -h localhost -p 10000");

            // 3. Stworzenie serwanta/serwant?
            AccountFactoryI accountFactoryServant = new AccountFactoryI(exchangeRates, objectAdapter);

            // 4. Dodanie wpis?w do tablicy ASM
            objectAdapter.add(accountFactoryServant, new Identity("accountFactory1", "accountFactory"));


            // 5. Aktywacja adaptera i przej?cie w p?tl? przetwarzania ??da?
            objectAdapter.activate();

            System.out.println("Entering event processing loop...");

            communicator.waitForShutdown();


        } catch (Exception e) {
            System.err.println(e);
            status = 1;
        } finally {

            if (communicator != null) {
                // Clean up
                try {
                    communicator.destroy();
                } catch (Exception e) {
                    System.err.println(e);
                    status = 1;
                }
            }
            shutdown();
            System.exit(status);
        }


    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void subscribeToCurrenciesService() {

        currencyProviderStub.getExchangeRates(
                Currencies.newBuilder().addAllCurrency(exchangeRates.keySet()).build(),
                new StreamObserver<ExchangeRate>() {

                    @Override
                    public void onNext(ExchangeRate exchangeRate) {
                        exchangeRates.put(exchangeRate.getCurrency(), exchangeRate.getRate());
                        printRate(exchangeRate);
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


    private void printRate(ExchangeRate exchangeRate) {
        logger.info(exchangeRate.getCurrency() + " : " + df2.format(exchangeRate.getRate()));
    }

}
