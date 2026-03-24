package io.github.laptop59.iodide.text;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossbarManager implements Listener {
    private final Map<UUID, IodideBossbar> bossbarMap = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        IodideBossbar bossbar;
        bossbarMap.put(player.getUniqueId(), bossbar = new IodideBossbar(player.getUniqueId()));
        bossbar.show();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        bossbarMap.remove(player.getUniqueId()).hide();
    }
}
