package dropspider;

import java.util.LinkedList;

public class Errors {
    
    public String mobName;
    public LinkedList<String> wrong = new LinkedList<>();
    
    public String createErrorLog() {
        StringBuilder sb = new StringBuilder();
        
        for (String w : wrong) {
            sb.append(mobName).append(" : ").append(w).append("\r\n");
        }
        
        return sb.toString();
    }
}
