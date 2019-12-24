package constants.net;

public class ServerConstants {
    
    //Server Version
    public static short VERSION = 83;

    //Java Configuration
    public static final boolean JAVA_8 = getJavaVersion() >= 8;         //Max amount of times a party leader is allowed to persist on the Party Search before entry expiration (thus needing to manually restart the Party Search to be able to search for members).
    
    //Debug Variables
    public static int DEBUG_VALUES[] = new int[10];             // Field designed for packet testing purposes
    
    // https://github.com/openstreetmap/josm/blob/a3a6e8a6b657cf4c5b4c64ea14d6e87be6280d65/src/org/openstreetmap/josm/tools/Utils.java#L1566-L1585
    // Added by kolakcc (Familiar)
    /**
     * Returns the Java version as an int value.
     * @return the Java version as an int value (8, 9, etc.)
     * @since 12130
     */
    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        // Allow these formats:
        // 1.8.0_72-ea
        // 9-ea
        // 9
        // 9.0.1
        int dotPos = version.indexOf('.');
        int dashPos = version.indexOf('-');
        return Integer.parseInt(version.substring(0,
                dotPos > -1 ? dotPos : dashPos > -1 ? dashPos : 1));
    }
}
