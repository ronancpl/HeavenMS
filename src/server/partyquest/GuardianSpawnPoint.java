package server.partyquest;

import java.awt.Point;

/**
 *
 * @author David
 */
public class GuardianSpawnPoint {

    private Point position;
    private boolean taken;
    private int team = -1;

    public GuardianSpawnPoint(Point a) {
        this.position = a;
        this.taken = true;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }
}
