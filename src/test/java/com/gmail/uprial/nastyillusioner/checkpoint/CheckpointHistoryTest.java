package com.gmail.uprial.nastyillusioner.checkpoint;

import com.gmail.uprial.nastyillusioner.helpers.TestConfigBase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CheckpointHistoryTest extends TestConfigBase {
    private static final int MAX_HISTORY_LENGTH = 4;

    private CheckpointHistory history = null;

    @Before
    public void setUp() throws Exception {
        history = new CheckpointHistory(MAX_HISTORY_LENGTH);
    }

    // ==== getGroundDistance ====

    @Test
    public void testGetGroundDistance() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 1.0, 2.0));
        assertEquals(1.0, history.getGroundDistance(), 0.01);
    }

    @Test
    public void testGetGroundDistance_Empty() {
        assertEquals(0.0, history.getGroundDistance(), 0.01);
    }

    @Test
    public void testGetGroundDistance_OneCheckpoint() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        assertEquals(0.0, history.getGroundDistance(), 0.01);
    }
    @Test
    public void testGetGroundDistance_SeveralCheckpoints() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 1.0, 2.0));
        history.add(new Checkpoint(1.0, 1.0, 3.0));
        assertEquals(2.0, history.getGroundDistance(), 0.01);
    }

    @Test
    public void testGetGroundDistance_HistoryOverwhelming() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 1.0, 2.0));
        history.add(new Checkpoint(1.0, 1.0, 3.0));
        history.add(new Checkpoint(1.0, 1.0, 4.0));
        history.add(new Checkpoint(1.0, 1.0, 5.0));
        assertEquals(3.0, history.getGroundDistance(), 0.01);
    }

    @Test
    public void testGetGroundDistance_VerticalsDoNotMatter() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 11.0, 2.0));
        assertEquals(1.0, history.getGroundDistance(), 0.01);
    }

    @Test
    public void testGetGroundDistance_TwoHorizontalChanges() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(2.0, 1.0, 2.0));
        assertEquals(1.41, history.getGroundDistance(), 0.01);
    }

    // ==== getGroundProjectionCheckpoint ====

    @Test
    public void testGetGroundProjectionCheckpoint() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 1.0, 2.0));
        assertEquals(new Checkpoint(1.0, 1.0, 12.0),
                history.getGroundProjectionCheckpoint(2, 10.0));
    }

    @Test
    public void testGetGroundProjectionCheckpoint_Empty() {
        assertNull(history.getGroundProjectionCheckpoint(2, 10.0));
    }

    @Test
    public void testGetGroundProjectionCheckpoint_OneCheckpoint() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        assertEquals(new Checkpoint(1.0, 1.0, 1.0), history.getGroundProjectionCheckpoint(2, 10.0));
    }

    @Test
    public void testGetGroundProjectionCheckpoint_UseLastVertical() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 11.0, 2.0));
        assertEquals(new Checkpoint(1.0, 11.0, 12.0),
                history.getGroundProjectionCheckpoint(2, 10.0));
    }

    @Test
    public void testGetGroundProjectionCheckpoint_SeveralCheckpoints() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 1.0, 2.0));
        history.add(new Checkpoint(1.0, 1.0, 3.0));
        assertEquals(new Checkpoint(1.0, 1.0, 13.0),
                history.getGroundProjectionCheckpoint(2, 10.0));
    }

    @Test
    public void testGetGroundProjectionCheckpoint_HistoryOverwhelming() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 1.0, 2.0));
        history.add(new Checkpoint(1.0, 1.0, 3.0));
        history.add(new Checkpoint(1.0, 1.0, 4.0));
        history.add(new Checkpoint(1.0, 1.0, 5.0));
        assertEquals(new Checkpoint(1.0, 1.0, 15.0),
                history.getGroundProjectionCheckpoint(2, 10.0));
    }

    @Test
    public void testGetGroundProjectionCheckpoint_TwoHorizontalChanges() {
        final double delta = Math.sqrt(Math.pow(10.0, 2) / 2);

        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(2.0, 1.0, 2.0));
        assertEquals( 7.07, delta,0.01);
        assertEquals(new Checkpoint(2.0 + delta, 1.0, 2.0 + delta),
                history.getGroundProjectionCheckpoint(2, 10.0));
    }

    @Test
    public void testGetGroundProjectionCheckpoint_SearchLimit() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 1.0, 2.0));
        history.add(new Checkpoint(1.0, 1.0, 3.0));
        history.add(new Checkpoint(2.0, 1.0, 3.0));
        history.add(new Checkpoint(3.0, 1.0, 3.0));
        assertEquals(new Checkpoint(13.0, 1.0, 3.0),
                history.getGroundProjectionCheckpoint(2, 10.0));
    }

    @Test
    public void testGetGroundProjectionCheckpoint_SearchOverwhelming() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 1.0, 2.0));
        history.add(new Checkpoint(1.0, 1.0, 3.0));
        history.add(new Checkpoint(1.0, 1.0, 4.0));
        history.add(new Checkpoint(1.0, 1.0, 5.0));
        assertEquals(new Checkpoint(1.0, 1.0, 15.0),
                history.getGroundProjectionCheckpoint(99, 10.0));
    }

    @Test
    public void testGetGroundProjectionCheckpoint_NoMove() {
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        history.add(new Checkpoint(1.0, 1.0, 1.0));
        assertNull(history.getGroundProjectionCheckpoint(99, 10.0));
    }
}