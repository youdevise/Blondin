package com.timgroup.blondin.throttler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

import static java.util.concurrent.TimeUnit.MINUTES;

public final class ThrottlingHandler implements Container {

    private final Container handler;
    private final ThreadPoolExecutor executor;

    public ThrottlingHandler(Container handler, int bandwidth) {
        this.handler = handler;
        this.executor = new ThreadPoolExecutor(1, bandwidth, 10, MINUTES, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void handle(final Request request, final Response response) {
        this.executor.execute(new Runnable() {
            @Override public void run() {
                handler.handle(request, response);
            }
        });
    }
    
    public long receivedTaskCount() {
        return executor.getTaskCount();
    }

    public long completedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    public int activeTaskCount() {
        return executor.getActiveCount();
    }
}
