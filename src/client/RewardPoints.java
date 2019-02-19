package client;

import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RewardPoints {

    private int id;

    public RewardPoints(int id) {
        this.id = id;
    }

    public int getPoints() {
        int point = -1;
        try {
            ResultSet rs = query("SELECT rewardpoints FROM accounts WHERE id=" + id +";");
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
            query("UPDATE accounts SET rewardpoints=" + value + "WHERE id=" + id +";");
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
            throw new SQLException(e);
        } finally {
            try {
                ps.close();
            } catch (Exception e) { /* ignored */ }
            try {
                con.close();
            } catch (Exception e) { /* ignored */ }
        }
        return ps.executeQuery();
    }
}