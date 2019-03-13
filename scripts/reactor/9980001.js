function act() {
    rm.dispelAllMonsters(parseInt(rm.getReactor().getName().substring(1,2)), parseInt(rm.getReactor().getName().substring(0,1)));
}