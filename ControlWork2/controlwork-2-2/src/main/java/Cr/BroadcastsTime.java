package Cr;

public class BroadcastsTime implements Comparable<BroadcastsTime> {
    private final byte hour;
    private final byte minutes;
    public byte getHour() {
        return hour;
    }

    public byte getMinutes() {
        return minutes;
    }

    public boolean after(BroadcastsTime t) {
        return this.compareTo(t) > 0;
    }

    public boolean before(BroadcastsTime t) {
        return this.compareTo(t) < 0;
    }

    public boolean between(BroadcastsTime t1, BroadcastsTime t2) {
        return this.compareTo(t1) >= 0 && this.compareTo(t2) <= 0;
    }

    @Override
    public int compareTo(BroadcastsTime t) {
        int hourDiff = this.hour - t.hour;
        if (hourDiff != 0) {
            return hourDiff;
        }
        return this.minutes - t.minutes;
    }

    public BroadcastsTime(String time) {
        String[] parts = time.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid time format, expected HH:mm: " + time);
        }
        this.hour = Byte.parseByte(parts[0]);
        this.minutes = Byte.parseByte(parts[1]);
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d", hour, minutes);
    }
}
