/*
 *  Copyright 2011-2011 Pavel Ponec
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ujorm.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;

/**
 * The Immutable and Serializable UjoProperty collection.
 * @author Pavel Ponec
 */
public class PropertyGroup<UJO extends Ujo> implements Iterable<UjoProperty<UJO,?>>, Serializable {
    static final long serialVersionUID = 1L;

    private final Class<UJO> baseClass;
    private final String[] tProperties;
    private transient List<UjoProperty<UJO,?>> properties;

    /**
     * Constructor
     * @param baseClass Not null base class for all properties
     * @param properties Property array
     * @see #newInstance(java.lang.Class, org.ujorm.UjoProperty<T,?>[]) 
     */
    public PropertyGroup(Class<UJO> baseClass, UjoProperty<UJO, ?> ... properties) {
        if (baseClass==null) {
            throw new IllegalArgumentException("The baseClass must be defined");
        }
        this.baseClass = baseClass;
        this.properties = Arrays.asList(properties);
        this.tProperties = new String[properties.length];

        for (int i = properties.length - 1; i >= 0; --i) {
            tProperties[i] = properties[i].getName();
        }
    }

    /** Get Class */
    public Class<UJO> getBaseClass() {
        return baseClass;
    }

    /** Create Properties */
    private List<UjoProperty<UJO,?>> getProperties() {
        final UjoPropertyList propertyList = UjoManager.getInstance().readProperties(baseClass);

        if (properties==null) {
            final List<UjoProperty<UJO,?>> ps = new ArrayList<UjoProperty<UJO,?>>(tProperties.length);
            for (int i = 0; i < tProperties.length; i++) {
                ps.add(propertyList.findIndirect(tProperties[i], true));
            }
            properties = ps;
        }
        return properties;
    }

    /** Get The First Properties */
    public UjoProperty<UJO,?> getFirstProperty() {
        return get(0);
    }

    /** Get The Last Properties */
    public UjoProperty<UJO,?> getLastProperty() {
        return get(tProperties.length-1);
    }

    /** Get First Properties */
    public UjoProperty<UJO,?> get(int i) {
        return getProperties().get(i);
    }


    /** Size */
    public int size() {
        return tProperties.length;
    }

    /** Test collection if it contains a property parameter  */
    @SuppressWarnings("element-type-mismatch")
    public boolean contains(Object property) {
        return getProperties().contains(property);
    }

    /** Create Property Interator */
    public Iterator<UjoProperty<UJO, ?>> iterator() {
        return getProperties().iterator();
    }

    /** Convert Properties to an Array */
    public UjoProperty[] toArray() {
        return getProperties().toArray(new  UjoProperty[tProperties.length]);
    }

    /** Returns the property names */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(32);
        for (String p : tProperties) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(p);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PropertyGroup) {
            PropertyGroup<UJO> o = (PropertyGroup<UJO>) obj;
            if (this.size()!=o.size()) {
                return false;
            }
            if (!this.getBaseClass().equals(o.getBaseClass())) {
                return false;
            }
            for (int i = size()-1; i>=0; --i) {
                UjoProperty p1 = this.get(i);
                UjoProperty p2 = o.get(i);
                if (!p1.equals(p2)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.baseClass != null ? this.baseClass.hashCode() : 0);
        hash = 83 * hash + Arrays.deepHashCode(this.tProperties);
        return hash;
    }
    
    // -------------- STATIC METHOD(S) --------------

    /** Create new Instance */
    public static <T extends Ujo> PropertyGroup<T> newInstance(Class<T> baseClass, UjoProperty<T, ?> ... properties) {
        return new PropertyGroup<T>(baseClass, properties);
    }

}