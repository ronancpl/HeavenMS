importPackage(Packages.server.life);
importPackage(Packages.tools);
importPackage(Packages.server.events);

function start(ms) {
    try {
        ms.getPlayer().resetEnteredScript();
        ms.getPlayer().getClient().getSession().write(MaplePacketCreator.getClock(BalrogPQ.getSecondsLeft())); // 60 mins(1hr)
        BalrogPQ.spawnBalrog(1, ms.getPlayer());
    } catch(err) {
        ms.getPlayer().dropMessage(err);
    }
}