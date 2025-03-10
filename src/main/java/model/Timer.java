package model;

public class Timer {
    private int adjustedTimeLeftInPhase;
    private long internalNowInEpochMs;
    private boolean isInfinite;
    private String phase;
    private int totalTimeInPhase;

    public Timer() {}

    public Timer(int adjustedTimeLeftInPhase, long internalNowInEpochMs, boolean isInfinite, String phase, int totalTimeInPhase) {
        this.adjustedTimeLeftInPhase = adjustedTimeLeftInPhase;
        this.internalNowInEpochMs = internalNowInEpochMs;
        this.isInfinite = isInfinite;
        this.phase = phase;
        this.totalTimeInPhase = totalTimeInPhase;
    }

    public int getAdjustedTimeLeftInPhase() {
        return adjustedTimeLeftInPhase;
    }

    public void setAdjustedTimeLeftInPhase(int adjustedTimeLeftInPhase) {
        this.adjustedTimeLeftInPhase = adjustedTimeLeftInPhase;
    }

    public long getInternalNowInEpochMs() {
        return internalNowInEpochMs;
    }

    public void setInternalNowInEpochMs(long internalNowInEpochMs) {
        this.internalNowInEpochMs = internalNowInEpochMs;
    }

    public boolean isInfinite() {
        return isInfinite;
    }

    public void setInfinite(boolean infinite) {
        isInfinite = infinite;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public int getTotalTimeInPhase() {
        return totalTimeInPhase;
    }

    public void setTotalTimeInPhase(int totalTimeInPhase) {
        this.totalTimeInPhase = totalTimeInPhase;
    }

    @Override
    public String toString() {
        return "{" +
                "adjustedTimeLeftInPhase=" + adjustedTimeLeftInPhase +
                ", internalNowInEpochMs=" + internalNowInEpochMs +
                ", isInfinite=" + isInfinite +
                ", phase='" + phase + '\'' +
                ", totalTimeInPhase=" + totalTimeInPhase +
                '}';
    }
}
