/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 
package constants;
 
/*
 * Courtesy of GabrielSin (gabrielsin@playellin.net)
 * Ellin
 * MapleStory Server
 * CharsetConstants
 */
 
public class CharsetConstants {
   
    public static MapleLanguageType MAPLE_TYPE = MapleLanguageType.LANGUAGE_PT_BR;
   
    public enum MapleLanguageType {
        LANGUAGE_PT_BR(1, "ISO-8859-1"),
        LANGUAGE_US(2, "US-ASCII");
        final byte type;
        final String ascii;
 
        private MapleLanguageType(int type, String ascii) {
            this.type = (byte) type;
            this.ascii = ascii;
        }
 
        public String getAscii() {
            return ascii;
        }
 
        public byte getType() {
            return type;
        }
 
        public static MapleLanguageType getByType(byte type) {
            for (MapleLanguageType l : MapleLanguageType.values()) {
                if (l.getType() == type) {
                    return l;
                }
            }
            return LANGUAGE_PT_BR;
        }
    }
}