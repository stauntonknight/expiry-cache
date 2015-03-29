
class ValueSpec<T> {
	private final T value;
	private final long ttlMs;

	public ValueSpec(T value, long ttlMs) {
		this.value = value;
		this.ttlMs = ttlMs;
	}

	boolean hasExpired() {
		return ttlMs < System.currentTimeMillis();
	}

	public T getValue() {
		return value;
	}
}