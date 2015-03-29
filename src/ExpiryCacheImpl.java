import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// We try to minimize the chances of locking the complete store here.
// We also ensure that gets are always consistent with the current state.

// TIME COMPLEXITY ANALYSIS:
// ALL READS ARE AMORTIZED O(1)
// ALL WRITES ARE AMORTIZED O(1)
// TIME STARTED : 09:25 pm
// TIME ENDED: 10:35 pm

// INTERNALS:
// {@code NULL_READ_LOCK} = READ LOCK for null values.
// {@code NULL_READ_LOCK} + lock store = WRITE LOCK for null values.
public class ExpiryCacheImpl<K, V> implements ExpiryCache<K, V> {

	private final HashMap<K, ValueSpec<V>> store;
	private final String NULL_READ_LOCK = "r";

	public <K, V> ExpiryCacheImpl() {
		// I use hash-map because I am not well aware of time complexities of
		// {@code ConcurrentHashMap}.
		store = new HashMap();
	}

	@Override
	public void put(K key, V value, int ttl, TimeUnit timeUnit) {
		ValueSpec<V> vs = new ValueSpec<V>(value, timeUnit.toMillis(ttl)
				+ System.currentTimeMillis());
		ValueSpec<V> existing = store.get(key);
		if (existing == null) {
			// There is no existing value hence lock the whole store.
			synchronized (store) {
				// Also ensure that no reads can happen at this time.
				synchronized (NULL_READ_LOCK) {
					// Synchronize over the whole store and double-check if the
					// existing value is null.
					existing = store.get(key);
					if (existing == null) {
						store.put(key, vs);
						return;
					}
				}
			}
		}
		// There is some existing value and we don't need to lock the whole
		// store for this operation. Just locking the existing value is good enough for us.
		synchronized (existing) {
			store.put(key, vs);
		}
	}

	@Override
	public V get(K key) {
		ValueSpec<V> valueSpec = store.get(key);
		if (valueSpec == null) {
			// In case the value is null, we have to lock the whole store to
			// ensure that no puts happen.
			synchronized (NULL_READ_LOCK) {
				valueSpec = store.get(key);
				if (valueSpec == null) {
					return null;
				}
			}
		}
		// This should be read-lock too.
		synchronized (valueSpec) {
			if (valueSpec.hasExpired()) {
				return null;
			}
			return valueSpec.getValue();
		}
	}
}
