package io.github.laptop59.iodide.text;

import io.github.laptop59.iodide.Iodide;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

/** Contains helpers when dealing with Iodide's text. */
public class IodideText {
    /** Font used in advancing. */
    public static final Key ADVANCE = Key.key(Iodide.NAMESPACE, "advance");

    /**
     * Gets a text component that advances by the given number of pixels.
     *
     * @param advance The number of pixels advanced to the right, negative to go left instead.
     * @return A text component that does the required action.
     */
    public static Component getAdvanceComponent(int advance) {
        if (advance == 0)
            return Component.empty();
        return Component.text(getAdvanceString(advance)).font(ADVANCE);
    }

    /**
     * Gets text that advances by the given number of pixels when combined with font {@link IodideText#ADVANCE}.
     *
     * @param advance The number of pixels advanced to the right, negative to go left instead.
     * @return A string that does the required action.
     */
    public static String getAdvanceString(int advance) {
        if (advance == 0) return "";

        // Maximum of 4 different characters used.
        char[] result = new char[4];
        int i = 0, negativeFlag = 0, b;

        // EXCEPTION:
        //  advance = -2147483648
        // IT IS THEN NEGATED, YIELDING
        //  advance =  2147483648
        // But this doesn't fit into an `int`, so we get
        //  advance = -2147483648
        // We need not use `>>>` in the 4th byte due to the `& 0xFF`.
        if (advance < 0) {
            advance = -advance;
            negativeFlag = 1;
        }

        // Extract the first byte:
        // \uE000: 1
        // \uE001: -1
        // \uE002: 2
        // \uE003: -2
        // \uE004: 3
        // \uE005: -3
        // ...
        // \uE1FC: 255
        // \uE1FD: -255
        b = advance & 0xFF;
        if (b != 0)
            result[i++] = (char) (0xE000 | ((b - 1) << 1 | negativeFlag));

        // Extract the second byte:
        // \uE200: 256
        // \uE201: -256
        // \uE202: 512
        // \uE203: -512
        // \uE204: 768
        // \uE205: -768
        // ...
        // \uE3FC: 65280
        // \uE3FD: -65280
        b = (advance >> 8) & 0xFF;
        if (b != 0)
            result[i++] = (char) (0xE200 | ((b - 1) << 1 | negativeFlag));

        // Third byte:
        b = (advance >> 16) & 0xFF;
        if (b != 0)
            result[i++] = (char) (0xE400 | ((b - 1) << 1 | negativeFlag));

        // Fourth byte:
        b = (advance >> 24) & 0xFF;
        if (b != 0)
            result[i++] = (char) (0xE600 | ((b - 1) << 1 | negativeFlag));

        return new String(result, 0, i);
    }
}
