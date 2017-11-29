/**
 * @author: Ronan
 * @reactor: Spine
 * @map: 930000200 - Forest of Poison Haze - Deteriorated Forest
 * @func: Water Fountain
*/

function act() {
    if(rm.getReactor().getState() == 4) {
        rm.getEventInstance().showClearEffect(rm.getMap().getId());
    }
}