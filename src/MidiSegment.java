import java.util.ArrayList;
import java.util.Objects;

public class MidiSegment implements Comparable<MidiSegment> {
    long duration;
    ArrayList<int[]> data;
    String notes;
    int channel;
    int perfSplit;
    public int index; // TODO
    public int lengthSplit;
    //slice info contained in notes


    public MidiSegment(long duration, String notes, ArrayList<int[]> data, int channel) {
        this.duration = duration;
        this.notes = notes;
        this.data = data;
        this.channel = channel;
    }

    public MidiSegment(long duration, String notes, ArrayList<int[]> data, int channel, int index) {
        this.duration = duration;
        this.notes = notes;
        this.data = data;
        this.channel = channel;
        this.index = index;
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
        return Objects.equals(notes, that.notes) && Objects.equals(index, that.index) && Objects.equals(channel, that.channel) && Objects.equals(lengthSplit, that.lengthSplit);
    }

    public MidiSegment mergeMidiSegments(MidiSegment otherMidiSegment) {
        if (!notes.equals(otherMidiSegment.notes)) {
            throw new IllegalArgumentException("Only merge if the data matches!");
        }
        data.addAll(otherMidiSegment.data);
       // places.addAll(otherMidiSegment.places);

        return this;
    }
}


