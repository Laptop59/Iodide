package io.github.laptop59.iodide.anchor;

import io.github.laptop59.iodide.Iodide;
import io.github.laptop59.iodide.hud.UnregisteredAnchorException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

/**
 * Represents an anchor.
 * In simple terms, an anchor contains
 * its own GLSL code to render.
 */
public final class Anchor {
    private final String compiled;
    public static final Key FONT = Key.key(Iodide.NAMESPACE, "anchor");

    private Anchor(String glsl) {
        this.compiled = glsl;
    }

    private static void putRelativeCoords(StringBuilder output, float relativeX, float relativeY) {
        if (relativeX != 0.0f) {
            output.append(" relativeX = ");
            output.append(relativeX);
            output.append(';');
        }

        if (relativeY != 0.0f) {
            output.append(" relativeY = ");
            output.append(relativeY);
            output.append(';');
        }
    }

    private static void putOffsetCoords(StringBuilder output, int offsetX, int offsetY) {
        if (offsetX != 0.0f) {
            output.append(" offsetX = ");
            output.append(offsetX);
            output.append(';');
        }

        if (offsetY != 0.0f) {
            output.append(" offsetY = ");
            output.append(offsetY);
            output.append(';');
        }
    }

    /**
     * Constructs a new anchor at the provided
     * <b>RELATIVE SCREEN COORDINATES</b>.
     * @param relativeX Between 0 (left) and 1 (right)
     * @param relativeY Between 0 (top) and 1 (bottom)
     * @return The compiled anchor.
     */
    public static Anchor at(float relativeX, float relativeY) {
        StringBuilder output = new StringBuilder();
        putRelativeCoords(output, relativeX, relativeY);
        output.append(" break;");
        return custom(output.toString());
    }

    /**
     * Constructs a new anchor at the provided
     * <b>RELATIVE SCREEN COORDINATES</b> and with
     * a given offset in pixels.
     * @param relativeX Between 0 (left) and 1 (right)
     * @param relativeY Between 0 (top) and 1 (bottom)
     * @param offsetX Pixels to go right (negative for left)
     * @param offsetY Pixels to go down (negative for up)
     * @return The compiled anchor.
     */
    public static Anchor at(float relativeX, float relativeY, int offsetX, int offsetY) {
        StringBuilder output = new StringBuilder();
        putRelativeCoords(output, relativeX, relativeY);
        putOffsetCoords(output, offsetX, offsetY);
        output.append(" break;");
        return custom(output.toString());
    }

    /**
     * Constructs a new anchor at the center of the screen.
     * <pre>
     * {@code
     * ...
     * .#.
     * ...
     * }
     * @return The compiled anchor.
     */
    public static Anchor center() {
        return center(0, 0);
    }

    /**
     * Constructs a new anchor at the center of the screen,
     * along with a given pixel offset.
     * <pre>
     * {@code
     * ...
     * .#.
     * ...
     * }
     * @param offsetX Pixels to go right (negative for left)
     * @param offsetY Pixels to go down (negative for up)
     * @return The compiled anchor.
     */
    public static Anchor center(int offsetX, int offsetY) {
        return at(0.5f, 0.5f, offsetX, offsetY);
    }

    /**
     * Constructs a new anchor at the left middle point of the screen.
     * <pre>
     * {@code
     * ...
     * #..
     * ...
     * }
     * </pre>
     * @return The compiled anchor.
     */
    public static Anchor leftMiddle() {
        return leftMiddle(0, 0);
    }

    /**
     * Constructs a new anchor at the left middle point of the screen,
     * along with a given pixel offset.
     * <pre>
     * {@code
     * ...
     * #..
     * ...
     * }
     * </pre>
     * @param offsetX Pixels to go right (negative for left)
     * @param offsetY Pixels to go down (negative for up)
     * @return The compiled anchor.
     */
    public static Anchor leftMiddle(int offsetX, int offsetY) {
        return at(0f, 0.5f, offsetX, offsetY);
    }

    /**
     * Constructs a new anchor at the right middle point of the screen.
     * <pre>
     * {@code
     * ...
     * ..#
     * ...
     * }
     * </pre>
     * @return The compiled anchor.
     */
    public static Anchor rightMiddle() {
        return rightMiddle(0, 0);
    }

    /**
     * Constructs a new anchor at the right middle point of the screen,
     * along with a given pixel offset.
     * <pre>
     * {@code
     * ...
     * ..#
     * ...
     * }
     * </pre>
     * @param offsetX Pixels to go right (negative for left)
     * @param offsetY Pixels to go down (negative for up)
     * @return The compiled anchor.
     */
    public static Anchor rightMiddle(int offsetX, int offsetY) {
        return at(1f, 0.5f, offsetX, offsetY);
    }

    /**
     * Constructs a new anchor at the top center point of the screen.
     * <pre>
     * {@code
     * .#.
     * ...
     * ...
     * }
     * </pre>
     * @return The compiled anchor.
     */
    public static Anchor topCenter() {
        return topCenter(0, 0);
    }

