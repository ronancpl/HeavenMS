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
package client;

public class MapleFamilyEntry {
    private int familyId;
    private int rank, reputation, totalReputation, todaysRep, totalJuniors, juniors, chrid;
    private String familyName;

    public int getId() {
        return familyId;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getChrId() {
        return chrid;
    }

    public void setChrId(int chrid) {
        this.chrid = chrid;
    }

    public int getReputation() {
        return reputation;
    }

    public int getTodaysRep() {
        return todaysRep;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public void setTodaysRep(int today) {
        this.todaysRep = today;
    }

    public void gainReputation(int gain) {
        this.reputation += gain;
        this.totalReputation += gain;
    }

    public int getTotalJuniors() {
        return totalJuniors;
    }

    public void setTotalJuniors(int totalJuniors) {
        this.totalJuniors = totalJuniors;
    }

    public int getJuniors() {
        return juniors;
    }

    public void setJuniors(int juniors) {
        this.juniors = juniors;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public int getTotalReputation() {
        return totalReputation;
    }

    public void setTotalReputation(int totalReputation) {
        this.totalReputation = totalReputation;
    }
}
