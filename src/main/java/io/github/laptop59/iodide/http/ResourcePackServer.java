package io.github.laptop59.iodide.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.laptop59.iodide.Iodide;
import net.kyori.adventure.resource.ResourcePackInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An HTTP server that hosts a resource pack.
 */
public class ResourcePackServer implements AutoCloseable {
    public HttpServer httpServer;
    public String hash = null;
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
        final File file = Iodide.INSTANCE.getDataFolder().toPath().resolve("resources.zip").toFile();

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

    public void updateHash() {
        final File file = Iodide.INSTANCE.getDataFolder().toPath().resolve("resources.zip").toFile();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            final byte[] buf = new byte[8192];
            int read;
            while ((read = fileInputStream.read(buf)) != -1) {
                digest.update(buf, 0, read);
            }
            hash = bytesToString(digest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String bytesToString(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b & 0xff));
        }
        return builder.toString();
    }

    public void sendPackToEveryone() {
        Bukkit.getServer().getOnlinePlayers().forEach(this::sendPackToPlayer);
    }

    public void sendPackToPlayer(Player player) {
        player.sendResourcePacks(
            ResourcePackInfo.resourcePackInfo()
                .uri(URI.create("http://127.0.0.1:" + PORT + "/"))
                .hash(hash)
                .build()
        );
    }

    @Override
    public void close() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }
}
