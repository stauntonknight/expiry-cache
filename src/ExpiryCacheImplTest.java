import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class ExpiryCacheImplTest extends TestCase {
	private ExpiryCacheImpl<String, Integer> cache;

	public void setUp() {
		cache = new ExpiryCacheImpl<String, Integer>();
	}

	public void tearDown() {
		cache = null;
	}

	public void testSimple() throws InterruptedException {
		cache.put("test", 100, 2, TimeUnit.MILLISECONDS);
		Thread.sleep(1);
		assertEquals(100, cache.get("test").intValue());
		Thread.sleep(2);
		assertEquals(null, cache.get("test"));
	}
}
