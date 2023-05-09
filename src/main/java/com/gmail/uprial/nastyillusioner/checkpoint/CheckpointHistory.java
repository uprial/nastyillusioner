package com.gmail.uprial.nastyillusioner.checkpoint;

import java.util.HashMap;


public class CheckpointHistory {

    private static final class TimerWheel extends HashMap<Integer, Checkpoint> {
    }

    private final TimerWheel timerWheel = new TimerWheel();
    private int currentIndex = -1;

    private final int maxHistoryLength;

    public CheckpointHistory(final int maxHistoryLength) {
        this.maxHistoryLength = maxHistoryLength;
    }

    public void add(Checkpoint checkpoint) {
        currentIndex = getNextIndex(currentIndex);
        timerWheel.put(currentIndex, checkpoint);
    }

    public Double getGroundDistance() {
        int index = currentIndex;
        // maxHistoryLength - 1 because should stop before currentIndex
        for(int i = 0; i < maxHistoryLength - 1; i++) {
            index = getNextIndex(index);

            if(timerWheel.containsKey(index)) {
                final Checkpoint firstCheckpoint = timerWheel.get(index);
                final Checkpoint lastCheckpoint = timerWheel.get(currentIndex);
                return getGroundLength(firstCheckpoint.getSubtract(lastCheckpoint));
            }
        }

        return 0.0;
    }

    public Checkpoint getGroundProjectionCheckpoint(int historySearchDepth,
                                                    final double projectionDistance) {

        final Checkpoint lastCheckpoint = timerWheel.get(currentIndex);

        if(lastCheckpoint == null) {
            return null;
        }

        Checkpoint firstCheckpoint = null;
        {
            if (historySearchDepth > maxHistoryLength - 1) {
                historySearchDepth = maxHistoryLength - 1;
            }

            int index = currentIndex;
            for (int i = 0; i < historySearchDepth; i++) {
                index = getPrevIndex(index);
                if (timerWheel.containsKey(index)) {
                    firstCheckpoint = timerWheel.get(index);
                }
            }
            if(firstCheckpoint == null) {
                return lastCheckpoint;
            }
        }

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
    }

    private int getNextIndex(int index) {
        index ++;
        if(index >= maxHistoryLength) {
            index = 0;
        }

        return index;
    }

    private int getPrevIndex(int index) {
        index --;
        if(index < 0) {
            index = maxHistoryLength - 1;
        }

        return index;
    }

    @Override
    public String toString() {
        return String.format("[%s, %d/%d]",
                timerWheel,
                currentIndex,
                maxHistoryLength);
    }
}
