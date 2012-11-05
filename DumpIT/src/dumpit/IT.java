/*
 * This is my log library. I use it in every project as a log system.
 * It outputs to console and files.
 */
package dumpit;

class Print {
    /**
     * Print string to System.out.
     * @param s 
     */
    public static void printToOut(String s) {
	System.out.println(s);
    }
    
    /**
     * Print string to System.err.
     * @param s 
     */
    public static void printToError(String s) {
	System.err.println(s);
    }    
}

/**
 *
 * @author gabrielpoca
 */
public class IT {
    
    /**
     * Log string.
     * @param s 
     */
    public static void log(String s) {
	Print.printToOut(s);
    }
    
    /**
     * Log error string.
     * @param s 
     */
    public static void error(String s) {
	Print.printToError(s);
    }
    
    /**
     * Outputs toString result on given object.
     * @param o 
     */
    public static void dump(Object o) {
	Print.printToOut(o.toString());
    }
     
}
