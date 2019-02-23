package client.command.commands.gm3;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;

public class GiveRpCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient client, String[] params) {
        MapleCharacter player = client.getPlayer();
        if (params.length < 2) {
            player.yellowMessage("Syntax: !giverp <playername> <gainrewardpoint>");
            return;
        }

        MapleCharacter victim = client.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
        if (victim != null) {
            victim.setRewardPoints(victim.getRewardPoints() + Integer.parseInt(params[1]));
            player.message("RP given. Player " + params[0] + " now has " + victim.getRewardPoints()
                    + " reward points." );
        } else {
            player.message("Player '" + params[0] + "' could not be found.");
        }
    }
}
