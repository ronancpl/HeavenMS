/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
package server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Ronan
 */
public class ThreadManager {
    private static ThreadManager instance = new ThreadManager();
    
    public static ThreadManager getInstance() {
        return instance;
    }
    
    private ThreadPoolExecutor tpe;
    
    private ThreadManager() {}
    
    private class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Thread t = new Thread(r);
            t.start();
        }

    }
    
    public void newTask(Runnable r) {
        tpe.execute(r);
    }
    
    public void start() {
        RejectedExecutionHandler reh = new RejectedExecutionHandlerImpl();
        ThreadFactory tf = Executors.defaultThreadFactory();
        
        tpe = new ThreadPoolExecutor(20, 1000, 77, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(50), tf, reh);
    }
    
    public void stop() {
        tpe.shutdown();
        try {
            tpe.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException ie) {}
    }
    
}
