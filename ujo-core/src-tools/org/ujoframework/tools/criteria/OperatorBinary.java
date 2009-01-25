/*
 *  Copyright 2007-2008 Paul Ponec
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
   


package org.ujoframework.tools.criteria;

import org.ujoframework.Ujo;

/**
 * Binary operator
 * @author pavel
 */
public enum OperatorBinary {
    /** (e1 AND e2) */
    AND,
    /** (e1 OR e2) */
    OR,
    /** (e1 XOR e2) */
    XOR;

    /** Join two expressions. */ 
    public <UJO extends Ujo> Expression<UJO> join(final Expression<UJO> ex1, final Expression<UJO> ex2) {
        return ex1.join(this, ex2);
    }
         
}
