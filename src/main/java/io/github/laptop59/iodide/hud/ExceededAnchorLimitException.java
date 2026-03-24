package io.github.laptop59.iodide.hud;

public class ExceededAnchorLimitException extends RuntimeException {
    public ExceededAnchorLimitException(int limit) {
        super("Anchor limit has been exceeded (limit of " + limit + " anchors)");
    }
}
