package io.github.laptop59.iodide.hud;

import com.google.gson.JsonObject;
import io.github.laptop59.iodide.anchor.Anchor;
import io.github.laptop59.iodide.events.RegisterAnchorsEvent;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.bukkit.Bukkit;

import java.util.ArrayList;

/**
 * A device that represents all the existing anchors in the system.
 * They are relative to screen size, so it is possible to use them
 * for alignment to screen borders.
 * <p>
 * Anchors currently store:
 * <ul>
 *     <li>Their relative screen width
 *     <li>Their relative screen height
 * </ul>
 * <p>
 * There is a fundamental limit of 1024 (2<sup>10</sup>) anchors.
 */
public class AnchorRegistry {
    public static final int CAPACITY = 1024;
    public static final int NEGATION_COMPENSATION = 1 << 10;

    private final Object2IntMap<Anchor> registryFrom = new Object2IntArrayMap<>();
    private final ArrayList<Anchor> registryTo = new ArrayList<>();
    private String glsl = "";

    /**
     * Creates a new registry of anchors.
     * Initially, it is not initialized. When the server initializes,
     * call the {@link AnchorRegistry#reload} method.
     */
    public AnchorRegistry() {}

    /**
     * Clears this registry of all anchors, meaning that
     * after this operation completes, no anchors will
     * be registered.
     */
    public void clear() {
        this.registryFrom.clear();
        this.registryTo.clear();
    }

    /**
     * Registers an anchor to the registry.
     * @param anchor The anchor to register.
     * @throws ExceededAnchorLimitException If the anchor limit has been exceeded.
     */
    public void register(Anchor anchor) throws ExceededAnchorLimitException {
        if (this.registryFrom.containsKey(anchor)) return;

        if (this.registryTo.size() >= CAPACITY) {
            throw new ExceededAnchorLimitException(CAPACITY);
        }

        this.registryFrom.put(anchor, this.registryTo.size());
        this.registryTo.add(anchor);
    }

    /**
     * Gets the index of a provided anchor from the registry.
     */
    public int indexOf(Anchor anchor) throws UnregisteredAnchorException {
        if (this.registryFrom.containsKey(anchor))
            return this.registryFrom.getInt(anchor);
        else
            throw new UnregisteredAnchorException();
    }

    /**
     * Gives the inner body of a switch statements (GLSL) from the shaders provided,
     * which has already been generated.
     */
    public String getGlsl() {
        return glsl;
    }

    /**
     * Generates the inner body of a switch statements (GLSL) from the shaders provided.
     */
    String innerSwitchGlsl() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.registryTo.size(); i++) {
            result.append("case ");
            result.append(i);
            result.append(":");
            result.append(this.registryTo.get(i));
            result.append("\n");
        }
        return result.toString();
    }

    public JsonObject advances() {
        JsonObject advances = new JsonObject();
        int base = 1 << 23;
        char ch = '\uE000';
        for (int i = 0; i < this.registryTo.size(); i++) {
            int pixels = base | (i << 13) + NEGATION_COMPENSATION;
            advances.addProperty(Character.toString(ch++), pixels);
            advances.addProperty(Character.toString(ch++), -pixels);
        }
        return advances;
    }

    /**
     * Reloads this registry by calling a Bukkit event to refresh the anchors.
     * This must be called on the server thread!
     */
    public void reload() {
        clear();
        Bukkit.getPluginManager().callEvent(new RegisterAnchorsEvent(this));

        // Compile the GLSL.
        glsl = innerSwitchGlsl();
    }
}
