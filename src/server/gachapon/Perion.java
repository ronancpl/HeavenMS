package server.gachapon;

/**
*
* @author Alan (SharpAceX) - gachapon source classes stub & pirate equipment
* @author Ronan - parsed MapleSEA loots
* 
* MapleSEA-like loots thanks to AyumiLove - src: https://ayumilovemaple.wordpress.com/maplestory-gachapon-guide/
*/

public class Perion extends GachaponItems {

	@Override
	public int[] getCommonItems() {
		return new int [] {
                        
                        /* Scrolls */
                        2044907, 2044802,
                        
                        /* Useable drop */
                        2000004, 2000005,

                        /* Common equipment */
                        1402010, 1302022, 1002060, 1322021, 1082147, 1002006, 1002026, 1002392, 1322025, 1322027, 1102000, 1082150,
                        1332020, 1322007, 1302021, 1002395, 1082148, 1322012, 1302017, 1322010, 1032000, 1102013, 1002097,

                        /* Warrior equipment */
                        1322020, 1312007, 1312008, 1302004, 1312006, 1082036, 1082117, 1061088, 1302008, 1422005, 1002048, 1061087,
                        1302018, 1322017, 1422001, 1040103, 1060077, 1002022, 1002050, 1442000, 1432030, 1402037, 1092002, 1041092,
                        1050006, 1432004, 1061019, 1432000, 1060009, 1051000, 1002021, 1322014, 1432005,

                        /* Magician equipment */
                        1051032, 1040018, 1051027, 1372007, 1050049, 1002036, 1382012, 1002217, 1051033, 1382006, 1050048,

                        /* Bowman equipment */
                        1061061, 1060062, 1040075, 1462013, 1041065, 1452006,
                        
                        /* Thief equipment */
                        1040095, 1060084, 1002182, 1041049, 1002247, 1332024, 1332009, 1060024, 1332015, 1041060, 1061032, 1041074,
                        1041003, 1332016, 1472020, 1332003, 1041059,

                        /* Pirate equipment */
                        1482001, 1492002, 1052113, 1002616, 1072294, 1492004, 1482006, 1082192, 1082189, 1082195
                };
	}

	@Override
	public int[] getUncommonItems() {
		return new int [] {1082149, 1002391, 1002419, 1102041};
	}

	@Override
	public int[] getRareItems() {
		return new int [] {};
	}

}
