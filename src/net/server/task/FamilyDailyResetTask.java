package net.server.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import client.MapleFamily;
import net.server.world.World;
import tools.DatabaseConnection;
import tools.FilePrinter;

public class FamilyDailyResetTask implements Runnable {

    private final World world;

    public FamilyDailyResetTask(World world) {
        this.world = world;
    }

    @Override
    public void run() {
        resetEntitlementUsage(world);
        for(MapleFamily family : world.getFamilies()) {
            family.resetDailyReps();
        }
    }

    public static void resetEntitlementUsage(World world) {
        Calendar resetTime = Calendar.getInstance();
        resetTime.add(Calendar.MINUTE, 1); // to make sure that we're in the "next day", since this is called at midnight
        resetTime.set(Calendar.HOUR_OF_DAY, 0);
        resetTime.set(Calendar.MINUTE, 0);
        resetTime.set(Calendar.SECOND, 0);
        resetTime.set(Calendar.MILLISECOND, 0);
        try(Connection con = DatabaseConnection.getConnection()) {
            try(PreparedStatement ps = con.prepareStatement("UPDATE family_character SET todaysrep = 0, reptosenior = 0 WHERE lastresettime <= ?")) {
                ps.setLong(1, resetTime.getTimeInMillis());
                ps.executeUpdate();
            } catch(SQLException e) {
                FilePrinter.printError(FilePrinter.FAMILY_ERROR, e, "Could not reset daily rep for families. On " + Calendar.getInstance().getTime());
                e.printStackTrace();
            }
            try(PreparedStatement ps = con.prepareStatement("DELETE FROM family_entitlement WHERE timestamp <= ?")) {
                ps.setLong(1, resetTime.getTimeInMillis());
                ps.executeUpdate();
            } catch(SQLException e) {
                FilePrinter.printError(FilePrinter.FAMILY_ERROR, e, "Could not do daily reset for family entitlements. On " + Calendar.getInstance().getTime());
                e.printStackTrace();
            }
        } catch(SQLException e) {
            FilePrinter.printError(FilePrinter.FAMILY_ERROR, e, "Could not get connection to DB.");
            e.printStackTrace();
        }
    }
}
