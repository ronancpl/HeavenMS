importPackage(Packages.client); 
importPackage(Packages.server.maps); 

var status; 
var sel; 

function start() { 
    status = -1; 
    action(1, 0, 0); 
} 

function action(mode, type, selection) { 
    if (mode == -1) { 
        cm.dispose(); 
    } else { 
        if (mode == 0) { 
            cm.dispose(); 
            return; 
        } 
        if (mode == 1) 
            status++; 
        else 
            status--; 
            if (status == 0) { 
            if (cm.getLevel() < 20) { 
                cm.sendDimensionalMirror("#-1# There is no place for you to transport to from here."); 
                cm.dispose(); 
            } else { 
                var selStr = ""; 
                if (cm.getLevel() >= 20 && cm.getLevel() <= 30) { 
                    selStr += "#0# Ariant Coliseum"; 
                } 

                if (cm.getLevel() >= 25) { 
                    selStr += "#1# Mu Lung Dojo"; 
                } 

                if (cm.getLevel() >= 30 && cm.getLevel() <= 50) { 
                    selStr += "#2# Monster Carnival 1"; 
                } 

                if (cm.getLevel() >= 51 && cm.getLevel() <= 70) { 
                    selStr += "#3# Monster Carnival 2"; 
                } 

                if (cm.getLevel() >= 40) { 
                    selStr += "#5# Nett's Pyramid"; 
                } 

                if (cm.getLevel() >= 25 && cm.getLevel() <= 30) { 
                    selStr += "#6# Construction Site"; 
                } 
                cm.sendDimensionalMirror(selStr); 
            } 
        } else if (status == 1) { 
            cm.getPlayer().saveLocation("MIRROR"); 
            switch (selection) { 
                case 0: 
                    cm.warp(980010000, 3); 
                    break; 
                case 1: 
                    cm.warp(925020000, 0); 
                    break; 
                case 2: 
                    cm.warp(980000000, 3); 
                    break; 
                case 3: 
                    cm.warp(980030000, 3); 
                    break; 
                case 5: 
                    cm.warp(926010000); 
                    break; 
                case 6: 
                    cm.warp(910320000); 
                    break; 
            } 
            cm.dispose(); 
        } 
    } 
}  
