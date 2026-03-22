package io.github.laptop59.iodide.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.laptop59.iodide.Iodide;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An HTTP server that hosts a resource pack.
 */
public class ResourcePackServer implements AutoCloseable {
    public HttpServer httpServer;
    public final int PORT = 10616;

    public void start() {
        if (httpServer != null) return;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        httpServer.createContext("/", this::sendPack);
        httpServer.setExecutor(Executors.newFixedThreadPool(8));
        httpServer.start();
        Iodide.logger().info("Started resource pack server at address " + httpServer.getAddress().toString());
    }

    public void stop() {
        if (httpServer == null) return;
        httpServer.stop(0);
        ((ExecutorService) httpServer.getExecutor()).shutdown();
        httpServer = null;
    }

    private void sendPack(HttpExchange httpExchange) {
        File file = null;

        if (file == null) {
            try {
                httpExchange.sendResponseHeaders(404, -1);
            } catch (IOException ignored) {}
            return;
        }

        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                OutputStream os = httpExchange.getResponseBody();
        ) {
            httpExchange.getResponseHeaders().add("Content-Type", "application/zip");
            httpExchange.sendResponseHeaders(200, file.length());
            byte[] buffer = new byte[16384];
            int bufferSize;
            while ((bufferSize = fileInputStream.read(buffer, 0, buffer.length)) >= 0) {
                os.write(buffer, 0, bufferSize);
            }
        } catch (FileNotFoundException e) {
            try {
                httpExchange.sendResponseHeaders(404, -1);
            } catch (IOException ignored) {}
        } catch (IOException e) {
            try {
                Iodide.logger().severe("I/O error while reading from resource pack to be downloaded.");
                httpExchange.sendResponseHeaders(500, -1);
            } catch (IOException ignored) {}
        } catch (Exception ignored) {}
    }

    @Override
    public void close() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }
}
