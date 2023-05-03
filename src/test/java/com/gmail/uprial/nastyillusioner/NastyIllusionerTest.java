package com.gmail.uprial.nastyillusioner;

import com.gmail.uprial.nastyillusioner.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NastyIllusionerTest extends TestConfigBase {
    @Test
    public void testLoadException() throws Exception {
        NastyIllusioner.loadConfig(getPreparedConfig(""), getCustomLogger());
    }
}