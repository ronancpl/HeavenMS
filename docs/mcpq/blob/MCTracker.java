/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server.partyquest.mcpq;

import org.slf4j.LoggerFactory;

/**
 * Logs various errors and also keeps data on Carnival PQ runs.
 * @author s4nta
 */
public class MCTracker {

    static org.slf4j.Logger log = LoggerFactory.getLogger(MCTracker.class);

    // TODO:
    // Add field-specific info
    // Add methods for calls from different files
    // Maybe write own version of FilePrinter?

    static final String PATH = "Reports/MCPQ.txt";

    public static void log(String msg) {
        System.out.println(msg);
        log.debug(msg);
    }
}  
