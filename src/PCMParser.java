import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.*;

public class PCMParser
{
    private byte[] bytes;
    private String pcmFileName;
    public int[] theStuff;
    public int[] pcm1, pcm2;


    public PCMParser(String pcmFile) throws IOException {
        pcmFileName = pcmFile;
        File fileData = new File(pcmFileName);
        FileInputStream inFile = new FileInputStream(pcmFileName);
        bytes = inFile.readAllBytes();
        inFile.close();
        theStuff = toIntArray(bytes);
    }

    public PCMParser(String pcm1path, String pcm2path) throws IOException {
        File fileData = new File(pcm1path);
        FileInputStream inFile = new FileInputStream(pcm1path);
        pcm1 = toIntArray(inFile.readAllBytes());
        inFile.close();

        fileData = new File(pcm2path);
        inFile = new FileInputStream(pcm2path);
        pcm2 = Arrays.copyOfRange(toIntArray(inFile.readAllBytes()), 0, pcm1.length); // remember to revert!
        inFile.close();
    }



    public int[] toIntArray(byte[] input) {
        /*int[] ints = new int[input.length / 4];
        for (int i = 0; i < input.length / 4; i++) {
            //ints[i] = Integer.parseInt(String.valueOf(input[i * 4]));
            byte b1 = input[i * 4];
            byte b2 = input[i * 4 + 1];
            byte b3 = input[i * 4 + 2];
            byte b4 = input[i * 4 + 3];
            ints[i] = ((0xFF & b1) << 24) | ((0xFF & b2) << 16) |
                    ((0xFF & b3) << 8) | (0xFF & b4);
        }
        return ints;*/
        IntBuffer intBuf =
                ByteBuffer.wrap(input)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asIntBuffer();
        int[] array = new int[intBuf.remaining()];
        intBuf.get(array);
        return array;

    }

    public void calculateDiffSavings() throws Exception {
        System.out.println("Size of pcm1: " + countUsedBits(pcm1));
        System.out.println("Size of pcm2: " + countUsedBits(pcm2) + " " + countUsedBits(pcm2.length));
        System.out.println("Size of pcm1 - pcm2: " + countUsedBits(difference(pcm1, pcm2)));
        System.out.println("Size saved in bits " + (Math.max(countUsedBits(pcm1), countUsedBits(pcm2)) - countUsedBits(difference(pcm1, pcm2))));
        int[] pcm1Small = Arrays.copyOfRange(pcm1, (int) (pcm1.length * (0.25)), (int) (pcm1.length * 0.2505));
       // findBestMatch(pcm1Small, pcm2);

        ArrayList<Integer> zeros = findZeros(pcm1);
        long sum = 0;
        long trueSum = 0;
        for (int i = 0; i < zeros.size() - 1; i++) {
            long toAdd = findBestMatch(Arrays.copyOfRange(pcm1, zeros.get(i), zeros.get(i + 1)), pcm2);
            if (toAdd > 0) {
                sum += toAdd;
            }
            if (toAdd - 18 > 0) {
                trueSum += toAdd - 18;
            }
            //trueSum += toAdd - 18;
            //System.out.println("We are at " + 100 * ((double) i / zeros.size()));
        }
        System.out.println("Bits saved " + sum);
        System.out.println("Bytes saved " + ((double) sum / 8));
        System.out.println("True bits saved " + trueSum);
        System.out.println("True bytes saved " + ((double) trueSum / 8));


    }

