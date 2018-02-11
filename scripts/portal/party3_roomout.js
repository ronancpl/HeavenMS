function enter(pi) {
        var exitPortal = 0;
        
        switch(pi.getMapId()) {
            case 920010200:
                exitPortal = 4;
                break;

            case 920010300:
                exitPortal = 12;
                break;

            case 920010400:
                exitPortal = 5;
                break;

            case 920010500:
                exitPortal = 13;
                break;

            case 920010600:
                exitPortal = 15;
                break;

            case 920010700:
                exitPortal = 14;
                break;
                
            case 920011000:
                exitPortal = 16;
                break;
        }

        pi.playPortalSound(); pi.warp(920010100, exitPortal);
        return true;
}