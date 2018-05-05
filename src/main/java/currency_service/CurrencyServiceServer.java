package currency_service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.log4j.Logger;

import java.io.IOException;

public class CurrencyServiceServer {

    private static final Logger logger = Logger.getLogger(CurrencyServiceServer.class);

    private final static int PORT = 50051;
    private Server server;

    private void start() throws IOException {

        server = ServerBuilder.forPort(PORT)
                .addService(new CurrencyProviderImpl())
                .build()
                .start();

        logger.info("*** server started, listening on " + PORT);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.info("*** shutting down gRPC server since JVM is shutting down");
                CurrencyServiceServer.this.stop();
                logger.info("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final CurrencyServiceServer server = new CurrencyServiceServer();
        server.start();
        server.blockUntilShutdown();
    }

}
