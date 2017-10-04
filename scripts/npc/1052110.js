/* 
    Trash Can 2
    Kerning Subway	
*/

var status;
 
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
            cm.sendOk("Just a trash can sitting there.");
            cm.dispose();
        }
    }
}