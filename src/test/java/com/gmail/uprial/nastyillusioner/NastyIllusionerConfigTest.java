package com.gmail.uprial.nastyillusioner;

import com.gmail.uprial.nastyillusioner.config.InvalidConfigException;
import com.gmail.uprial.nastyillusioner.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class NastyIllusionerConfigTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testEmptyDebug() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("Empty 'debug' flag. Use default value false");
        NastyIllusionerConfig.isDebugMode(getPreparedConfig(""), getDebugFearingCustomLogger());
    }

    @Test
    public void testNormalDebug() throws Exception {
        assertTrue(NastyIllusionerConfig.isDebugMode(getPreparedConfig("debug: true"), getDebugFearingCustomLogger()));
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("Empty 'enabled' flag. Use default value true");
        loadConfig(getDebugFearingCustomLogger(), "");
    }

    @Test
    public void testMoveProjectionHistoryLengthBiggerThanMovingHistoryWindow() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Move projection history length of 3 " +
                "is greater than " +
                "moving history window of 2");
        loadConfig( "enabled: true",
                "moving_history_window: 2",
                "run_share_to_trigger: 80",
                "move_projection_history_length: 3");
    }

    @Test
    public void testMoveProjectionMinHistoryDistanceBiggerThanMoveProjectionDistance() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Move projection min history distance of 10.00 " +
                "is greater than " +
                "move projection distance of 9.00");
        loadConfig( "enabled: true",
                "moving_history_window: 30",
                "run_share_to_trigger: 80",
                "move_projection_history_length: 4",
                "move_projection_min_history_distance: 10.0",
                "move_projection_distance: 9.0");
    }

    @Test
    public void testNormalConfig() throws Exception {
        assertEquals(
                "enabled: true, " +
                        "moving_history_window: 30, " +
                        "run_share_to_trigger: 80.00, " +
                        "move_projection_history_length: 4, " +
                        "move_projection_min_history_distance: 0.10, " +
                        "move_projection_distance: 30.00, " +
                        "max_distance_to_existing_illusioner: 50.00, " +
                        "per_second_trigger_probability: 5.00, " +
                        "illusioner_detection_distance: 50.00",
                loadConfig("debug: false",
                        "enabled: true",
                        "moving_history_window: 30",
                        "run_share_to_trigger: 80.0",
                        "move_projection_history_length: 4",
                        "move_projection_min_history_distance: 0.1",
                        "move_projection_distance: 30.0",
                        "max_distance_to_existing_illusioner: 50.0",
                        "per_second_trigger_probability: 5.0",
                        "illusioner_detection_distance: 50.0").toString());
    }
}