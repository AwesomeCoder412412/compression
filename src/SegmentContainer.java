import java.util.ArrayList;
import java.util.Objects;

public class SegmentContainer implements Comparable<SegmentContainer> {
    public ArrayList<MidiSegment> segments;
    String notes;
    final int channel;

    public int getPerfSplit() {
        return perfSplit;
    }

    public void setPerfSplit(int perfSplit) {
        this.perfSplit = perfSplit;
        for (int i = 0; i < segments.size(); i++) {
            segments.get(i).perfSplit = perfSplit;
        }
    }

    private int perfSplit;

    public SegmentContainer(MidiSegment segment)  {
        segments = new ArrayList<>();
        notes = segment.notes;
        channel = segment.channel;
        setPerfSplit(segment.perfSplit);
        segment.index = 0;
    }

    public SegmentContainer(ArrayList<MidiSegment> segments, int perfSplit)  {
        this.segments = segments;
        notes = segments.getFirst().notes;
        channel = segments.getFirst().channel;
        setPerfSplit(perfSplit);
    }

    public SegmentContainer[] split() {
        SegmentContainer[] splits = new SegmentContainer[2];

        return splits;
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
    }


    public int size() {
        return segments.size();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentContainer that = (SegmentContainer) o;
        return Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segments, notes);
    }

    @Override
    public int compareTo(SegmentContainer o){
        try {
            throw new Exception("this should never ever ever be used, here in case i need comparable");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //return notes.compareTo(o.notes);
    }


}
