package com.gmail.uprial.nastyillusioner.config;

import com.gmail.uprial.nastyillusioner.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.nastyillusioner.config.ConfigReaderNumbers.*;
import static org.junit.Assert.*;

@SuppressWarnings("ClassWithTooManyMethods")
public class ConfigReaderNumbersTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    // ==== getInt ====
    @Test
    public void testEmptyInt() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty value number");
        getInt(getPreparedConfig(""), "n", "value number", 0, 100);
    }

    @Test
    public void testWrongInt() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A value number is not an integer");
        getInt(getPreparedConfig("n: 1.0"), "n", "value number", 0, 100);
    }

    @Test
    public void testSmallInt() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A value number should be at least 0");
        getInt(getPreparedConfig("n: -1"), "n", "value number", 0, 100);
    }

    @Test
    public void testBigInt() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A value number should be at most 100");
        getInt(getPreparedConfig("n: 1000"), "n", "value number", 0, 100);
    }

    @Test
    public void testNormalInt() throws Exception {
        assertEquals(50, getInt(getPreparedConfig("n: 50"), "n", "value number", 0, 100));
    }

    @Test
    public void testIntMinMaxConflict() throws Exception {
        e.expect(InternalConfigurationError.class);
        e.expectMessage("Max value of value number is greater than max value");
        getInt(getPreparedConfig(""), "n", "value number", 200, 100);
    }
}