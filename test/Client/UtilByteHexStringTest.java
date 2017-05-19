package Client;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class UtilByteHexStringTest {
	
	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testByteHexString() {
		byte[] byteData = { 0, 69, 121, 101, 45, 62, 118, 101, 114, 61, 101, 98 };
		
		String expected = "004579652d3e7665723d6562";
		String actual = Util.byteHexString(byteData);
		
		assertEquals(expected, actual);
	}
	
}
