package net.mcaefcompat;

public final class McaEfCompatState {

    private McaEfCompatState() {}

    public static final ThreadLocal<Boolean> EF_RENDERING =
            ThreadLocal.withInitial(() -> false);

    public static final ThreadLocal<Boolean> SCALE_PUSHED =
            ThreadLocal.withInitial(() -> false);

    public static boolean isEfRendering() {
        return Boolean.TRUE.equals(EF_RENDERING.get());
    }
}
