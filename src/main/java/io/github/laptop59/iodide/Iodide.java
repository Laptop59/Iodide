package io.github.laptop59.iodide;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Iodide extends JavaPlugin {
    public static Iodide INSTANCE;
    public final static String NAMESPACE = "iodide";

    @Override
    public void onEnable() {
        INSTANCE = this;
        // Plugin startup logic
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Logger logger() {
        return INSTANCE.getLogger();
    }
}
