/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml;

import java.util.Date;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.map.MapUjo;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class XmlUjoItem extends MapUjo  {
    
    public static final Property<XmlUjoItem,Long>    PRO_P0 = newProperty("P0", Long.class);
    public static final Property<XmlUjoItem,Integer> PRO_P1 = newProperty("P1", Integer.class);
    public static final Property<XmlUjoItem,String>  PRO_P2 = newProperty("P2", String.class);
    public static final Property<XmlUjoItem,Date>    PRO_P3 = newProperty("P3", Date.class);
    public static final Property<XmlUjoItem,Float>   PRO_P4 = newProperty("P4", Float.class);
    
}