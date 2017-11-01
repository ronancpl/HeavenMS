function enter(pi) {   player = pi.getPlayer();
   if (player.getMCPQField() != null) {
       player.getMCPQField().onRevive(player);
   } else {
       pi.warp(980000000);
   }
   return true;
}