import java.util.ArrayList;
import java.util.Objects;

public class LyricSegment implements Comparable<LyricSegment> {
    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    private String lyric;

    public ArrayList<int[]> getPcmData() {
        return pcmData;
    }

    public void setPcmData(ArrayList<int[]> pcmData) {
        this.pcmData = pcmData;
    }

    private ArrayList<int[]> pcmData;

    public LyricSegment(String lyric, ArrayList<int[]> pcmData) {
        this.lyric = lyric;
        this.pcmData = pcmData;
    }

    public LyricSegment mergeLyricSegments(LyricSegment otherLyricSegment) {
        if (!lyric.equals(otherLyricSegment.getLyric())) {
            throw new IllegalArgumentException("Only merge if the lyrics match!");
        }
        pcmData.addAll(otherLyricSegment.getPcmData());
        return this;
    }

    @Override
    public String toString() {
        return "LyricSegment{" +
                "lyric='" + lyric + '\'' +
                ", pcmData=" + pcmData +
                '}';
    }

    @Override
    public int compareTo(LyricSegment o) {
        return lyric.compareTo(o.getLyric());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LyricSegment that = (LyricSegment) o;
        return lyric.equals(that.lyric); //&& Objects.equals(pcmData, that.pcmData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lyric, pcmData);
    }
}
