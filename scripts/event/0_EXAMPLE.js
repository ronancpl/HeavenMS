// Event-instantiation variables
var isPq = true;
var minPlayers, maxPlayers;     // Range of party members for this event instance.
var minLevel, maxLevel;         // Level range of eligible team members for this event instance.
var entryMap;                   // Initial map, where players all moved into at the event startup.
var exitMap;                    // Upon failing to complete the event, players may be moved to this map.
var recruitMap;                 // Map where players must be before staring this event.
var clearMap;                   // Upon event clearing, players may be moved to this map.

var minMapId;                   // Event takes place inside these map id interval. Players found out is instantly dropped from the event.
var maxMapId;

var eventTime;                  // Max time allotted for the event, in minutes.

var lobbyRange = [0, 0];        // Range of concurrent lobbies (min range is 0, max range is 7).

function init() {
    // After loading, ChannelServer
}

function setLobbyRange() {
        return lobbyRange;
}

function setEventRequirements() {
    // sets requirement info about the event to be displayed at the recruitment area.
}

function setEventExclusives(eim) {
    // sets all items that should exist only for the event instance, and that should be removed from inventory at the end of the run.
}

function setEventRewards(eim) {
    // sets all possible treasures that can be given, randomly, to a player at the end of the event.
}

function getEligibleParty(party) {
    // selects, from the given party, the team that is allowed to attempt this event
}

function setup(eim, leaderid) {
    // Setup the instance when invoked, EG : start PQ
}

function afterSetup(eim) {
    // Happens after the event instance is initialized and all players have been assigned for the event instance, but before entrying players.
}

function respawnStages(eim) {
    // Defines which maps inside the event are allowed to respawn. This function should create a new task at the end of it's body calling itself at a given respawn rate.
}

function playerEntry(eim, player) {
    // Warp player in etc..
}

function playerUnregistered(eim, player) {
    // Do something with the player that is about to unregister right before unregistering he/she.
}

function playerExit(eim, player) {
    // Do something with the player right before disbanding the event instance.
}

function playerLeft(eim, player) {
    // Do something with the player right before leaving the party.
}

function changedMap(eim, player, mapid) {
    // What to do when player've changed map, based on the mapid.
}

function changedLeader(eim, leader) {
    // Do something if the party leader has been changed.
}

function scheduledTimeout(eim) {
    // When event timeout without before completion..
}

function timeOut(eim) {
    if (eim.getPlayerCount() > 0) {
        var pIter = eim.getPlayers().iterator();
        while (pIter.hasNext()){
            var player = pIter.next();
            player.dropMessage(6, "You have run out of time to complete this event!");
            playerExit(eim, player);
        }
    }
    eim.dispose();
}

function monsterKilled(mob, eim) {
    // Happens when an opposing mob dies
}

function monsterValue(eim, mobid) {
    // Invoked when a monster that's registered has been killed
    // return x amount for this player - "Saved Points"
}

function friendlyKilled(mob, eim) {
    // Happens when a friendly mob dies
}

function allMonstersDead(eim) {
    // When invoking unregisterMonster(MapleMonster mob) OR killed
    // Happens only when size = 0
}

function playerDead(eim, player) {
    // Happens when player dies
}

function monsterRevive(mob, eim) {
    // Happens when an opposing mob revives
}

function playerRevive(eim, player) {
    // Happens when player's revived.
    // @Param : returns true/false
}

function playerDisconnected(eim, player) {
    // return 0 - Deregister player normally + Dispose instance if there are zero player left
    // return x that is > 0 - Deregister player normally + Dispose instance if there x player or below
    // return x that is < 0 - Deregister player normally + Dispose instance if there x player or below, if it's leader = boot all
}

function end(eim) {
    // Happens when the party fails to complete the event instance.
}

function giveRandomEventReward(eim, player) {
    // Selects randomly a reward to give from the reward pool.
}

function clearPQ(eim) {
    // Happens when the party succeeds on completing the event instance.
}

function leftParty(eim, player) {
    // Happens when a player left the party
}

function disbandParty(eim, player) {
    // Happens when the party is disbanded by the leader.
}

function removePlayer(eim, player) {
    // Happens when the funtion NPCConversationManager.removePlayerFromInstance() is invoked
}

function registerCarnivalParty(eim, carnivalparty) {
    // Happens when carnival PQ is started. - Unused for now.
}

function onMapLoad(eim, player) {
    // Happens when player change map - Unused for now.
}

function cancelSchedule() {
    // Finishes ongoing schedules.
}

function dispose() {
    // Finishes the event instance.
}