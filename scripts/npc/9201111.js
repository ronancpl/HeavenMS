/* @Author SharpAceX
*/

function start() {
        if (cm.getPlayer().getMap().getId() == 610030500) {
                cm.sendOk("You're about to get wet and do what a Pirate does best--dig for booty! Be careful--that water beneath is known as Heavy Water, and it's so dense that I doubt you could swim through it! You'll have to make your way around....The relic you seek is known as the Forbidden Gun, which is an ancient weapon of the finest Pirate to grace the shores of Masteria--Steel Fist Jack! It's been buried in one of the many treasure chests you'll find below the sea. It won't be easy...Pirates were known for burying things in the most unlikely places, so dig deep and keep your guard up. There are sharks and much worse in those waters!");
                cm.dispose();
        } else if (cm.getPlayer().getMap().getId() == 610030000) {
                cm.sendOk("Long ago, a strange warrior washed upon the shores of Masteria. This being claimed to be a member of a mysterious band of warriors that used claw-like weapons and projectile-based artillery to defeat foes. Known as 'Steel Fist Jack', his cunning and trickery in battle was devilishly effective. He eventually built a boat and left the Keep in search of his former crew and captain.");
                cm.dispose();
        }
}