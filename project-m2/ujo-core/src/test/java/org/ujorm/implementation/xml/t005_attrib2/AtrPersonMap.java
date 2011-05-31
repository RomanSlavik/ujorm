/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.t005_attrib2;

import org.ujorm.UjoProperty;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.extensions.ListProperty;
import org.ujorm.UjoAction;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.map.MapUjo;
import static org.ujorm.UjoAction.*;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPersonMap extends MapUjo  {


    public static final Property<AtrPersonMap, String> NAME_ELEM = newProperty("name", String.class);
    @XmlAttribute
    public static final Property<AtrPersonMap, String> NAME_ATTR = newProperty("name", String.class);
    public static final ListProperty<AtrPersonMap, AtrPersonMap> CHILDS = newListProperty("child", AtrPersonMap.class);
    
    
    @SuppressWarnings("deprecation")
    public boolean readAuthorization(final UjoAction action, final UjoProperty property, final Object value) {
        
        switch(action.getType()) {
            case ACTION_XML_ELEMENT:
                return property!=NAME_ATTR;
            default:
                return super.readAuthorization(action, property, value);
        }
    }
    
}