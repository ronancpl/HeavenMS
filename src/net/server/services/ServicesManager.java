/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
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
package net.server.services;

/**
 *
 * @author Ronan
 */
public class ServicesManager {
    
    private Service[] services;
    
    public ServicesManager(ServiceType serviceBundle) {
        Enum[] serviceTypes = serviceBundle.enumValues();
        
        services = new Service[serviceTypes.length];
        for (Enum type : serviceTypes) {
            services[type.ordinal()] = ((ServiceType) type).createService();
        }
    }
    
    public Service getAccess(ServiceType s) {
        return services[s.ordinal()];
    }
    
    public void shutdown() {
        for (int i = 0; i < services.length; i++) {
            services[i].dispose();
        }
        services = null;
    }
    
}
