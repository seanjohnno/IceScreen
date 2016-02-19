package timeout.slang.com.icescreen;

/**
 * Interface for subclasses to impl when they're holding a reference to something which would be
 * dangerous as a hard reference (View, Contexts, etc)
 */
public interface SafeRef<T> {
    public T get();
}