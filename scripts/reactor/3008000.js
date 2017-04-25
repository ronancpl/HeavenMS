/**
 * @author: Ronan
 * @reactor: Water Fountain
 * @map: 930000800 - Forest of Poison Haze - Outer Forest Exit
 * @func: Water Fountain
*/

function hit() {
	var players = rm.getMap().getAllPlayers().toArray();
        
        for(var i = 0; i < players.length; i++) {
                rm.giveCharacterExp(52000, players[i]);
        }
}

function act() {} //do nothing