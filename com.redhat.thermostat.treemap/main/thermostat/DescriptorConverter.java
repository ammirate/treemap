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

import java.util.HashMap;
import java.util.Map;

/**
 * The JVM uses internal names for classes and fields (like "<code>[I</code>").
 * This class helps to decode them.
 *
 * @see MethodDescriptorConverter
 */
public class DescriptorConverter {

    private static final Map<Character, String> lookupTable = new HashMap<>();

    static {
        lookupTable.put('Z', "boolean");
        lookupTable.put('B', "byte");
        lookupTable.put('C', "char");
        lookupTable.put('S', "short");
        lookupTable.put('I', "int");
        lookupTable.put('J', "long");
        lookupTable.put('F', "float");
        lookupTable.put('D', "double");
    }

    public static String toJavaType(String fieldDescriptor) {
        return toJavaType(fieldDescriptor, lookupTable);
    }

    static String toJavaType(String fieldDescriptor, Map<Character, String> lookupTable) {
        StringBuilder result = new StringBuilder();

        int arrayDimensions = 0;
        int lastLocation = 0;
        int i = -1;
        while ((i = fieldDescriptor.indexOf('[', lastLocation)) != -1) {
            arrayDimensions++;
            lastLocation = i + 1;
        }

        char indicator = fieldDescriptor.charAt(lastLocation);

        if (lookupTable.get(indicator) != null) {
            result.append(lookupTable.get(indicator));
        } else if (indicator == 'L') {
            String internalClassName = fieldDescriptor.substring(lastLocation + 1, fieldDescriptor.length() - 1);
            String commonClassName = internalClassName.replace('/', '.');
            result.append(commonClassName);
        } else {
            result.append(fieldDescriptor);
        }
        String dim = "";
        for (int k = 0; k < arrayDimensions; k++) {
            dim += "[]";
        }
        result.append(dim);

        return result.toString();
    }
}

