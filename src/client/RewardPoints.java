package client;

import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RewardPoints {

    public int getPoints() {
        int point = -1;
        try {
            ResultSet rs = query("SELECT rewardpoints FROM accounts;");
            while (rs.next()) {
                point = rs.getInt("rewardpoints");
            }
        } catch (SQLException e) {
            System.out.println("RewardPoints failed to fetch rewardpoints");
        }
        return point;
    }

    public boolean setPoints(int value) {
        try {
            query("UPDATE accounts SET rewardpoints=" + value);
            return true;
        } catch (SQLException e) {
            System.out.println("RewardPoints failed to update rewardpoints");
        }
        return false;
    }

    private ResultSet query(String queryString) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement(queryString);
        } catch (SQLException e) {
            try {
                ps.close();
            } catch (Exception stacktrace) { /* ignored */ }
            try {
                con.close();
            } catch (Exception stacktrace) { /* ignored */ }
            throw new SQLException(e);
        }
        return ps.executeQuery();
    }
}