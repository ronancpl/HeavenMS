package server.gachapon;

/**
*
* @author Alan (SharpAceX) - gachapon source classes stub & pirate equipment
* @author Ronan - parsed MapleSEA loots, thanks Vcoc for noticing somewhat unbalanced loots in NLC
* 
* MapleSEA-like loots thanks to AyumiLove - src: https://ayumilovemaple.wordpress.com/maplestory-gachapon-guide/
*/

public class NewLeafCity extends GachaponItems {

	@Override
	public int[] getCommonItems() {
		return new int[] {
                        
                        /* Scroll */
                        2040406, 2040408, 2040404, 2040411, 2040409, 2044405, 2040610, 2040607, 2040812, 2041039, 2041040, 2041034,
                        2041030, 2041037, 2043105, 2043304, 2040103, 2040605, 2040611, 2043004, 2043204, 2044204, 2044005, 2040521,
                        2040510, 2043304, 2040908, 2040904, 2040907, 2040809, 2040812, 2040014, 2040714, 2040712, 2044004, 2043705,
                        2044505, 2040519, 2040204, 2040104, 2040109, 2044704, 2040906, 2044304, 2043007, 2040307, 2040304, 2040309,
                        2040208, 2040209, 2044803,

                        /* Common equipment */
                        1102040, 1102086, 1082145, 1032027, 1082146, 1002395, 1002083, 1002392, 1002587, 1022047,

                        /* Warrior equipment */
                        1312002, 1432013, 1060030, 1422008, 1050022, 1050011, 1402013, 1402017, 1302012,

                        /* Mage equipment */
                        1002074, 1050029, 1040093, 1050056, 1050039, 1382008,

                        /* Bowman equipment */
                        1002159, 1061051, 1040023,

                        /* Thief equipment */
                        1061054, 1061106, 1002249, 1040084, 1060052, 1472054,
                        
                        /* Pirate equipment */
                        1002640, 1002643, 1002646, 1052125, 1052128, 1052131, 1072312, 1072315, 1072318, 1082207, 1082210, 1082213,
                        1482010, 1482011, 1002640, 1482012, 1492010, 1492011, 1492012
                };
	}

	@Override
	public int[] getUncommonItems() {
		return new int[] {2022284, 2040811, 2040815, 2040811, 1102041, 1102042, 1082149};
	}

	@Override
	public int[] getRareItems() {
		return new int[] {};
	}

}