    /**
     * Constructs a new anchor at the top center point of the screen,
     * along with a given pixel offset.
     * <pre>
     * {@code
     * .#.
     * ...
     * ...
     * }
     * </pre>
     * @param offsetX Pixels to go right (negative for left)
     * @param offsetY Pixels to go down (negative for up)
     * @return The compiled anchor.
     */
    public static Anchor topCenter(int offsetX, int offsetY) {
        return at(0.5f, 0f, offsetX, offsetY);
    }

    /**
     * Constructs a new anchor at the bottom center point of the screen.
     * <pre>
     * {@code
     * ...
     * ...
     * .#.
     * }
     * </pre>
     * @return The compiled anchor.
     */
    public static Anchor bottomCenter() {
        return bottomCenter(0, 0);
    }

    /**
     * Constructs a new anchor at the bottom center point of the screen,
     * along with a given pixel offset.
     * <pre>
     * {@code
     * ...
     * ...
     * .#.
     * }
     * </pre>
     * @param offsetX Pixels to go right (negative for left)
     * @param offsetY Pixels to go down (negative for up)
     * @return The compiled anchor.
     */
    public static Anchor bottomCenter(int offsetX, int offsetY) {
        return at(0.5f, 1f, offsetX, offsetY);
    }

    /**
     * Constructs a new anchor in the top-left corner of the screen.
     * <pre>
     * {@code
     * #..
     * ...
     * ...
     * }
     * </pre>
     * @return The compiled anchor.
     */
    public static Anchor topLeft() {
        return topLeft(0, 0);
    }

    /**
     * Constructs a new anchor in the top-left corner of the screen,
     * along with a given pixel offset.
     * <pre>
     * {@code
     * #..
     * ...
     * ...
     * }
     * </pre>
     * @param offsetX Pixels to go right (negative for left)
     * @param offsetY Pixels to go down (negative for up)
     * @return The compiled anchor.
     */
    public static Anchor topLeft(int offsetX, int offsetY) {
        return at(0f, 0f, offsetX, offsetY);
    }

    /**
     * Constructs a new anchor in the bottom-left corner of the screen.
     * <pre>
     * {@code
     * ...
     * ...
     * #..
     * }
     * </pre>
     * @return The compiled anchor.
     */
    public static Anchor bottomLeft() {
        return bottomLeft(0, 0);
    }

    /**
     * Constructs a new anchor in the bottom-left corner of the screen,
     * along with a given pixel offset.
     * <pre>
     * {@code
     * ...
     * ...
     * #..
     * }
     * </pre>
     * @param offsetX Pixels to go right (negative for left)
     * @param offsetY Pixels to go down (negative for up)
     * @return The compiled anchor.
     */
    public static Anchor bottomLeft(int offsetX, int offsetY) {
        return at(0f, 1f, offsetX, offsetY);
    }

    /**
     * Constructs a new anchor in the top-right corner of the screen.
     * <pre>
     * {@code
     * #..
     * ...
     * ...
     * }
     * </pre>
     * @return The compiled anchor.
     */
    public static Anchor topRight() {
        return topLeft(0, 0);
    }

    /**
     * Constructs a new anchor in the top-right corner of the screen,
     * along with a given pixel offset.
     * <pre>
     * {@code
     * #..
     * ...
     * ...
     * }
     * </pre>
     * @param offsetX Pixels to go right (negative for left)
     * @param offsetY Pixels to go down (negative for up)
     * @return The compiled anchor.
     */
    public static Anchor topRight(int offsetX, int offsetY) {
        return at(1f, 0f, offsetX, offsetY);
    }

    /**
     * Constructs a new anchor in the bottom-right corner of the screen.
     * <pre>
     * {@code
     * ...
     * ...
     * #..
     * }
     * </pre>
     * @return The compiled anchor.
     */
    public static Anchor bottomRight() {
        return bottomLeft(0, 0);
    }

    /**
     * Constructs a new anchor in the bottom-right corner of the screen,
     * along with a given pixel offset.
     * <pre>
     * {@code
     * ...
     * ...
     * #..
     * }
     * </pre>
     * @param offsetX Pixels to go right (negative for left)
     * @param offsetY Pixels to go down (negative for up)
     * @return The compiled anchor.
     */
    public static Anchor bottomRight(int offsetX, int offsetY) {
        return at(1f, 1f, offsetX, offsetY);
    }

    /**
     * Constructs a new anchor from
     * the provided GLSL statements.
     * <p>
     * <b>WARNING</b>: Invalid GLSL will invalidate the Iodide resource pack!
     * Only use this method if you know what you're doing!
     * @return The compiled anchor.
     */
    public static Anchor custom(String string) {
        return new Anchor(string);
    }

    /**
     * Generates this anchor in the vertex shader GLSL. (inside a switch statement)
     */
    @Override
    public String toString() {
        return compiled;
    }

    /**
     * Applies this anchor to a component.
     * For this to work, the anchor must be registered first!
     */
    public Component use(Component inner) throws UnregisteredAnchorException {
        int i = Iodide.ANCHOR_REGISTRY.indexOf(this);
        char start = (char) ('\uE000' + i * 2);
        char end = (char) (start + 1);

        return Component.text()
                .append(Component.text(start).font(FONT))
                .append(inner)
                .append(Component.text(end).font(FONT))
                .build();
    }
}