    public long findBestMatch(int[] small, int[] big) throws Exception {
       // double ratio = 1.0;
        long bitsSaved = 0;
        long smallBits = countUsedBits(small);
        int[] bigBitsArray = usedBitsArray(big);
        long bigBits = arraySum(Arrays.copyOfRange(bigBitsArray, 0, small.length));
        int index = 0;

        for (int i = small.length; i < big.length; i++) {
           // System.out.println(((double)(i-small.length) / (big.length - small.length)) * 100);
            int[] bigSection = Arrays.copyOfRange(big, i - small.length, i);
           // double currRatio = (double) countUsedBits(difference(small, bigSection)) / Math.max(smallBits, bigBits);
            long currBitsSaved = Math.max(smallBits, bigBits) - countUsedBits(difference(small, bigSection));

            //bitsSaved = Math.max(bitsSaved, currBitsSaved);
            if (bitsSaved < currBitsSaved) {
                bitsSaved = currBitsSaved;
                index = i;
            }
          //  ratio = Math.min(ratio, currRatio);
            bigBits -= bigBitsArray[i - small.length];
            bigBits += bigBitsArray[i];
        }
       // System.out.println("Ratio: " + ratio);
       // System.out.println("Bits saved " + bitsSaved);
       // System.out.println("Bytes saved " + (double) bitsSaved / 8);
        return bitsSaved;
    }

