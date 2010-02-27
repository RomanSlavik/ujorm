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

package org.ujoframework.orm.annot;
import java.lang.annotation.*;
import org.ujoframework.orm.SqlDialect;

/** 
 * Use the annotation to mark a UjoProperty static field like XML Attribute.
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.TYPE)
public @interface Db {

    /** Default name of table schema is copied into table models if thay are empty. */
    String schema() default "";
    /** SQL dialect by a DB Vendor. */
    Class<? extends SqlDialect> dialect();
    /** <a href="http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface" target="_blank">JNDI</a>
     * (java naming and directory interface) connection string
     */
    String jndi() default "";
    /** JDBC Url */
    String jdbcUrl() default "";
    /** JDBC Driver */
    String jdbcDriver() default "";
    /** Connection User */
    String user()     default "";
    /** Connection password */
    String password() default "";
    /** The sequencer class for tables of the current database.
     * A value can be a subtype of 'org.ujoframework.orm.UjoSequencer' with one-parameter constructor type of MetaTable.
     * If the NULL value is specified the then a default sequencer 'UjoSequencer' will be used. */
    Class sequencer() default org.ujoframework.orm.UjoSequencer.class;


}