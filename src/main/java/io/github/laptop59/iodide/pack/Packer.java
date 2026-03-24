package io.github.laptop59.iodide.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.laptop59.iodide.Iodide;
import io.github.laptop59.iodide.http.ResourcePackServer;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Packer {
    public static final String VERTEX_SHADER_REL_PATH = "assets/minecraft/shaders/core/rendertype_text.vsh";
    public static final String ANCHOR_FONT_REL_PATH = "assets/iodide/font/anchor.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final ExecutorService executor =
            Executors.newFixedThreadPool(1);

    /**
     * Reloads this packer and registries associated.
     * This method must be called on the server thread.
     */
    public CompletableFuture<Void> reload() {
        Iodide.logger().info("Reloading...");
        CountDownLatch eventsFiredLatch = new CountDownLatch(1);
        CompletableFuture<Void> success = new CompletableFuture<>();

        executor.submit(() -> {
            try {
                extractTemplate();
                try {
                    eventsFiredLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    success.completeExceptionally(e);
                    return;
                }
                // Now the anchors have been registered, so we're free to make the pack!
                regenerateZip();
                Bukkit.getScheduler().runTask(Iodide.INSTANCE, () -> {
                    ResourcePackServer rpServer = Iodide.RESOURCE_PACK_SERVER;
                    rpServer.updateHash();
                    rpServer.sendPackToEveryone();
                });
                success.complete(null);
            } catch (Exception e) {
                success.completeExceptionally(e);
            }
        });

        Iodide.ANCHOR_REGISTRY.reload();
        eventsFiredLatch.countDown();

        return success;
    }

    public CompletableFuture<Void> forceResetTemplate() {
        Iodide.logger().info("Forcefully resetting template...");
        CountDownLatch eventsFiredLatch = new CountDownLatch(1);
        CompletableFuture<Void> success = new CompletableFuture<>();

        executor.submit(() -> {
            try {
                extractTemplate(true);
                success.complete(null);
            } catch (Exception e) {
                success.completeExceptionally(e);
            }
        });
        eventsFiredLatch.countDown();

        return success;
    }

    /**
     * Extracts default resources from the packer into the plugin's folder by
     * loading the template into the plugin folder if non-existent.
     */
    public void extractTemplate() {
        extractTemplate(false);
    }

    /**
     * Extracts default resources from the packer into the plugin's folder by
     * loading the template into the plugin folder if forced to or non-existent.
     */
    public void extractTemplate(boolean forceExtract) {
        Path dataFolder = Iodide.INSTANCE.getDataFolder().toPath();
        Path templateFolder = dataFolder.resolve("template");

        try {
            if (!forceExtract && templateFolder.toFile().exists())
                return;

            // Create the new folder in a temporary directory to prevent race conditions.
            Path tmpFolder = Files.createTempDirectory(dataFolder, "tmp_template");

            // Extract the resources.zip file.
            try (InputStream resourceSource = Packer.class.getResourceAsStream("/template/resources.zip");
                ZipInputStream zipInputStream = new ZipInputStream(resourceSource)) {
                ZipEntry entry;
                byte[] buffer = new byte[1024];
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    Path path = tmpFolder.resolve(entry.getName()).normalize();

                    if (!path.startsWith(tmpFolder)) {
                        throw new RuntimeException("Bad zip entry: " + entry.getName());
                    }

                    File file = path.toFile();

                    if (entry.isDirectory()) {
                        Files.createDirectories(path);
                    } else {
                        File parent = file.getParentFile();
                        Files.createDirectories(parent.toPath());

                        try (FileOutputStream outputStream = new FileOutputStream(file)) {
                            int len;
                            while ((len = zipInputStream.read(buffer)) > 0)
                                outputStream.write(buffer, 0, len);
                        }
                    }
                }
            }

            // Delete pre-existing one
            if (forceExtract) {
                try (Stream<Path> stream = Files.walk(templateFolder)) {
                    stream
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                } catch (NoSuchFileException ignored) {}
            }
            // Copy them over
            try {
                Files.move(tmpFolder, templateFolder, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmpFolder, templateFolder);
            } catch (FileAlreadyExistsException e) {
                // Already exists. So we ignore
                return;
            }
            Iodide.logger().info("Extracted Iodide template...");
        } catch (FileAlreadyExistsException e) {
            // Do nothing
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void regenerateZip() {
        // We zip up the template folder.
        Path dataFolder = Iodide.INSTANCE.getDataFolder().toPath();
        Path templateFolder = dataFolder.resolve("template");

        if (!templateFolder.toFile().exists()) {
            extractTemplate();
        }

        Path zipFile = dataFolder.resolve("resources.zip");

        try (FileOutputStream fileOutputStream = new FileOutputStream(zipFile.toFile());
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
             try (Stream<Path> stream = Files.walk(templateFolder)) {
                 stream
                     .filter(Files::isRegularFile)
                     .forEach(path -> {
                         String entryName = templateFolder.relativize(path).toString().replace("\\", "/");

                         try {
                             zipOutputStream.putNextEntry(new ZipEntry(entryName));

                             switch (entryName) {
                                 case VERTEX_SHADER_REL_PATH -> {
                                     String result = Files.readString(path);
                                     result = result.replace("/* anchors substitution */", Iodide.ANCHOR_REGISTRY.getGlsl());
                                     zipOutputStream.write(result.getBytes(StandardCharsets.UTF_8));
                                 }
                                 case ANCHOR_FONT_REL_PATH -> {
                                     JsonObject font = generateAnchorFontObject();
                                     String result = escapeUnicode(GSON.toJson(font));
                                     zipOutputStream.write(result.getBytes(StandardCharsets.UTF_8));
                                 }
                                 default -> Files.copy(path, zipOutputStream);
                             }
                         } catch (IOException e) {
                             throw new RuntimeException(e);
                         }
                     });
             }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Iodide.logger().info("Successfully generated the Iodide resource pack!");
    }

    /** Replaces \E000 and above with their {@code \}{@code uXXXX} counterpart. */
    private static String escapeUnicode(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c < '\uE000')
                sb.append(c);
            else
                sb.append(String.format("\\u%04X", (int) c));
        }
        return sb.toString();
    }

    private JsonObject generateAnchorFontObject() {
        JsonObject font = new JsonObject();

        JsonArray providers = new JsonArray();
        JsonObject provider = new JsonObject();

        provider.addProperty("type", "space");
        provider.add("advances", Iodide.ANCHOR_REGISTRY.advances());

        providers.add(provider);
        font.add("providers", providers);
        return font;
    }
}
