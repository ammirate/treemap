package com.redhat.thermostat.treemap.impl;

/**
 * This interface allow to implement the Decorator Pattern for an object.
 */
public interface Decorator {
    
    /**
     * Add a new info, as <key, value> pair to the Decorator object.
     * 
     * @param key the key that identified the information.
     * @param value the value of the information to store.
     * @return the old value if present for the given key, else null.
     */
    public String addInfo(String key, String value);
    
    
    /**
     * Retrieve the information identifiable by the given key.
     * 
     * @param key the key to get the desired information
     * @return a String object containing the information for the given key. 
     * It can be null.
     */
    public String getInfo(String key);
}
