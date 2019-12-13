package server.gachapon;

/**
*
* @author Alan (SharpAceX) - gachapon source classes stub & pirate equipment
* @author Ronan - parsed MapleSEA loots
* 
* MapleSEA-like loots thanks to AyumiLove - src: https://ayumilovemaple.wordpress.com/maplestory-gachapon-guide/
*/

public class KerningCity extends GachaponItems {

	@Override
	public int[] getCommonItems() {
		return new int [] {
                        
                        /* Scroll */
                        2041016, 2043302, 2040902, 2044804, 2044906,

                        /* Useable drop */
                        2000004, 2000005, 2022025, 2022027,

                        /* Common equipment */
                        1442013, 1432009, 1322021, 1050018, 1002392, 1002394, 1442004, 1372002, 1002418, 1002033, 1092008,
                        1082148, 1062001, 1302017, 1032023, 1102013, 1102040, 1002041, 1002097,

                        /* Warrior equipment */
                        1332026, 1051010, 1432001, 1422005, 1332019, 1302010, 1002056, 1060011, 1322011, 1432004, 1002028,
                        1051000, 1442007, 1302002,

                        /* Magician equipment */
                        1002037, 1002034, 1082020, 1050039, 1372000, 1002215, 1051034, 1040019, 1061034, 1382003, 1382006,
                        1050025,

                        /* Bowman equipment */
                        1002118, 1061081, 1452011, 1462012, 1452006, 1452007,

                        /* Thief equipment */
                        1472010, 1472029, 1041048, 1041095, 1060031, 1061033, 1041049, 1472011, 1040096, 1472033, 1332026,
                        1051006, 1082074, 1472025, 1061106, 1040084, 1332015, 1472000, 1332019, 1002183, 1002209, 1092020,
                        1332029, 1092019, 1061099, 1060106, 1040032, 1040059, 1332003, 1040060, 1060046, 1472005, 1332027,
                        
                        /* Pirate equipment */
                        1082192, 1072288, 1492003, 1052113, 1052104, 
                        1492002, 1052095, 1492001, 1002613, 1492004
                };
	}

	@Override
	public int[] getUncommonItems() {
		return new int[] {2040805, 1082149, 1102041};
	}

	@Override
	public int[] getRareItems() {
		return new int[] {};
	}

}
