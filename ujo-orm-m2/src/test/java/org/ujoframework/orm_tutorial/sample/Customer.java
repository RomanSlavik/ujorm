/*
 *  Copyright 2009-2010 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujoframework.orm_tutorial.sample;

import java.util.Date;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.OrmTable;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
public class Customer extends OrmTable<Customer> {

    /** Unique key */
    @Column(pk = true)
    public static final UjoProperty<Customer, Long> ID = newProperty(Long.class);
    /** Personal Numbr */
    public static final UjoProperty<Customer, Integer> PIN = newProperty(Integer.class);
    /** Firstname */
    @Column(length=50, uniqueIndex="idx_customer_full_name")
    public static final UjoProperty<Customer, String> FIRSTNAME = newProperty(String.class);
    /** Lastname */
    @Column(length=50, uniqueIndex="idx_customer_full_name")
    public static final UjoProperty<Customer, String> LASTNAME = newProperty(String.class);
    /** Date of creation */
    public static final UjoProperty<Customer, Date> CREATED = newProperty(Date.class);

    // --- An optional implementation of commonly used setters and getters ---
    public Long getId() {
        return get(ID);
    }

    public void setId(Long id) {
        set(ID, id);
    }

    public Integer getUsrId() {
        return get(PIN);
    }

    public void setUsrId(Integer usrId) {
        set(PIN, usrId);
    }

}