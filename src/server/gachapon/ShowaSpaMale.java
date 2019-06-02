package server.gachapon;

/**
*
* @author Alan (SharpAceX) - gachapon source classes stub
* @author Ronan - parsed MapleSEA loots
* 
* MapleSEA-like loots src: http://maplesecrets.blogspot.com/2011/05/gachapon-showa-towns-sauna.html
*/

public class ShowaSpaMale extends GachaponItems {

	@Override
	public int[] getCommonItems() {
		return new int [] {
                    
                        /* Scroll */
                        2048005, 2048002, 2043202, 2044602, 2043214, 2041307, 2041035, 2044104, 2044505, 2044305, 2043304, 2044902,
                        2044901, 2044811, 2044903, 2044804,

                        /* Useable drop */
                        2022016, 2000005, 2022025, 2022027,

                        /* Common equipment */
                        1332020, 1312004, 1332032, 1322023, 1322026, 1322022, 1322012, 1302014, 1302049, 1302017, 1332007, 1432009,
                        1432016, 1432017, 1432009, 1402013, 1402044, 1442014, 1442017, 1442016, 1442025, 1002418, 1082178, 1082179,
                        1082148, 1032027, 1032032, 1102028, 1102086,

                        /* Common setup */
                        3010073, 3010111,

                        /* Warrior equipment */
                        1412005, 1402048, 1402049, 1322011, 1302003, 1302004, 1302008,

                        /* Magician equipment */
                        1372000, 1372009, 1372001, 1372011, 1382006, 1382014,

                        /* Bowman equipment */
                        1452018, 1452006, 1452008, 1452005, 1462002, 1462007, 1462003, 1002169,

                        /* Thief equipment */
                        1472023, 1332012, 1332017, 1332022, 1332006, 1332029, 1040097,

                        /* Pirate equipment */
                        1052107, 1082204, 1072318, 1002637, 1482009, 1492007
                        
                };
	}

	@Override
	public int[] getUncommonItems() {
		return new int [] {2040916, 1102042};
	}

	@Override
	public int[] getRareItems() {
		return new int [] {};
	}

}
