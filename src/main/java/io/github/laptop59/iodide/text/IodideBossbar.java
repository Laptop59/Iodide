package io.github.laptop59.iodide.text;

import io.github.laptop59.iodide.IodideListener;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A bossbar shown to its own player. */
public class IodideBossbar {
    private BossBar internalBossbar;
    private UUID uuid;
    private List<Component> title = new ArrayList<>();

    IodideBossbar(UUID uuid) {
        this.uuid = uuid;
        this.title.add(IodideListener.CENTER.use(Component.text("LET'S GIZ! YEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYEYE")));

        this.internalBossbar = BossBar.bossBar(
            Component.empty().children(title),
            0.0f,
            BossBar.Color.WHITE,
            BossBar.Overlay.PROGRESS
        );
    }

    public void show() {
        // Show this to the player.
        Player player = getPlayer();
        if (player != null) {
            player.showBossBar(internalBossbar);
        }
    }

    public void hide() {
        // Hide this to the player.
        Player player = getPlayer();
        if (player != null) {
            player.hideBossBar(internalBossbar);
        }
    }

    /** Gets the player to which this bossbar is shown. */
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
