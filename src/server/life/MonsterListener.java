package server.life;
import client.MapleCharacter;

public interface MonsterListener {
    
    public void monsterKilled(int aniTime);
    public void monsterDamaged(MapleCharacter from, int trueDmg);
    public void monsterHealed(int trueHeal);
}
