/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.server.world;

import client.MapleCharacter;
import client.MapleJob;

public class MaplePartyCharacter {
    private String name;
    private int id;
    private int level;
    private int channel, world;
    private int jobid;
    private int mapid;
    private boolean online;
    private MapleJob job;
    private MapleCharacter character;
    
    public MaplePartyCharacter(MapleCharacter maplechar) {
        this.character = maplechar;
    	this.name = maplechar.getName();
        this.level = maplechar.getLevel();
        this.channel = maplechar.getClient().getChannel();
        this.world = maplechar.getWorld();
        this.id = maplechar.getId();
        this.jobid = maplechar.getJob().getId();
        this.mapid = maplechar.getMapId();
        this.online = true;
        this.job = maplechar.getJob();
    }

    public MaplePartyCharacter() {
        this.name = "";
    }
    
    public MapleCharacter getPlayer() {
    	return character;
    }

    public MapleJob getJob() {
        return job;
    }

    public int getLevel() {
        return level;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
    
    public boolean isLeader() {
        return getPlayer().isPartyLeader();
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getMapId() {
        return mapid;
    }

    public void setMapId(int mapid) {
        this.mapid = mapid;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getJobId() {
        return jobid;
    }
    
    public int getGuildId() {
        return character.getGuildId();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MaplePartyCharacter other = (MaplePartyCharacter) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public int getWorld() {
        return world;
    }
    
    public String getJobNameById(int job) {
        switch (job) {
            case 0:
                return "Aprendiz";
            case 100:
                return "Guerreiro";// Warrior
            case 110:
                return "Soldado";
            case 111:
                return "Templario";
            case 112:
                return "Heroi";
            case 120:
                return "Escudeiro";
            case 121:
                return "Cavaleiro Branco";
            case 122:
                return "Paladino";
            case 130:
                return "Lanceiro";
            case 131:
                return "Cavaleiro Draconiano";
            case 132:
                return "Cavaleiro Negro";

            case 200:
                return "Bruxo";
            case 210:
                return "Feiticeiro (Fogo, Veneno)";
            case 211:
                return "Mago (Fogo, Veneno)";
            case 212:
                return "Arquimago (Fogo, Veneno)";
            case 220:
                return "Feiticeiro (Gelo, Raio)";
            case 221:
                return "Mago (Gelo, Raio)";
            case 222:
                return "Arquimago (Gelo, Raio)";
            case 230:
                return "Clérigo";
            case 231:
                return "Sacerdote";
            case 232:
                return "Sumo Sacerdote";

            case 300:
                return "Arqueiro";
            case 310:
                return "Caçador";
            case 311:
                return "Rastreador";
            case 312:
                return "Mestre Arqueiro";
            case 320:
                return "Balestreiro";
            case 321:
                return "Atirador";
            case 322:
                return "Atirador De Elite";

            case 400:
                return "Gatuno";
            case 410:
                return "Mercenario";
            case 411:
                return "Andarilho";
            case 412:
                return "Lorde Negro";
            case 420:
                return "Arruaceiro";
            case 421:
                return "Mestre Arruaceiro";
            case 422:
                return "Mestre Das Sombras";

            case 500:
                return "Pirata";
            case 510:
                return "Lutador";
            case 511:
                return "Saqueador";
            case 512:
                return "Foragido";
            case 520:
                return "Pistoleiro";
            case 521:
                return "Bucaneiro";
            case 522:
                return "Captain";

            default:
                return "Unknown Job";
        }
    }
}
