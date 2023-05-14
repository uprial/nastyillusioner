package com.gmail.uprial.nastyillusioner;

import com.gmail.uprial.nastyillusioner.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NastyIllusionerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testLoadException() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("[ERROR] Empty moving history window");
        NastyIllusioner.loadConfig(getPreparedConfig(""), getCustomLogger());
    }
}