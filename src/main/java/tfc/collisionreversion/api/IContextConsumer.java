package tfc.collisionreversion.api;

@FunctionalInterface
public interface IContextConsumer<T> {
	void accept(T context);
}
