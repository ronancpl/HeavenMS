/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
/* NPC: MapleTV / Larry
	
	Exchanger NPC:
	* Scroll generator
        * 
        * @author Ronan Lana
*/

importPackage(Packages.client);
importPackage(Packages.config);
importPackage(Packages.constants.game);
importPackage(Packages.server);
importPackage(Packages.server.life);

var status;

var jobWeaponRestricted = [[[2043000, 2043100, 2044000, 2044100, 2043200, 2044200]], [[2043000, 2043100, 2044000, 2044100], [2043000, 2043200, 2044000, 2044200], [2044300, 2044400]], [[2043700, 2043800], [2043700, 2043800], [2043700, 2043800]], [[2044500], [2044600]], [[2044700], [2043300]], [[2044800], [2044900]]];
var aranWeaponRestricted = [jobWeaponRestricted[1][2][1]];

var tier1Scrolls = [];
var tier2Scrolls = [2040000, 2040400, 2040500, 2040600, 2040700, 2040800, 2040900];
var tier3Scrolls = [2048000, 2049200, 2041000, 2041100, 2041300, 2040100, 2040200, 2040300];

var typeTierScrolls = [["PAD", "MAD"], ["STR", "DEX", "INT", "LUK", "ACC", "EVA", "Speed", "Jump"], ["PDD", "MDD", "MHP", "MMP"]];

var sgItems = [4003004, 4003005, 4001006, 4006000, 4006001, 4030012];
var sgToBucket = [100, 50, 37.5, 37.5, 37.5, 200];
var mesoToBucket = 2800000;

var sgAppliedItems = [0, 0, 0, 0, 0, 0];
var sgAppliedMeso = 0;

var sgBuckets = 0.0;
var sgBookBuckets = 0.0;
var sgItemBuckets = 0.0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;

        if(status == 0) {
            cm.sendNext("This is the MapleTV Scroll Generator broadcast. Place your supplies or mesos earned throughout your adventure to redeem a prize! You can place #bany amount of supplies#k, however take note that placing #rdifferent supplies#k with #rbigger shots of any of them#k will improve the reward possibilities!");
        } else if(status == 1) {
            var sendStr;

            //print("Book: " + sgBookBuckets + " Item: " + sgItemBuckets);
            
            if(sgItemBuckets > 0.0) sendStr = "With the items you have currently placed, you have #r" + sgBuckets + "#k buckets (#r" + (sgItemBuckets < 1.0 ? sgItemBuckets.toFixed(2) : Math.floor(sgItemBuckets)) + "#k supply buckets) for claiming a prize. Place supplies:";
            else sendStr = "You have placed no supplies yet. Place supplies:";

            var listStr = "";
            var i;
            for(i = 0; i < sgItems.length; i++) {
                listStr += "#b#L" + i + "##t" + sgItems[i] + "##k";
                if(sgAppliedItems[i] > 0) listStr += " - " + sgAppliedItems[i];
                listStr += "#l\r\n";
            }

            listStr += "#b#L" + i + "#Mesos#k";
            if(sgAppliedMeso > 0) listStr += " - " + sgAppliedMeso;
            listStr += "#l\r\n";

            cm.sendSimple(sendStr + "\r\n\r\n" + listStr + "#r#L" + (sgItems.length + 2) + "#Retrieve a prize!#l#k\r\n");
        } else if(status == 2) {
            if(selection == (sgItems.length + 2)) {
                if(sgItemBuckets < 1.0) {
                    cm.sendPrev("You have set not enough supplies. Insert at least one bucket of #bsupplies#k to claim a prize.");
                } else {
                    generateRandomScroll();
                    cm.dispose();
                }
            } else {
                var tickSel;
                if(selection < sgItems.length) {
                    tickSel = "of #b#t" + sgItems[selection] + "##k";
                    curItemQty = cm.getItemQuantity(sgItems[selection]);
                } else {
                    tickSel = "#bmesos#k";
                    curItemQty = cm.getMeso();
                }
                
                curItemSel = selection;
                if(curItemQty > 0) {
                    cm.sendGetText("How many " + tickSel + " do you want to provide? (#r" + curItemQty + "#k available)#k");
                } else {
                    cm.sendPrev("You have got #rnone#k " + tickSel + " to provide for Scroll Generation. Click '#rBack#k' to return to the main interface.");
                }
            }
        } else if(status == 3) {
            var text = cm.getText();

            try {
                var placedQty = parseInt(text);
                if(isNaN(placedQty) || placedQty < 0) throw true;

                if(placedQty > curItemQty) {
                    cm.sendPrev("You cannot insert the given amount of #r" + (curItemSel < sgItems.length ? "#t" + sgItems[curItemSel] + "#" : "mesos") + "#k (#r" + curItemQty + "#k available). Click '#rBack#k' to return to the main interface.");
                } else {
                    if(curItemSel < sgItems.length) sgApplyItem(curItemSel, placedQty);
                    else sgApplyMeso(placedQty);

                    cm.sendPrev("Operation succeeded. Click '#rBack#k' to return to the main interface.");
                }
            } catch(err) {
                cm.sendPrev("You must enter a positive number of supplies to insert. Click '#rBack#k' to return to the main interface.");
            }

            status = 2;
        } else {
            cm.dispose();
        }
    }
}

