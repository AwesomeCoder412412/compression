import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a container for managing a collection of {@code MidiSegment} objects.
 * The class provides functionality to group, process, and modify the properties of
 * MIDI segments collectively. It can be initialized with a single {@code MidiSegment},
 * or a collection.
 */
public class SegmentContainer implements Comparable<SegmentContainer> {
    public ArrayList<MidiSegment> segments;
    String notes;
    int channel;

    private int lengthSplit;
    private int perfSplit;

    public int getPerfSplit() {
        return perfSplit;
    }

    public void setPerfSplit(int perfSplit) {
        this.perfSplit = perfSplit;
        for (int i = 0; i < segments.size(); i++) {
            segments.get(i).perfSplit = perfSplit;
        }
    }

    public int getLengthSplit() {
        return lengthSplit;
    }


    public void setLengthSplit(int lengthSplit) {
        this.lengthSplit = lengthSplit;
        for (int i = 0; i < segments.size(); i++) {
            segments.get(i).lengthSplit = lengthSplit;
        }
    }

    public SegmentContainer(MidiSegment segment) {
        segments = new ArrayList<>();
        segments.add(segment);
        notes = segment.notes;
        channel = segment.channel;
        setPerfSplit(segment.perfSplit);
        segment.index = 0;
        lengthSplit = segment.lengthSplit;
    }

    public SegmentContainer(MidiSegment segment, int empty) {
        segments = new ArrayList<>();
        //segments.add(segment);
        notes = segment.notes;
        channel = segment.channel;
        setPerfSplit(segment.perfSplit);
        segment.index = 0;
        lengthSplit = segment.lengthSplit;
    }

    public SegmentContainer(ArrayList<MidiSegment> segments, int perfSplit) {
        this.segments = segments;
        for (int i = 0; i < segments.size(); i++) {
            segments.get(i).index = i;
        }
        notes = segments.getFirst().notes;
        channel = segments.getFirst().channel;
        setPerfSplit(perfSplit);
    }

    public ArrayList<int[]> rawData() {
        ArrayList<int[]> data = new ArrayList<>();
        for (MidiSegment seg : segments) {
            data.addAll(seg.data);
        }
        return data;
    }

    public void addMidiSegment(MidiSegment segment) {
        segments.add(segment);
        segment.index = segments.size() - 1;
        segment.lengthSplit = lengthSplit;
    }


    public int size() {
        return segments.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentContainer that = (SegmentContainer) o;
        return Objects.equals(notes, that.notes) && Objects.equals(perfSplit, that.perfSplit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segments, notes);
    }

    @Override
    public int compareTo(SegmentContainer o) {
        try {
            throw new Exception("this should never ever ever be used, here in case I need comparable");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
