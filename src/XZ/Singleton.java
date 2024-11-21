
package XZ;

import XZ.lz.Matches;

public class Singleton {
    private static final Singleton instance = new Singleton();

    private static Matches matches;
    public static int matchesSet;
    public static int totalMatches;
    public static boolean test = false;

    //Private constructor to prevent instantiation of the class from other classes.
    private Singleton(){}

    public static Singleton getInstance(){
        return instance;
    }

    public static Matches getMatches(){
        return matches;
    }

    public static void setMatches(Matches matches){
        Singleton.matches = matches;
        matchesSet++;
        totalMatches += matches.count;
    }
}