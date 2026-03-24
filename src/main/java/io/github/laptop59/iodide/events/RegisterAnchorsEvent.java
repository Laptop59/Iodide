package io.github.laptop59.iodide.events;

import io.github.laptop59.iodide.anchor.Anchor;
import io.github.laptop59.iodide.hud.AnchorRegistry;
import io.github.laptop59.iodide.hud.ExceededAnchorLimitException;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that fires when it's time to register anchors that can be used in HUDs.
 */
public class RegisterAnchorsEvent extends Event {
    @NotNull
    private final AnchorRegistry registry;

    public RegisterAnchorsEvent(AnchorRegistry registry) {
        this.registry = registry;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Registers an anchor to the registry.
     * @param anchor The anchor to register.
     * @throws ExceededAnchorLimitException If the anchor limit has been exceeded.
     */
    public void register(Anchor anchor) throws ExceededAnchorLimitException {
        this.registry.register(anchor);
    }
}
