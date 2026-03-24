package io.github.laptop59.iodide.hud;

public class UnregisteredAnchorException extends RuntimeException {
    public UnregisteredAnchorException() {
        super("Attempted to fetch ID of an unregistered anchor.");
    }
}
