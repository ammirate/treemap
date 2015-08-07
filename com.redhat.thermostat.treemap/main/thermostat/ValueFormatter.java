/*
 * Copyright 2012-2015 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package thermostat;

public class ValueFormatter {
    
    private double value;
    
    public ValueFormatter(double val) {
        this.value = val;
    }
    
    /**
     * This method returns the node value calculating it in bytes, KB or MB. 
     * 
     * i.e. if node's weight = 200 it returns: "200 bytes" <br>
     * if weight = 20152: "20.15 KB"  <br>
     * if weight = 2015248: "2.01 MB"  <br>
     * 
     * Note that float values are approximated to the second decimal digit.
     */
    public String format() {
        int KB = 1000;
        int MB = 1000000;
        String unit = "Bytes";
        
        if (value >= KB && value < MB) {
            value /= KB;
            unit = "KBytes";
        } else if (value >= MB) {
            value /= MB;
            unit = "MBytes";
        }
        // show 2 decimal digits 
        String formattedValue = String.format("%.2f", value);
        return formattedValue + " " + unit;
    }
}
