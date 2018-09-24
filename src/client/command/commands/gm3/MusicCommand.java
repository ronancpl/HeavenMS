/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2018 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command.commands.gm3;

import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import tools.MaplePacketCreator;

public class MusicCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        final String[] songs = {
                "Jukebox/Congratulation",
                "Bgm00/SleepyWood",
                "Bgm00/FloralLife",
                "Bgm00/GoPicnic",
                "Bgm00/Nightmare",
                "Bgm00/RestNPeace",
                "Bgm01/AncientMove",
                "Bgm01/MoonlightShadow",
                "Bgm01/WhereTheBarlogFrom",
                "Bgm01/CavaBien",
                "Bgm01/HighlandStar",
                "Bgm01/BadGuys",
                "Bgm02/MissingYou",
                "Bgm02/WhenTheMorningComes",
                "Bgm02/EvilEyes",
                "Bgm02/JungleBook",
                "Bgm02/AboveTheTreetops",
                "Bgm03/Subway",
                "Bgm03/Elfwood",
                "Bgm03/BlueSky",
                "Bgm03/Beachway",
                "Bgm03/SnowyVillage",
                "Bgm04/PlayWithMe",
                "Bgm04/WhiteChristmas",
                "Bgm04/UponTheSky",
                "Bgm04/ArabPirate",
                "Bgm04/Shinin'Harbor",
                "Bgm04/WarmRegard",
                "Bgm05/WolfWood",
                "Bgm05/DownToTheCave",
                "Bgm05/AbandonedMine",
                "Bgm05/MineQuest",
                "Bgm05/HellGate",
                "Bgm06/FinalFight",
                "Bgm06/WelcomeToTheHell",
                "Bgm06/ComeWithMe",
                "Bgm06/FlyingInABlueDream",
                "Bgm06/FantasticThinking",
                "Bgm07/WaltzForWork",
                "Bgm07/WhereverYouAre",
                "Bgm07/FunnyTimeMaker",
                "Bgm07/HighEnough",
                "Bgm07/Fantasia",
                "Bgm08/LetsMarch",
                "Bgm08/ForTheGlory",
                "Bgm08/FindingForest",
                "Bgm08/LetsHuntAliens",
                "Bgm08/PlotOfPixie",
                "Bgm09/DarkShadow",
                "Bgm09/TheyMenacingYou",
                "Bgm09/FairyTale",
                "Bgm09/FairyTalediffvers",
                "Bgm09/TimeAttack",
                "Bgm10/Timeless",
                "Bgm10/TimelessB",
                "Bgm10/BizarreTales",
                "Bgm10/TheWayGrotesque",
                "Bgm10/Eregos",
                "Bgm11/BlueWorld",
                "Bgm11/Aquarium",
                "Bgm11/ShiningSea",
                "Bgm11/DownTown",
                "Bgm11/DarkMountain",
                "Bgm12/AquaCave",
                "Bgm12/DeepSee",
                "Bgm12/WaterWay",
                "Bgm12/AcientRemain",
                "Bgm12/RuinCastle",
                "Bgm12/Dispute",
                "Bgm13/CokeTown",
                "Bgm13/Leafre",
                "Bgm13/Minar'sDream",
                "Bgm13/AcientForest",
                "Bgm13/TowerOfGoddess",
                "Bgm14/DragonLoad",
                "Bgm14/HonTale",
                "Bgm14/CaveOfHontale",
                "Bgm14/DragonNest",
                "Bgm14/Ariant",
                "Bgm14/HotDesert",
                "Bgm15/MureungHill",
                "Bgm15/MureungForest",
                "Bgm15/WhiteHerb",
                "Bgm15/Pirate",
                "Bgm15/SunsetDesert",
                "Bgm16/Duskofgod",
                "Bgm16/FightingPinkBeen",
                "Bgm16/Forgetfulness",
                "Bgm16/Remembrance",
                "Bgm16/Repentance",
                "Bgm16/TimeTemple",
                "Bgm17/MureungSchool1",
                "Bgm17/MureungSchool2",
                "Bgm17/MureungSchool3",
                "Bgm17/MureungSchool4",
                "Bgm18/BlackWing",
                "Bgm18/DrillHall",
                "Bgm18/QueensGarden",
                "Bgm18/RaindropFlower",
                "Bgm18/WolfAndSheep",
                "Bgm19/BambooGym",
                "Bgm19/CrystalCave",
                "Bgm19/MushCatle",
                "Bgm19/RienVillage",
                "Bgm19/SnowDrop",
                "Bgm20/GhostShip",
                "Bgm20/NetsPiramid",
                "Bgm20/UnderSubway",
                "Bgm21/2021year",
                "Bgm21/2099year",
                "Bgm21/2215year",
                "Bgm21/2230year",
                "Bgm21/2503year",
                "Bgm21/KerningSquare",
                "Bgm21/KerningSquareField",
                "Bgm21/KerningSquareSubway",
                "Bgm21/TeraForest",
                "BgmEvent/FunnyRabbit",
                "BgmEvent/FunnyRabbitFaster",
                "BgmEvent/wedding",
                "BgmEvent/weddingDance",
                "BgmEvent/wichTower",
                "BgmGL/amoria",
                "BgmGL/Amorianchallenge",
                "BgmGL/chapel",
                "BgmGL/cathedral",
                "BgmGL/Courtyard",
                "BgmGL/CrimsonwoodKeep",
                "BgmGL/CrimsonwoodKeepInterior",
                "BgmGL/GrandmastersGauntlet",
                "BgmGL/HauntedHouse",
                "BgmGL/NLChunt",
                "BgmGL/NLCtown",
                "BgmGL/NLCupbeat",
                "BgmGL/PartyQuestGL",
                "BgmGL/PhantomForest",
                "BgmJp/Feeling",
                "BgmJp/BizarreForest",
                "BgmJp/Hana",
                "BgmJp/Yume",
                "BgmJp/Bathroom",
                "BgmJp/BattleField",
                "BgmJp/FirstStepMaster",
                "BgmMY/Highland",
                "BgmMY/KualaLumpur",
                "BgmSG/BoatQuay_field",
                "BgmSG/BoatQuay_town",
                "BgmSG/CBD_field",
                "BgmSG/CBD_town",
                "BgmSG/Ghostship",
                "BgmUI/ShopBgm",
                "BgmUI/Title"
        };
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !music <song>");
            for (String s : songs) {
                player.yellowMessage(s);
            }
            return;
        }
        String song = joinStringFrom(params, 0);
        for (String s : songs) {
            if (s.equals(song)) {
                player.getMap().broadcastMessage(MaplePacketCreator.musicChange(s));
                player.yellowMessage("Now playing song " + song + ".");
                break;
            }
        }
        player.yellowMessage("Song not found, please enter a song below.");
        for (String s : songs) {
            player.yellowMessage(s);
        }
    }
}
