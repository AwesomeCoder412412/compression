import XZ.LZMA2Options;
import XZ.XZOutputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws IOException {



        String name = "smile";
        String currMidi = name + ".mid";
        String currWav =  name + ".wav";

        try {
            boolean encode = true;

            if (encode) {
                Encoder("/Users/jacksegil/Desktop/compression/testfiles/" + currWav, "/Users/jacksegil/Desktop/compression/testfiles/" + currMidi, name, false);

            } else {

                Decoder decoder = new Decoder("/Users/jacksegil/Desktop/compression/testfiles/" + name, name);
                decoder.Decode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        int currValue = ThreadManager.threadCounter.get();
        while (currValue > 0) {
            int temp = ThreadManager.threadCounter.get();
            if (temp != currValue) {
                System.out.println("We have " + currValue + " threads left");
            }
            currValue = temp;
        }

    }


    public static void XZTesting(String[] args) {
        try {


            File fileData = new File(args[0]);
            FileInputStream inFile = new FileInputStream(args[0]);
            FileOutputStream outfile = new FileOutputStream(args[0] + ".xz");

            LZMA2Options options = new LZMA2Options();


            //options.setPreset(9); // play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)
            //options.setDepthLimit((int) (fileData.length() / 2));
            //System.out.println((int) (fileData.length() / 2));
            options.setDepthLimit(Integer.MAX_VALUE);
            options.setNiceLen(273);
            options.setDictSize(805306368);
            //options.setNiceLen(8);

            XZOutputStream out = new XZOutputStream(outfile, options);


            boolean sizeCap = false;
            if (sizeCap) {

                int bufferSize = 8192;
                byte[] buf = new byte[bufferSize];
                int size;
                int buffersRead = 0;
                while ((size = inFile.read(buf)) != -1) {
                    out.write(buf, 0, size);
                    buffersRead++;
                    System.out.println("Currently at " + 100 * ((double) (buffersRead * bufferSize) / fileData.length()) + " percent");
                }

                out.finish();
            } else {
                byte[] killMe = inFile.readAllBytes();
                int size = killMe.length;
                out.write(killMe, 0, size);
                out.finish();
                //System.out.println(Singleton.getMatches().toString() + " matchesSet: " + Singleton.matchesSet + " totalMatches: " + Singleton.totalMatches);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void Encoder(String wavPath, String midiPath, String fileName, boolean processOG) throws IOException, InvalidMidiDataException, MidiUnavailableException, InterruptedException {

        MidiParser m = new MidiParser(wavPath, fileName, processOG);
        ArrayList<MidiSegment> segments = m.theStuff(midiPath);
        ArrayList<MidiSegment> segments1 = m.segments1;
        ArrayList<MidiSegment> segmentsMap = m.segmentsMap;

        try {
            //var sortedSegments = stupidSort2(segments);
            //var sortedSegments1 = stupidSort2(segments1);
            var sortedSegments = stupidSort2(segmentsMap);

            ArrayList<StupidInteger> stupidIntegers = new ArrayList<>();
            // ArrayList<StupidInteger> stupidIntegers1 = new ArrayList<>();

            for (int i = 0; i < segmentsMap.size(); i++) {
                stupidIntegers.add(new StupidInteger(i));
              //  stupidIntegers1.add(new StupidInteger(i));
                //segments.get(i).segmentIndex = stupidIntegers.get(i);
                //segments1.get(i).segmentIndex = stupidIntegers1.get(i);
                segmentsMap.get(i).segmentIndex = stupidIntegers.get(i);
            }

            SliceSegments(m, segmentsMap, sortedSegments); //TODO: EVIL LIVES HERE
            //SliceSegments(m, segments1, sortedSegments1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File map = new File("/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "/mapfirstchannel.txt");
            FileWriter myWriter = new FileWriter(map);

            for (MidiSegment segment : segments) {

                String toWrite = MidiParser.segmentEntry(segment) + "\n";
                myWriter.write(toWrite);
            }
            myWriter.close();

            map = new File("/Users/jacksegil/Desktop/compression/testfiles/" + fileName  + "/mapsecondchannel.txt");
            myWriter = new FileWriter(map);

            for (MidiSegment segment : segments1) {
                String toWrite = MidiParser.segmentEntry(segment) + "\n";
                myWriter.write(toWrite);
            }

            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void SliceSegments(MidiParser m, ArrayList<MidiSegment> segments, ArrayList<SegmentContainer> sortedSegments) throws Exception {

        ArrayList<String> registry = new ArrayList<>();
        int iteration = 0;
        for (SegmentContainer container : sortedSegments) {


            ArrayList<MidiSegment> instances = container.segments;
            int partNumber = 1;
            while (!instances.isEmpty()) {
                SegmentContainer firstHalves = new SegmentContainer(container.segments.getFirst(), 0);
                SegmentContainer secondHalves = new SegmentContainer(container.segments.getFirst(), 0);
                firstHalves.setLengthSplit(partNumber);
                secondHalves.setLengthSplit(partNumber);

                ArrayList<int[]> data = new ArrayList<>();
                for (MidiSegment seg : instances) {
                    data.addAll(seg.data);
                }

                int length = MidiParser.getSmallestLength(data);
                for (MidiSegment instance : instances) {
                    ArrayList<int[]> tempList = new ArrayList<int[]>();
                    tempList.add(Arrays.copyOfRange(instance.data.getFirst(), 0, length));
                    MidiSegment toAdd = new MidiSegment(instance.duration, instance.notes, tempList, instance.channel, instance.perfSplit, true);
                    toAdd.segmentIndex = instance.segmentIndex;
                    firstHalves.addMidiSegment(toAdd);

                    if (length < instance.data.getFirst().length) {
                        ArrayList<int[]> tempList2 = new ArrayList<int[]>();//TODO: KILL TEMPLIST BY GETTING RID OF ARRAYLIST OF INT ARRAYS IN MIDISEGMENT AND JUST STORING THE GODDAMN INT ARRAY
                        tempList2.add(Arrays.copyOfRange(instance.data.getFirst(), length, instance.data.getFirst().length));
                        toAdd = new MidiSegment(instance.duration, instance.notes, tempList2, instance.channel, instance.perfSplit, true);
                        toAdd.segmentIndex = instance.segmentIndex;
                        secondHalves.addMidiSegment(toAdd);
                    }
                }

                int position = 0;

                if (container.notes.equals("7230753079300")) {
                    System.out.println("f4ee");
                }


                for (MidiSegment segment : firstHalves.segments) { //assumes firstHalves is a container containing midisegments compeltely ready for writing (outside of the lengthSplit value, which will be set here) , this loop updates the order refernence segment array for encoding purposees

                    MidiSegment segmentToSearchFor = new MidiSegment(segment.duration, segment.notes, segment.data, segment.channel, partNumber - 1, segment.perfSplit, segment.index);
                    //MidiSegment segmentToSearchFor = instances.get(position);
                    if (segmentToSearchFor.segmentIndex == segment.segmentIndex) {
                        System.out.println("f4");
                    }
                    segmentToSearchFor.segmentIndex = segment.segmentIndex;
                    int indexOfLatestSplit = segments.indexOf(segmentToSearchFor);

                    MidiSegment segmentToAdd = new MidiSegment(segment.duration, segment.notes, segment.data, segment.channel, partNumber, segment.perfSplit, segment.index);


                    if (indexOfLatestSplit == -1) {
                        System.err.println("dang, not found");
                    }

                    if (partNumber == 1) {
                        segmentToAdd.segmentIndex = segmentToSearchFor.segmentIndex;
                        segments.set(indexOfLatestSplit, segmentToAdd);
                    } else {
                        segmentToAdd.segmentIndex = segmentToSearchFor.segmentIndex;
                        segments.add(indexOfLatestSplit + 1, segmentToAdd);//TODO: they're getting added to the wrong things, we needa better way of identifiyng hte INDEX of the segment to search for cause the index property isn't reliable!

                       /* for (int i = 0; i < secondHalves.segments.size(); i++) {
                            if (secondHalves.segments.get(i).segmentIndex.getValue() > indexOfLatestSplit) {
                                secondHalves.segments.get(i).segmentIndex.increment();
                            }
                        }
                        for (int i = 0; i < firstHalves.segments.size(); i++) {
                            if (firstHalves.segments.get(i).segmentIndex.getValue() > indexOfLatestSplit) {
                                firstHalves.segments.get(i).segmentIndex.increment();
                            }
                        }*/ /*
                        for (int i = 0; i < segments.size(); i++) {
                            segments.get(i).segmentIndex.setValue(i);
                        }*/
                    }


                    position++;
                }


                instances = secondHalves.segments;
                partNumber++;

                String output = firstHalves.notes + "l" + firstHalves.getLengthSplit() + "p" + firstHalves.getPerfSplit();

                if (registry.contains(output)) {
                    throw new Exception("whyyyyyyy");
                }

                registry.add(output);


                m.writeIntoWAVFiles(firstHalves);
            }


            iteration++;
        }
    }

    public static void writeWAV(ArrayList<int[]> channels, int sampleRate, String outputPath, int bitDepth, int numChannels) throws IOException, InterruptedException {
        AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(LRCParser.writePCMToByteArray(channels, bitDepth, numChannels)), new AudioFormat(sampleRate, bitDepth, numChannels, true, false), channels.getFirst().length);

        FileOutputStream fileOut = new FileOutputStream(outputPath);
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, fileOut);
        fileOut.close();
        ProcessBuilder pb = new ProcessBuilder("/Users/jacksegil/Downloads/mp4alsRM23/bin/mac/mp4alsRM23", "-t" + numChannels, "-7", outputPath);
        pb.directory(new File(System.getProperty("user.home")));
        Process p = pb.start();
        p.waitFor();
    }

    public static ArrayList<SegmentContainer> stupidSort2(ArrayList<MidiSegment> segments) throws Exception {
        ArrayList<SegmentContainer> toReturn = new ArrayList<>();

        int count = 0;

        for (MidiSegment currSegment : segments) {
            SegmentContainer container = new SegmentContainer(currSegment);
            if (toReturn.contains(container)) {
                toReturn.get(toReturn.indexOf(container)).addMidiSegment(currSegment);
            } else {
                toReturn.add(container);
            }
            //for debugging
            count++;
            int totalSize = 0;
            for (SegmentContainer container2 : toReturn) {
                totalSize += container2.segments.size();
            }
            if (count != totalSize) {
                throw new Exception("I am going to throw this computer into hell");
            }
        }

        for(int i = 0; i < toReturn.size(); i++) {
            SegmentContainer container = toReturn.get(i);
            if (container.size() == 1) {
                MidiSegment segment = container.segments.getFirst();
                toReturn.remove(container);

                if (segment.channel == 1) {
                    segment.channel = 0;
                } else if (segment.channel == 0) {
                    segment.channel = 1;
                }// 2 is for mixed-channel containers
                SegmentContainer survivor = toReturn.get(toReturn.indexOf(new SegmentContainer(segment)));
                survivor.channel = 2;
                segment.channel = 2;
                survivor.segments.getFirst().channel = 2;
                survivor.addMidiSegment(segment);
                i--;
            }
        }

        int limit = 200; //max number of tracks we allow to be written to a single file
        for (int i = 0; i < toReturn.size(); i++) {
            SegmentContainer currContainer = toReturn.get(i);
            int divisor = currContainer.size() / limit; //divisor must be dynamic or i will exprience incredible, incredible pain
            // int quarter = currContainer.size() / divisor; // change name, not really a quarter


            if (currContainer.size() > limit) {
                int guard = 0;
                int evilIndex = i;
                for (int k = 0; k < divisor; k++) {// TODO: standardize numbers, figure out a way to include a part number even if not needed so can reliabiliy know position of each signigifant end digit
                    SegmentContainer toAdd = new SegmentContainer(new ArrayList<>(currContainer.segments.subList(k * limit, (k + 1) * limit)), k);
                    if (toReturn.contains(toAdd)) {
                        evilIndex = toReturn.indexOf(toAdd);
                        if (evilIndex != i) {
                            throw new Exception("aaaaaaaaaaa " + evilIndex);
                        }
                        //toReturn.add(toAdd);
                        guard += toAdd.size();
                        toReturn.set(evilIndex, toAdd);
                    } else {
                        toReturn.add(toAdd);
                        guard += toAdd.size();
                    }
                }
                if (currContainer.size() % limit > 0) {
                    SegmentContainer toAdd = new SegmentContainer(new ArrayList<>(currContainer.segments.subList(limit * (divisor), currContainer.size())), divisor);
                    toReturn.add(toAdd);
                    guard += toAdd.size();
                }

                if (evilIndex != i) {
                    throw new Exception("aaaaaaaaaaa " + evilIndex);
                }
                if (guard != currContainer.size()) {
                    throw new Exception("aaaadaaaaaaa " + guard);
                }
                //toReturn.remove(i);

                i--;
            }
        }

        return toReturn;
    }

}

