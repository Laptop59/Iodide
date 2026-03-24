package io.github.laptop59.iodide;

import io.github.laptop59.iodide.events.RegisterAnchorsEvent;
import io.github.laptop59.iodide.anchor.Anchor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class IodideListener implements Listener {
    public static final Anchor CENTER = Anchor.center();

    @EventHandler
    public void registerAnchors(RegisterAnchorsEvent event) {
        // If we don't register our anchors here,
        // we won't be able to use them later!
        event.register(CENTER);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Iodide.RESOURCE_PACK_SERVER.sendPackToPlayer(event.getPlayer());
    }
}
