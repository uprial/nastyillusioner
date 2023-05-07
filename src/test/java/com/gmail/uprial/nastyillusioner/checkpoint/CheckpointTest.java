package com.gmail.uprial.nastyillusioner.checkpoint;

import com.gmail.uprial.nastyillusioner.helpers.TestConfigBase;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckpointTest extends TestConfigBase {

    // ==== equals ====
    @Test
    public void testEquals() throws Exception {
        assertEquals(new Checkpoint(1.0, 1.0, 1.1),
                new Checkpoint(1.0, 1.0, 1.1));
        assertNotEquals(new Checkpoint(1.0, 1.0, 1.1),
                new Checkpoint(1.0, 1.0, 1.2));
    }

    @Test
    public void testEqualsCloseToEpsilon() throws Exception {
        assertEquals(new Checkpoint(1.0, 1.0, 1.1),
                new Checkpoint(1.0, 1.0, 1.1 + Checkpoint.EPSILON / 2));

        assertNotEquals(new Checkpoint(1.0, 1.0, 1.1),
                new Checkpoint(1.0, 1.0, 1.1 + Checkpoint.EPSILON * 2));
    }

    // ==== toString ====
    @Test
    public void testToString() throws Exception {
        assertEquals("(1.00, 1.00, 1.10)", new Checkpoint(1.0, 1.0, 1.1).toString());
        assertEquals("(1.00, 1.00, 1.10)", new Checkpoint(1.002, 1.0, 1.1).toString());
    }

    // ==== getSubtract ====
    @Test
    public void testGetSubtract() throws Exception {
        assertEquals(new Checkpoint(2.0, 2.0, 2.0),
                new Checkpoint(3.0, 3.0, 3.0)
                        .getSubtract(new Checkpoint(1.0, 1.0, 1.0)));
        assertEquals(new Checkpoint(-1.3, -1.4, -1.5),
                new Checkpoint(1.1, 1.2, 1.3)
                        .getSubtract(new Checkpoint(2.4, 2.6, 2.8)));
    }
}