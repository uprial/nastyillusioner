package com.gmail.uprial.nastyillusioner.checkpoint;

public class Checkpoint {
    static final double EPSILON = 1.0E-8D;

    private final double x;
    private final double y;
    private final double z;

    public Checkpoint(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getZ() {
        return z;
    }

    public Checkpoint getSubtract(final Checkpoint checkpoint) {
        return new Checkpoint(
                x - checkpoint.x,
                y - checkpoint.y,
                z - checkpoint.z
        );
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x * 1E+6 + y * 1E+3 + z);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        final Checkpoint checkpoint = (Checkpoint)o;
        return (Math.abs(x - checkpoint.x) < EPSILON)
                && (Math.abs(y - checkpoint.y) < EPSILON)
                && (Math.abs(z - checkpoint.z) < EPSILON);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}