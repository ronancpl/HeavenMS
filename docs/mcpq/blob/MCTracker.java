/*
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
