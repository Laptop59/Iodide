package io.github.laptop59.iodide.elements;

import io.github.laptop59.iodide.text.IodideText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/** A class that assists in rendering an element. */
public class Renderer {
    protected int widthInPixels = 0;
    protected TextComponent.Builder builder = Component.text();

    /**
     * Advances the rendering cursor to the number of pixels in the right direction.
     */
    public void advance(int pixels) {
        builder.append(IodideText.getAdvanceComponent(pixels));
        widthInPixels += pixels;
    }

    /**
     * Draws a text component, given its width in pixels (the width <b>MUST INCLUDE THE ADVANCE SPACE!</b>)
     * The cursor is then moved to the right appropriately.
     */
    public void drawAndMove(Component component, int width) {
        builder.append(component);
        widthInPixels += width;
    }

    /**
     * Draws a text component, given its width in pixels (the width <b>MUST INCLUDE THE ADVANCE SPACE!</b>)
     * The cursor will stay at the position it originally after the text is drawn.
     */
    public void draw(Component component, int width) {
        // No change in width: width + (-width) = 0
        builder.append(component);
        builder.append(IodideText.getAdvanceComponent(-width));
    }
}