function getJobTierScrolls() {
    var scrolls = [];

    var job = cm.getPlayer().getJob();
    var jobScrolls = jobWeaponRestricted[Math.floor(cm.getPlayer().getJobStyle().getId() / 100)];
    
    var jobBranch = GameConstants.getJobBranch(job);
    if (jobBranch >= 2) {
        Array.prototype.push.apply(scrolls, jobScrolls[Math.floor((job.getId() / 10) % 10) - 1]);
    } else {
        for (var i = 0; i < jobScrolls.length; i++) {
            Array.prototype.push.apply(scrolls, jobScrolls[i]);
        }
    }
    
    return scrolls;
}

function getScrollTypePool(rewardTier) {
    var scrolls = [];
    switch (rewardTier) {
        case 1:
            if (cm.getPlayer().isAran()) {
                Array.prototype.push.apply(scrolls, aranWeaponRestricted);
            } else {
                Array.prototype.push.apply(scrolls, getJobTierScrolls());
            }
            
            Array.prototype.push.apply(scrolls, tier1Scrolls);
            break;
        case 2:
            Array.prototype.push.apply(scrolls, tier2Scrolls);
            break;
        default:
            Array.prototype.push.apply(scrolls, tier3Scrolls);
    }
    
    return scrolls;
}

function getScrollTier(scrollStats) {
    for (var i = 0; i < typeTierScrolls.length; i++) {
        for (var j = 0; j < typeTierScrolls[i].length; j++) {
            if (scrollStats.get(typeTierScrolls[i][j]) > 0) {
                return i + 1;
            }
        }
    }
    
    return 4;
}

function getScrollSuccessTier(scrollStats) {
    var prop = scrollStats.get("success");

    if (prop > 90) {
        return 3;
    } else if (prop < 50) {
        return YamlConfig.config.server.SCROLL_CHANCE_ROLLS > 2 ? 2 : 1;
    } else {
        return YamlConfig.config.server.SCROLL_CHANCE_ROLLS > 2 ? 1 : 2;
    }
}

function getAvailableScrollsPool(baseScrolls, rewardTier, successTier) {
    var scrolls = [];
    var ii = MapleItemInformationProvider.getInstance();
    
    for (var i = 0; i < baseScrolls.length; i++) {
        for (var j = 0; j < 100; j++) {
            var scrollid = baseScrolls[i] + j;
            var scrollStats = ii.getEquipStats(scrollid);
            if (scrollStats != null && ii.getScrollReqs(scrollid).isEmpty()) {
                var scrollTier = getScrollTier(scrollStats);
                if (scrollTier == rewardTier && successTier == getScrollSuccessTier(scrollStats)) {
                    scrolls.push(scrollid);
                }
            }
        }
    }

    return scrolls;
}

// passive tier buckets...

function getLevelTier(level) {
    return Math.floor((level - 1) / 15) + 1;
}

function getPlayerCardTierPower() {
    var cardset = cm.getPlayer().getMonsterBook().getCardSet();
    var countTier = [0, 0, 0, 0, 0, 0, 0, 0, 0];

    for (var iterator = cardset.iterator(); iterator.hasNext();) {
        var ce = iterator.next();

        var cardid = ce.getKey();
        var ceTier = Math.floor(cardid / 1000) % 10;
        countTier[ceTier] += ce.getValue();

        if (ceTier >= 8) {  // is special card
            var mobLevel = MapleLifeFactory.getMonsterLevel(MapleItemInformationProvider.getInstance().getCardMobId(cardid));
            var mobTier = getLevelTier(mobLevel) - 1;

            countTier[mobTier] += (ce.getValue() * 1.2);
        }
    }
    
    return countTier;
}

function calculateMobBookTierBuckets(tierSize, playerCards, tier) {
    if (tier < 1) {
        return 0.0;
    }

    tier--; // started at 1
    var tierHitRate = playerCards[tier] / (tierSize[tier] * 5);
    if (tierHitRate > 0.5) {
        tierHitRate = 0.5;
    }
    
    return tierHitRate * 4;
}

