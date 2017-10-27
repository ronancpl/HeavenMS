package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FilePrinter {

    public static final String 
            ACCOUNT_STUCK = "accountStuck.txt",
            EXCEPTION_CAUGHT = "exceptionCaught.txt",
            CLIENT_START = "clientStartError.txt",
            ADD_PLAYER = "addPlayer.txt",
            MAPLE_MAP = "mapleMap.txt",
            ERROR38 = "error38.txt",
            PACKET_LOG = "log.txt",
            EXCEPTION = "exceptions.txt",
            SQL_EXCEPTION = "sqlexceptions.txt",
            PACKET_HANDLER = "PacketHandler/",
            PORTAL = "portals/",
            NPC = "npcs/",
            INVOCABLE = "invocable/",
            REACTOR = "reactors/",
            QUEST = "quests/",
            ITEM = "items/",
            MOB_MOVEMENT = "mobmovement.txt",
            MAP_SCRIPT = "mapscript/",
            DIRECTION = "directions/",
            SAVE_CHAR = "saveToDB.txt",
            INSERT_CHAR = "insertCharacter.txt",
            LOAD_CHAR = "loadCharFromDB.txt",
            UNHANDLED_EVENT = "doesNotExist.txt",
            SESSION = "sessions.txt",
            EXPLOITS = "exploits/",
            STORAGE = "storage/",
            PACKET_LOGS = "packetlogs/",
            DELETED_CHARACTERS = "deletedchars/",
            FREDRICK = "fredrick/",
            NPC_UNCODED = "uncodedNPCs.txt",
            QUEST_UNCODED = "uncodedQuests.txt",
            AUTOSAVING_CHARACTER = "saveCharAuto.txt",
            SAVING_CHARACTER = "saveChar.txt",
            USED_COMMANDS = "usedCommands.txt";//more to come (maps)
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //for file system purposes, it's nice to use yyyy-MM-dd
    private static final String FILE_PATH = "logs/" + sdf.format(Calendar.getInstance().getTime()) + "/"; // + sdf.format(Calendar.getInstance().getTime()) + "/"
    private static final String ERROR = "error/";

    public static void printError(final String name, final Throwable t) {
    	System.out.println("Error thrown: " + name);
    	System.out.println(getString(t));
        FileOutputStream out = null;
        final String file = FILE_PATH + ERROR + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(getString(t).getBytes());
            out.write("\n---------------------------------\r\n".getBytes());
        } catch (IOException ess) {
            ess.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
    }

    public static void printError(final String name, final Throwable t, final String info) {
    	System.out.println("Error thrown: " + name);
    	System.out.println(getString(t));
        FileOutputStream out = null;
        final String file = FILE_PATH + ERROR + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write((info + "\r\n").getBytes());
            out.write(getString(t).getBytes());
            out.write("\n---------------------------------\r\n".getBytes());
        } catch (IOException ess) {
            ess.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
    }

    public static void printError(final String name, final String s) {
    	System.out.println("Error thrown: " + name);
    	System.out.println(s);
        FileOutputStream out = null;
        final String file = FILE_PATH + ERROR + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            //out.write("\n---------------------------------\n".getBytes());
        } catch (IOException ess) {
            ess.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
    }

    public static void print(final String name, final String s) {
        print(name, s, true);
    }

    public static void print(final String name, final String s, boolean line) {
    	System.out.println("Log: " + name);
    	System.out.println(s);
        FileOutputStream out = null;
        String file = FILE_PATH + name;
        try {
            File outputFile = new File(file);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file, true);
            out.write(s.getBytes());
            out.write("\r\n".getBytes());
            if (line) {
                out.write("---------------------------------\r\n".getBytes());
            }
        } catch (IOException ess) {
            ess.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
    }

    private static String getString(final Throwable e) {
        String retValue = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            retValue = sw.toString();
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
        return retValue;
    }
}