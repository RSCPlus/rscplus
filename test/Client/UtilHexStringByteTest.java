package Client;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class UtilHexStringByteTest {
	
	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testHexStringByte() {
		String hexString = "004579652d3e7665723d6562";
		
		byte[] expected = { 0, 69, 121, 101, 45, 62, 118, 101, 114, 61, 101, 98 };
		byte[] actual = Util.hexStringByte(hexString);
		
		assertTrue(Arrays.equals(expected, actual));
	}
	
}
