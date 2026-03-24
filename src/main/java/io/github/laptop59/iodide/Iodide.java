package io.github.laptop59.iodide;

import io.github.laptop59.iodide.http.ResourcePackServer;
import io.github.laptop59.iodide.hud.AnchorRegistry;
import io.github.laptop59.iodide.pack.Packer;
import io.github.laptop59.iodide.text.BossbarManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.logging.Logger;

public final class Iodide extends JavaPlugin {
    public static Iodide INSTANCE;
    public static IodideListener LISTENER;
    public static Packer PACKER;
    public static ResourcePackServer RESOURCE_PACK_SERVER;
    public static BossbarManager BOSSBAR_MANAGER;
    public static AnchorRegistry ANCHOR_REGISTRY;

    public final static String NAMESPACE = "iodide";

    @Override
    public void onEnable() {
        INSTANCE = this;
        ANCHOR_REGISTRY = initializeIfNull(ANCHOR_REGISTRY, AnchorRegistry::new);
        PACKER = initializeIfNull(PACKER, Packer::new);
        LISTENER = initializeIfNull(LISTENER, IodideListener::new);

        RESOURCE_PACK_SERVER = initializeIfNull(RESOURCE_PACK_SERVER, ResourcePackServer::new);
        RESOURCE_PACK_SERVER.start();

        BOSSBAR_MANAGER = initializeIfNull(BOSSBAR_MANAGER, BossbarManager::new);

        getServer().getPluginManager().registerEvents(LISTENER, this);
        getServer().getPluginManager().registerEvents(BOSSBAR_MANAGER, this);

        // Now load everything
        PACKER.reload();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(IodideCommand.createCommand());
        });
    }

    @Override
    public void onDisable() {
        RESOURCE_PACK_SERVER.stop();
    }

    static <T> @NotNull T initializeIfNull(@Nullable T object, Supplier<@NotNull T> factory) {
        return object == null ? factory.get() : object;
    }

    public static Logger logger() {
        return INSTANCE.getLogger();
    }
}
