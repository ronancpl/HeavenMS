/**
 * @author: Ronan
 * @event: Jail
*/

function enter(pi) {
        var jailedTime = pi.getJailTimeLeft();
    
        if(jailedTime <= 0) {
                pi.playPortalSound(); pi.warp(300000010,"in01");
                return true;
        }
        else {
                var seconds = Math.floor(jailedTime / 1000) % 60 ;
                var minutes = (Math.floor(jailedTime / (1000*60)) % 60);
                var hours   = (Math.floor(jailedTime / (1000*60*60)) % 24);
            
                pi.playerMessage(5, "You have been caught in bad behaviour by the Maple POLICE. You've got to stay here for " + hours + " hours " + minutes + " minutes " + seconds + " seconds yet.");
                return false;
        }
}