package com.timgroup.blondin.diagnostics;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class StatsdRecorder {

    private final Monitor monitor;
    private final String prefix;
    private final DatagramSocket clientSocket;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        final ThreadFactory delegate = Executors.defaultThreadFactory();
        @Override public Thread newThread(Runnable r) {
            Thread result = delegate.newThread(r);
            result.setName("Metrics-"+result.getName());
            return result;
        }
    });

    public StatsdRecorder(Monitor monitor, String prefix, String host, int port) {
        this.monitor = monitor;
        this.prefix = prefix;
        
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.connect(new InetSocketAddress(host, port));
        } catch (Exception e) {
            this.monitor.logError(StatsdRecorder.class, "Failed to open socket to statsd", e);
        }
        this.clientSocket = socket;
    }

    private void send(final String message) {
        executor.execute(new Runnable() {
            @Override public void run() {
                blockingSend(message);
            }
        });
    }

    public void stop() {
        try {
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            this.monitor.logWarning(StatsdRecorder.class, "Failed to shutdown statsd recorder", e);
        }
        finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }

    private void blockingSend(String message) {
        try {
            final byte[] sendData = message.getBytes();
            final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
            clientSocket.send(sendPacket);
        } catch (Exception e) {
            this.monitor.logWarning(StatsdRecorder.class, "Failed to write to statsd", e);
        }
    }

    public void record(String aspect, int value) {
        send(String.format("%s.%s:%d|c", prefix, aspect, value));
    }
}
