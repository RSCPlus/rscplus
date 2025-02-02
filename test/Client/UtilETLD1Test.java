package Client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Tests for {@link Util#getETLD1(String)}
 */
public class UtilETLD1Test {

    /**
     * Reset the static cache between each test
     */
    @Before
    public void setup() throws Exception {
        Field field = Util.class.getDeclaredField("publicSuffixList");
        field.setAccessible(true);
        field.set(null, null);
    }

    /**
     * Tests basic eTLD+1 calculations
     */
    @Test
    public void testBasicCases() {
        Util.initPublicSuffixList();

        assertNull(Util.getETLD1(null));
        assertNull(Util.getETLD1(""));
        assertNull(Util.getETLD1("2001:0000:130F:0000:0000:09C0:876A:130B"));
        assertNull(Util.getETLD1("127.0.0.1"));
        assertNull(Util.getETLD1("localhost"));

        assertNull(Util.getETLD1("com"));

        assertEquals("rsc.plus", Util.getETLD1("rsc.plus"));
        assertEquals("rsc.plus", Util.getETLD1("other.rsc.plus"));
        assertEquals("rsc.plus", Util.getETLD1("really.another.rsc.plus"));

        assertEquals("runescape.com", Util.getETLD1("classic1.runescape.com"));
        assertEquals("runescape.com", Util.getETLD1("classic2.runescape.com"));
        assertEquals("runescape.com", Util.getETLD1("classic3.runescape.com"));
    }

    /**
     * Tests eTLD+1 edge case examples from the official docs
     *
     * @see <a href="https://github.com/publicsuffix/list/wiki/Format#example">Documentation examples</a>
     */
    @Test
    public void testDocExamples() {
        Util.initPublicSuffixList("/test/resources/psl-doc-cases.dat.gz");

        assertEquals("foo.com", Util.getETLD1("foo.com"));
        assertNull(Util.getETLD1("bar.foo.com"));
        assertEquals("example.bar.foo.com", Util.getETLD1("example.bar.foo.com"));
        assertEquals("foo.bar.jp", Util.getETLD1("foo.bar.jp"));
        assertNull(Util.getETLD1("bar.jp"));
        assertEquals("foo.bar.hokkaido.jp", Util.getETLD1("foo.bar.hokkaido.jp"));
        assertNull(Util.getETLD1("bar.hokkaido.jp"));
        assertEquals("foo.bar.tokyo.jp", Util.getETLD1("foo.bar.tokyo.jp"));
        assertNull(Util.getETLD1("bar.tokyo.jp"));
        assertEquals("pref.hokkaido.jp", Util.getETLD1("pref.hokkaido.jp"));
        assertEquals("metro.tokyo.jp", Util.getETLD1("metro.tokyo.jp"));
    }

    /**
     * Tests eTLD+1 exception rules when no base domain is defined
     */
    @Test
    public void testNoBaseDomain() {
        Util.initPublicSuffixList("/test/resources/psl-no-base-domain.dat");

        assertEquals("foo.bar.kawasaki.jp", Util.getETLD1("foo.bar.kawasaki.jp"));
        assertEquals("city.kawasaki.jp", Util.getETLD1("city.kawasaki.jp"));
        assertEquals("kawasaki.jp", Util.getETLD1("kawasaki.jp"));
        assertNull(Util.getETLD1("test.kawasaki.jp"));
    }
}
