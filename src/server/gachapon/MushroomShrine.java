package server.gachapon;

/**
*
* @author Alan (SharpAceX) - gachapon source classes stub
* @author Ronan - parsed MapleSEA loots
* 
* MapleSEA-like loots thanks to AyumiLove - src: https://ayumilovemaple.wordpress.com/maplestory-gachapon-guide/
*/

public class MushroomShrine extends GachaponItems {

	@Override
	public int[] getCommonItems() {
		return new int [] {
                        
                        /* Scroll */
                        2040305, 2040306, 2040308, 2044604, 2041039, 2041037, 2041035, 2041034, 2041041, 2040608, 2040605, 2040604, 2040611,
                        2040610, 2040813, 2040808, 2043004, 2040017, 2040015, 2040011, 2040013, 2040405, 2040406, 2040410, 2040511, 2040509,
                        2040508, 2040519, 2040521, 2040108, 2040904, 2040908, 2043104, 2044104, 2043005, 2043004, 2043006, 2044004, 2044205,
                        2043304, 2040607, 2040715, 2040713, 2044305, 2044904,

                        /* Common equipment */
                        1102040, 1002392, 1432009, 1002393, 1002394, 1082147, 1082148, 1032028, 1002585, 1002586, 1432013, 1022047, 1322027,
                        1012056, 1432018,

                        /* Beginner equipment */
                        1072264, 1072262, 1072263,

                        /* Warrior equipment */
                        1060074, 1322002, 1002340, 1442004, 1402037, 1422008, 1050022,

                        /* Mage equipment */
                        1382037, 1060014, 1051026, 1050056, 1050029, 1051030, 1382036, 1372032, 1041015, 1382015, 1372008, 1382008,

                        /* Bowman equipment */
                        1452018, 1041068, 1462007,

                        /* Thief equipment */
                        1060052, 1472013, 1002180, 1002170, 1060073, 1060099,
                        
                        /* Pirate equipment */
                        1492004, 1492012, 1482009, 1072303, 1002637, 1052107, 1082189, 1052116, 1072309 

                };
	}

	@Override
	public int[] getUncommonItems() {
		return new int [] {2040811, 2040810, 2040815, 1102041, 1102042, 1082149};
	}

	@Override
	public int[] getRareItems() {
		return new int [] {1102084, 3010019};
	}

}
