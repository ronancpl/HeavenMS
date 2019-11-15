package constants.game;
 
/**
 * @brief ScriptableNPCConstants
 * @author GabrielSin <gabrielsin@playellin.net>
 * @date   16/09/2018
 * 
 * Adaptations to use Pair and Set, in order to suit a one-packet marshall.
 * Adapted by Ronan
 */

import java.util.HashSet;
import java.util.Set;
import tools.Pair;

public class ScriptableNPCConstants {
    
    public static final Set<Pair<Integer, String>> SCRIPTABLE_NPCS = new HashSet<Pair<Integer, String>>(){{
        //add(new Pair<>(9200000, "Cody"));
        add(new Pair<>(9001105, "Grandpa Moon Bunny"));
    }};
    
}
 