    public static int[] difference(int[] a, int[] b) throws Exception{
        if (a.length != b.length) {
            throw new Exception("The array lengths do not match. First array has length of " + a.length + " and second array has length of " + b.length);
        }
        int[] toReturn = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            toReturn[i] = a[i] - b[i];
        }
        return toReturn;
    }

    public static long countUsedBits2(long z) {
        if (z == 0) {
            return 0;
        }
        return countUsedBits2(z >>> 1) + 1;
    }

    public static int countUsedBits(int z) {
        //return 1 + (long) (Math.log(Math.abs(z)) / Math.log(2) + 1);
       // return Integer.toBinaryString(z).length();
        int count = 0;
        z = Math.abs(z);
        while (z > 0)
        {
            count++;
            z >>= 1;
        }

        return count + 1;
    }



    public static long countUsedBits(int[] arr) {
        long count = 0;
        for (int i = 0; i < arr.length; i++) {
            long toAdd = countUsedBits(arr[i]);
            count += toAdd;
        }
        return count;
    }

    public static int[] usedBitsArray(int[] arr) {
        int[] toReturn = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            toReturn[i] = (int) countUsedBits(arr[i]);
        }
        return toReturn;
    }

    public static long arraySum(int[] arr) {
        long sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }







    public int numZeros() {
        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        ArrayList<int[]> stupid = new ArrayList<>();

        int count = 0;
        int dist = 0;
        int prevIndex = 0;
        int dupes = 0;

        hashMap.put(Arrays.hashCode(theStuff), 0);
        System.out.println(hashMap.containsKey(Arrays.hashCode(theStuff.clone())));

        theStuff = new int[] {-1, -1, 2, 4, -1, 2, 4, 5, 6, -2, -4, -1, 2, 4, 5, 6};
        //theStuff = new int[] {-1, -1, 2, 4, 5, 6, -2, -4, -1, 2, 4, 5, 6};
        for (int i = 0; i < theStuff.length - 1; i++) {
            //System.out.println(Integer.toHexString(ints[i]));
            if (theStuff[i] * theStuff[i+1] < 0) {
                if (i - prevIndex > 1 && i - prevIndex + 1 < dist) { // second term equivlaent to checking if i - prevIndex != dist + 1


                    if (hashMap.containsKey(Arrays.hashCode(Arrays.copyOfRange(theStuff, prevIndex, i + 1)))) {
                        dupes++;
                    } else {
                        hashMap.put(Arrays.hashCode(Arrays.copyOfRange(theStuff, prevIndex, i + 1)), 1);

                    }
                    stupid.add(Arrays.copyOfRange(theStuff, prevIndex, i + 1));

                    if (hashMap.containsKey(Arrays.hashCode(Arrays.copyOfRange(theStuff, i - dist, prevIndex)))) {
                        dupes++;
                    } else {
                        hashMap.put(Arrays.hashCode(Arrays.copyOfRange(theStuff, i - dist, prevIndex)), 1);

                    }
                    stupid.add(Arrays.copyOfRange(theStuff, i - dist, prevIndex));

                    count += 2;
                    dist = 0;

                } else if (dist > 1) {


                    if (i - dist == 0) {
                        if (hashMap.containsKey(Arrays.hashCode(Arrays.copyOfRange(theStuff, i - dist, i + 1)))) {
                            dupes++;
                        } else {
                            hashMap.put(Arrays.hashCode(Arrays.copyOfRange(theStuff, i - dist, i + 1)), 1);

                        }
                        stupid.add(Arrays.copyOfRange(theStuff, i - dist, i + 1));
                    } else {
                        if (hashMap.containsKey(Arrays.hashCode(Arrays.copyOfRange(theStuff, i - dist + 1, i + 1)))) {
                            dupes++;
                        } else {
                            hashMap.put(Arrays.hashCode(Arrays.copyOfRange(theStuff, i - dist + 1, i + 1)), 1);

                        }
                        stupid.add(Arrays.copyOfRange(theStuff, i - dist + 1, i + 1));
                    }



                    count++;
                    dist = 0;

                }
                prevIndex = i + 1;
           /* if (theStuff[i] < 0 && theStuff[i + 1] > 0) {
                count++;
            }*/
            }
                dist++;

            if (i == theStuff.length - 2) {
                if (hashMap.containsKey(Arrays.hashCode(Arrays.copyOfRange(theStuff, prevIndex, i + 2)))) {
                    dupes++;

                } else {
                    hashMap.put(Arrays.hashCode(Arrays.copyOfRange(theStuff, prevIndex, i + 2)), 1);

                }
                stupid.add(Arrays.copyOfRange(theStuff, prevIndex, i + 2));
            }


        }
        System.out.println(dupes);
        return count;



    }

    public int trueNumZeros() {
        int count = 0;
        for (int i = 0; i < theStuff.length - 1; i++) {
            if (theStuff[i] * theStuff[i+1] < 0) {
                count++;
            }
        }
        return count;
    }
    public long averageMax (int[] arr) {
        ArrayList<Integer> maxes = findTrueMaxes(arr);
        long sum = 0;
        for (int i : maxes) {
            sum += i;
        }
        return sum / maxes.size();
    }

    public long average(int[] arr) {
        long sum = 0;
        for (int i : arr) {
            sum += i;
        }
        return sum / arr.length;
    }

    public long localAverage(ArrayList<Integer> arr) {
        long sum = 0;
        for (Integer integer : arr) {
            sum += integer;
        }
        return sum / arr.size();
    }


    public void printStuff() {
        int[] maxes = findLocalMaximaMinima(theStuff.length, theStuff);
        long sum = 0;
        for (int i : maxes) {
            sum += i;
        }
        long average = sum / maxes.length;
        System.out.println(average);
        int count = 0;
        for (int i = 0; i < maxes.length - 1; i++) {
            if ((maxes[i] > average && maxes[i + 1] < average) || (maxes[i] < average && maxes[i + 1] > average)) {
                count++;
            }
            if (maxes[i] == average) {
                count++;
            }
        }
        System.out.println(count);

    }

    public int[] stupid(ArrayList<int[]> arr) {
        int [] toReturn = new int[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            toReturn[i] = arr.get(i)[1];
        }
        return toReturn;
    }

    public void lastResort() {
        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        int dupes = 0;
        for (int i = 0; i < theStuff.length; i++) {
            if (hashMap.containsKey(theStuff[i])) {
                dupes++;
            } else {
                hashMap.put(theStuff[i], i);
            }
        }
        System.out.println(dupes + " " + theStuff.length);
        System.out.println((double) dupes / theStuff.length);
    }

    public void printMaximaMinima() {
        //int[] maxes = findLocalMaximaMinima(theStuff.length, theStuff);
        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        //ArrayList<int[]> maxes = findTrueMaxesWithBiasAverage(theStuff);
        ArrayList<Integer> maxes = findZeros(theStuff);
        //int[] maxesmaxes = findLocalMaximaMinima(maxes.size(), maxes);
        int [] maxesmaxes = maxes.stream().mapToInt(i -> i).toArray();

        int dupes = 0;
        for (int i = 0; i < maxesmaxes.length; i++) {
            if (i == 0) {
                hashMap.put(Arrays.hashCode(Arrays.copyOfRange(theStuff, 0, maxesmaxes[i])), 1);
            } else {
                if (hashMap.containsKey(Arrays.hashCode(Arrays.copyOfRange(theStuff, maxesmaxes[i - 1], maxesmaxes[i])))) {
                    dupes++;
                } else {
                    hashMap.put(Arrays.hashCode(Arrays.copyOfRange(theStuff, maxesmaxes[i - 1], maxesmaxes[i])), 1);
                }
            }
        }
        if (hashMap.containsKey(Arrays.hashCode(Arrays.copyOfRange(theStuff, maxesmaxes[maxesmaxes.length - 1], theStuff.length)))) {
            dupes++;
        } else {
            hashMap.put(Arrays.hashCode(Arrays.copyOfRange(theStuff, maxesmaxes[maxesmaxes.length - 1], theStuff.length)), 1);
        }
        System.out.println(dupes);
        System.out.println(maxesmaxes.length);
    }

    public static int[] findLocalMaximaMinima(int n, ArrayList<int[]> arr) {

        // Empty vector to store points of
        // local maxima and minima
        Vector<Integer> mx = new Vector<Integer>();
        Vector<Integer> mn = new Vector<Integer>();


        // Checking whether the first point is
        // local maxima or minima or none
        if (arr.getFirst()[0] > arr.get(1)[0] && (arr.getFirst()[0] > 0))
            mx.add(arr.get(0)[1]);

        else if (arr.getFirst()[0] < arr.get(1)[0] && (arr.getFirst()[0] < 0))
            mn.add(arr.get(0)[1]);

        // Iterating over all points to check
        // local maxima and local minima
        for (int i = 1; i < n - 1; i++) {
            // Condition for local minima
            if ((arr.get(i - 1)[0] > arr.get(i)[0]) && (arr.get(i)[0] < arr.get(i + 1)[0]) && (arr.get(i)[0] < 0))
                mn.add(arr.get(i)[1]);

                // Condition for local maxima
            else if ((arr.get(i - 1)[0] < arr.get(i)[0]) && (arr.get(i)[0] > arr.get(i + 1)[0]) && (arr.get(i)[0] > 0)) {
                mx.add(arr.get(i)[1]);
            }
        }

        // Checking whether the last point is
        // local maxima or minima or none
        if (arr.get(n - 1)[0] > arr.get(n - 2)[0] && (arr.get(n - 1)[0] > 0))
            mx.add(arr.get(n - 1)[1]);

        else if (arr.get(n - 1)[0] < arr.get(n - 2)[0] && (arr.get(n - 1)[0] < 0))
            mn.add(arr.get(n - 1)[1]);

        // Print all the local maxima and
        // local minima indexes stored
        int[] toReturn = new int[mx.size()];
        if (!mx.isEmpty())
        {
            for (int i = 0; i < toReturn.length; i++) {
                toReturn[i] = mx.get(i);
            }
        }
        else
            System.out.println("There are no points " +
                    "of Local Maxima ");

        return toReturn;
    }

    public static int[] findLocalMaximaMinima(int n, int[] arr) {

        // Empty vector to store points of
        // local maxima and minima
        Vector<Integer> mx = new Vector<Integer>();
        Vector<Integer> mn = new Vector<Integer>();


        // Checking whether the first point is
        // local maxima or minima or none
        if (arr[0] > arr[1] && (arr[0] > 0))
            mx.add(0);

        else if (arr[0] < arr[1] && (arr[0] < 0))
            mn.add(0);

        // Iterating over all points to check
        // local maxima and local minima
        for (int i = 1; i < n - 1; i++) {
            // Condition for local minima
            if ((arr[i - 1] > arr[i]) && (arr[i] < arr[i + 1]) && (arr[i] < 0))
                mn.add(i);

                // Condition for local maxima
            else if ((arr[i - 1] < arr[i]) && (arr[i] > arr[i + 1]) && (arr[i] > 0)) {
                mx.add(i);
            }
        }

        // Checking whether the last point is
        // local maxima or minima or none
        if (arr[n - 1] > arr[n - 2] && (arr[n - 1] > 0))
            mx.add(n - 1);

        else if (arr[n - 1] < arr[n - 2] && (arr[n - 1] < 0))
            mn.add(n - 1);

        // Print all the local maxima and
        // local minima indexes stored
         int[] toReturn = new int[mx.size()];
         if (!mx.isEmpty())
         {
             for (int i = 0; i < toReturn.length; i++) {
                 toReturn[i] = arr[mx.get(i)];
             }
         }
        else
            System.out.println("There are no points " +
                    "of Local Maxima ");

        /*if (!mn.isEmpty())
        {
            System.out.print("Points of Local " +
                    "minima are : ");
            for(Integer a : mn)
                System.out.print(a + " ");
            System.out.println();
        }
        else {
            System.out.println("There are no points of " +
                    "Local Maxima ");

                    }
        */
       // System.out.println(mx.size() + " " + mn.size());
        return toReturn;
    }

    public void printTrueMaxes() {
        findTrueMaxes(theStuff);
    }



    public ArrayList<Integer> findTrueMaxes(int[] arr) {
        int max = 0;
        ArrayList<Integer> maxes = new ArrayList<>();
        int sectionLength = 100;
        for (int i = 0; i < arr.length - 1; i++) {
            long average = 0;
            if (i > sectionLength) {
                average = localAverage(findTrueMaxesWithBias(Arrays.copyOfRange(arr, i - sectionLength, i)));
            }
            if (arr[i] > max) {
                max = arr[i];
            }
            if (arr[i] > 0 && arr[i + 1] < 0 && max != 0 && max > average){
                maxes.add(max);
                max = 0;
            }
            System.out.println((double) i / (arr.length - 1) + "percent done!");
        }
        System.out.println(maxes.size());
        return maxes;
    }

    public ArrayList<Integer> findTrueMaxesWithBias(int[] arr) {
        ArrayList<Integer> maxes = new ArrayList<>();
        ArrayList<Integer> dists = new ArrayList<>();
        int max = 0;
        int dist = 0;
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
            if (arr[i] > 0 && arr[i + 1] < 0 && max != 0){
                maxes.add(max);
                dists.add(dist);
                dist = 0;
                max = 0;
            } else {
                dist++;
            }
        }
        System.out.println("Size of all maxes " + maxes.size());
        System.out.println("Average distace between maxes " + localAverage(dists));
        Collections.sort(dists);
        return maxes;

    }
    public ArrayList<int[]> findTrueMaxesWithBiasAverage(int[] arr) {
        long average = localAverage(findTrueMaxesWithBias(arr));
        average = 0;
        System.out.println("Average for true maxes with bias is: " + average);
        ArrayList<int[]> maxes = new ArrayList<>();
        ArrayList<Integer> dists = new ArrayList<>();
        int max = 0;
        int dist = 0;
        int pos = 0;
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > max) {
                max = arr[i];
                pos = i;
            }
            if (arr[i] > 0 && arr[i + 1] < 0 && max != 0 && max > average){
                maxes.add(new int[] {max, pos});
                dists.add(dist);
                dist = 0;
                max = 0;
            } else {
                dist++;
            }
        }
        System.out.println("Size of maxes above avergae " + maxes.size());
        System.out.println("Average distance between maxes above average " + localAverage(dists));
        Collections.sort(dists);
        return maxes;

    }

    public ArrayList<Integer> findZeros(int[] arr) {
        ArrayList<Integer> zeros = new ArrayList<>();
        ArrayList<Integer> dists = new ArrayList<>();
        int dist = 0;
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] * arr[i + 1] < 0 ){
                zeros.add(i);
                dists.add(dist);
                dist = 0;
            } else {
                dist++;
            }
        }
        System.out.println("Average distance between zeros " + localAverage(dists));
        System.out.println("Number of zeros " + zeros.size());
        Collections.sort(dists);
        return zeros;

    }

    public double calculateStandardDeviationOfMaxes() {

        // get the mean of array
        ArrayList<Integer> maxes = findTrueMaxesWithBias(theStuff);

        double mean = localAverage(maxes);

        // calculate the standard deviation
        double standardDeviation = 0.0;
        for (int num : maxes) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / maxes.size());
    }






}