function calculateMobBookBuckets() {
    var book = cm.getPlayer().getMonsterBook();
    var bookLevelMult = 0.9 + (0.1 * book.getBookLevel());
    
    var playerLevelTier = getLevelTier(cm.getPlayer().getLevel());
    if (playerLevelTier > 8) {
        playerLevelTier = 8;
    }

    var tierSize = MonsterBook.getCardTierSize();
    var playerCards = getPlayerCardTierPower();
    
    var prevBuckets = calculateMobBookTierBuckets(tierSize, playerCards, playerLevelTier - 1);
    var currBuckets = calculateMobBookTierBuckets(tierSize, playerCards, playerLevelTier);
    
    return (prevBuckets + currBuckets) * bookLevelMult;
}

function recalcBuckets() {
    sgBookBuckets = calculateMobBookBuckets();
    sgItemBuckets = calculateSuppliesBuckets();
    
    var buckets = sgBookBuckets + sgItemBuckets;
    if (buckets > 6.0) {
        sgBuckets = 6;
    } else {
        sgBuckets = Math.floor(buckets);
    }
}

// variable buckets...

function sgApplyItem(idx, amount) {
    if (sgAppliedItems[idx] != amount) {
        sgAppliedItems[idx] = amount;
        recalcBuckets();
    }
}

function sgApplyMeso(amount) {
    if (sgAppliedMeso != amount) {
        sgAppliedMeso = amount;
        recalcBuckets();
    }
}

function calculateSuppliesBuckets() {
    var suppliesHitRate = 0.0;
    for (var i = 0; i < sgItems.length; i++) {
        suppliesHitRate += sgAppliedItems[i] / sgToBucket[i];
    }
    suppliesHitRate *= 2;

    suppliesHitRate += (sgAppliedMeso / mesoToBucket);
    return suppliesHitRate;
}

function calculateScrollTiers() {
    var buckets = sgBuckets;
    var tiers = [0, 0, 0];
    while (buckets > 0) {
        var pool = [];
        for (var i = 0; i < tiers.length; i++) {
            if (tiers[i] < 2) {
                pool.push(i);
            }
        }
        
        var rnd = pool[Math.floor(Math.random() * pool.length)];
        
        tiers[rnd]++;
        buckets--;
    }

    // normalize tiers
    for (var i = 0; i < tiers.length; i++) {
        tiers[i] = 3 - tiers[i];
    }

    return tiers;
}

function getRandomScrollFromTiers(tiers) {
    var typeTier = tiers[0], subtypeTier = tiers[1], successTier = tiers[2];
    var scrollTypePool = getScrollTypePool(typeTier);
    var scrollPool = getAvailableScrollsPool(scrollTypePool, subtypeTier, successTier);
    
    if (scrollPool.length > 0) {
        return scrollPool[Math.floor(Math.random() * scrollPool.length)];
    } else {
        return -1;
    }
}

function getRandomScrollFromRightPermutations(tiers) {
    for (var i = 2; i >= 0; i--) {
        for (var j = i - 1; j >= 0; j--) {
            if (tiers[i] >= 3) {
                break;
            } else if (tiers[j] > 1) {
                tiers[i]++;
                tiers[j]--;

                var itemid = getRandomScrollFromTiers(tiers);
                if (itemid != -1) {
                    return itemid;
                }
            }
        }
    }

    return -1;
}

function getRandomScroll(tiers) {
    var itemid = getRandomScrollFromTiers(tiers);
    if (itemid == -1) {
        // worst case shift-right permutations...
        itemid = getRandomScrollFromRightPermutations(tiers);
    }
    
    return itemid;
}

function performExchange(sgItemid, sgCount) {
    if (cm.getMeso() < sgAppliedMeso) {
        return false;
    }
    
    for (var i = 0; i < sgItems.length; i++) {
        var itemid = sgItems[i];
        var count = sgAppliedItems[i];
        if (count > 0 && !cm.haveItem(itemid, count)) {
            return false;
        }
    }

    cm.gainMeso(-sgAppliedMeso);
    
    for (var i = 0; i < sgItems.length; i++) {
        var itemid = sgItems[i];
        var count = sgAppliedItems[i];
        cm.gainItem(itemid, -count);
    }

    cm.gainItem(sgItemid, sgCount);
    return true;
}

function generateRandomScroll() {
    if (cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.USE).getNumFreeSlot() >= 1) {
        var itemid = getRandomScroll(calculateScrollTiers());
        if (itemid != -1) {
            if (performExchange(itemid, 1)) {
                cm.sendNext("Transaction accepted! You have received a #r#t" + itemid + "##k.");
            } else {
                cm.sendOk("Oh, it looks like some items are missing... Please double-check provided items in your inventory before trying to exchange.");
            }
        } else {
            cm.sendOk("Sorry for the inconvenience, but it seems there are no scrolls on store right now... Try again later.");
        }
    } else {
        cm.sendOk("Please look out for a slot available on your USE inventory before trying for a scroll.");
    }
}
