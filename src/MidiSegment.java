import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a segment of a MIDI file paired with the corresponding WAV channel data, containing information about its duration,
 * notes, MIDI data, channel, etc.
 */
public class MidiSegment implements Comparable<MidiSegment> {
    long duration;
    ArrayList<int[]> data;
    String notes;
    int channel;
    int perfSplit;
    public int index;
    public int lengthSplit;
    public StupidInteger segmentIndex; //for internal use only
    //slice info contained in notes

    public MidiSegment(long duration, String notes, ArrayList<int[]> data, int channel) {
        this.duration = duration;
        this.notes = notes;
        this.data = data;
        this.channel = channel;
        segmentIndex = new StupidInteger(0);
    }

    public MidiSegment(long duration, String notes, ArrayList<int[]> data, int channel, int perfSplit, boolean weird) {
        this.duration = duration;
        this.notes = notes;
        this.data = data;
        this.channel = channel;
        this.perfSplit = perfSplit;
        segmentIndex = new StupidInteger(0);
    }

    public MidiSegment(long duration, String notes, ArrayList<int[]> data, int channel, int index) {
        this.duration = duration;
        this.notes = notes;
        this.data = data;
        this.channel = channel;
        this.index = index;
        segmentIndex = new StupidInteger(0);
    }

    public MidiSegment(long duration, String notes, ArrayList<int[]> data, int channel, int lengthSplit, int perfSplit, int index) {
        this.duration = duration;
        this.notes = notes;
        this.data = data;
        this.channel = channel;
        this.lengthSplit = lengthSplit;
        this.perfSplit = perfSplit;
        this.index = index;
        segmentIndex = new StupidInteger(0);
    }

    @Override
    public int compareTo(MidiSegment o) {
        return notes.compareTo(o.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration, notes, data);
    }

    @Override
    public String toString() {
        return "MidiSegment{" +
                "duration=" + duration +
                ", data=" + data +
                ", notes='" + notes + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MidiSegment that = (MidiSegment) o;
        // return duration == that.duration && Objects.deepEquals(data, that.data) && Objects.equals(notes, that.notes);
        return Objects.equals(notes, that.notes) && Objects.equals(channel, that.channel) && Objects.equals(lengthSplit, that.lengthSplit) && Objects.equals(perfSplit, that.perfSplit) && Objects.equals(segmentIndex, that.segmentIndex);
    }
}


