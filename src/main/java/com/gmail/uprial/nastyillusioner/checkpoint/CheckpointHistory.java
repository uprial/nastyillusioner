package com.gmail.uprial.nastyillusioner.checkpoint;

import java.util.HashMap;


public class CheckpointHistory {

    private static final class TimerWheel extends HashMap<Integer, Checkpoint> {
    }

    private final TimerWheel timerWheel = new TimerWheel();
    private int currentIndex = -1;
    private int historyLength = 0;

    private final int maxHistoryLength;

    public CheckpointHistory(final int maxHistoryLength) {
        this.maxHistoryLength = maxHistoryLength;
    }

    public void add(Checkpoint checkpoint) {
        if(historyLength < maxHistoryLength) {
            historyLength++;
        }
        currentIndex = getNextIndex(currentIndex);
        timerWheel.put(currentIndex, checkpoint);
    }

    public Double getGroundDistance() {
        if(historyLength > 0) {
            final Checkpoint firstCheckpoint = timerWheel.get(currentIndex);
            // the next index of wheel is the last by time
            final Checkpoint lastCheckpoint = timerWheel.get(getNextIndex(currentIndex));

            return getGroundLength(firstCheckpoint.getSubtract(lastCheckpoint));
        } else {
            return 0.0;
        }
    }

    public Checkpoint getGroundProjectionCheckpoint(int historySearchDepth,
                                                    final double projectionDistance) {

        final Checkpoint lastCheckpoint = timerWheel.get(currentIndex);

        if (historyLength < 2) {
            return lastCheckpoint;
        }

        if (historySearchDepth > historyLength - 1) {
            historySearchDepth = historyLength - 1;
        }
        final Checkpoint firstCheckpoint = timerWheel.get(getPrevIndex(currentIndex, historySearchDepth));

        if(lastCheckpoint.equals(firstCheckpoint)) {
            return null;
        }
        final Checkpoint direction = lastCheckpoint.getSubtract(firstCheckpoint);
        final Double multiplier = projectionDistance / getGroundLength(direction);

        return new Checkpoint(
                 lastCheckpoint.getX() + direction.getX() * multiplier,
                lastCheckpoint.getY(),
                lastCheckpoint.getZ() + direction.getZ() * multiplier
        );
    }

    public static Double getGroundLength(final Checkpoint checkpoint) {
        return Math.sqrt(
                Math.pow(checkpoint.getX(), 2)
                +
                Math.pow(checkpoint.getZ(), 2)
        );
    }

    public void clear() {
        timerWheel.clear();
        currentIndex = -1;
        historyLength = 0;
    }

    private int getNextIndex(int index) {
        index ++;
        if(index >= historyLength) {
            index = 0;
        }

        return index;
    }

    private int getPrevIndex(int index, final int decrement) {
        index -= decrement;
        if(index < 0) {
            index += historyLength;
        }

        return index;
    }

    @Override
    public String toString() {
        return String.format("[%s, %d/%d/%d]",
                timerWheel,
                currentIndex,
                historyLength,
                maxHistoryLength);
    }
}
