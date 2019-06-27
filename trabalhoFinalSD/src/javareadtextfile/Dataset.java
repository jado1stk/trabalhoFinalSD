package javareadtextfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Este é uma classe Wrapper, que encapsula o conjunto de dados em um HaspMap.
 *
 * @author Sobjak
 */
public class Dataset extends HashMap<String, ArrayList> {
    
    public String[] getColumnNamesArray() {
        String[] names = new String[keySet().size()];
        return keySet().toArray(names);
    }
    
    public Set getColumnNames() {
        return keySet();
    }

    public HashMap<String, ArrayList> getData() {
        return this;
    }
}
