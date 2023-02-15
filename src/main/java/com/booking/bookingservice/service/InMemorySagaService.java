package com.booking.bookingservice.service;

/**
 * An in-memory implementation of a saga service.
 */

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.camel.CamelContext;
import org.apache.camel.saga.CamelSagaCoordinator;
import org.apache.camel.saga.CamelSagaService;
import org.apache.camel.saga.CamelSagaStep;
import org.apache.camel.support.service.ServiceSupport;
import org.apache.camel.util.ObjectHelper;

/**
 * An in-memory implementation of a saga service.
 */
public class InMemorySagaService extends ServiceSupport implements CamelSagaService {

    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 5;

    public static final long DEFAULT_RETRY_DELAY_IN_MILLISECONDS = 5000;

    private CamelContext camelContext;

    private Map<String, CamelSagaCoordinator> coordinators = new ConcurrentHashMap<>();

    private ScheduledExecutorService executorService;

    private int maxRetryAttempts = DEFAULT_MAX_RETRY_ATTEMPTS;

    private long retryDelayInMilliseconds = DEFAULT_RETRY_DELAY_IN_MILLISECONDS;

    @Override
    public CompletableFuture<CamelSagaCoordinator> newSaga() {
        ObjectHelper.notNull(camelContext, "camelContext");

        String uuid = camelContext.getUuidGenerator().generateUuid();
        CamelSagaCoordinator coordinator = new InMemorySagaCoordinator(camelContext, this, uuid);
        coordinators.put(uuid, coordinator);

        return CompletableFuture.completedFuture(coordinator);
    }

    @Override
    public CompletableFuture<CamelSagaCoordinator> getSaga(String id) {
        return CompletableFuture.completedFuture(coordinators.get(id));
    }

    @Override
    public void registerStep(CamelSagaStep step) {
        // do nothing
    }

    @Override
    protected void doStart() throws Exception {
        if (this.executorService == null) {
            this.executorService = camelContext.getExecutorServiceManager()
                    .newDefaultScheduledThreadPool(this, "saga");
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (this.executorService != null) {
            camelContext.getExecutorServiceManager().shutdownGraceful(this.executorService);
            this.executorService = null;
        }
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public long getRetryDelayInMilliseconds() {
        return retryDelayInMilliseconds;
    }

    public void setRetryDelayInMilliseconds(long retryDelayInMilliseconds) {
        this.retryDelayInMilliseconds = retryDelayInMilliseconds;
    }

}