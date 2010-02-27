/*
 *  Copyright 2007-2010 Pavel Ponec
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

package org.ujoframework.swing;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoActionImpl;
import org.ujoframework.core.UjoCoder;
import org.ujoframework.core.UjoManager;
import org.ujoframework.UjoAction;

/**
 * An implementation of TableModel for a Single Ujo object.
 * 
 * @author Pavel Ponec
 */
public class SingleUjoTabModel extends UjoTableModel<UjoPropertyRow> {
    
    /** Property row */
    public static final UjoPropertyRow ROWS = null;
    
    /**
     * Creates a new instance of SingleUjoTabModel
     */
    public SingleUjoTabModel(Ujo content) {
        this(content, UjoManager.getInstance().readProperties(UjoPropertyRow.class).toArray() );
    }
    
    /**
     * Creates a new instance of SingleUjoTabModel
     */
    public SingleUjoTabModel(Ujo content, UjoProperty ... columns) {
        super(columns);
        rows = UjoManager.getInstance().createPropertyList(content, new UjoActionImpl(UjoAction.ACTION_TABLE_SHOW, this));
    }
    
    /** Only Value is editable. */
    @Override
    public boolean isCellEditable(int rowIndex, UjoProperty column) {
        final boolean result 
        =  column==UjoPropertyRow.P_VALUE
        || column==UjoPropertyRow.P_TEXT
        ;
        return result;
    }
    
    /** Set a value to a cell of table model. */
    @Override
    @SuppressWarnings("static-access")
    public void setValueAt(Object value, int rowIndex, UjoProperty column) {
        if (column==ROWS.P_VALUE
        && !column.getType().equals(String.class)
        &&  value instanceof String
        ){
            UjoPropertyRow row = getRow(rowIndex);
            row.writeValueString(column, (String) value, null, new UjoActionImpl(this));
        } else {
            super.setValueAt(value, rowIndex, column);
        }
    }
    
    /** Get Value in a String format. */
    @Override
    @SuppressWarnings("static-access")
    public Object getValueAt(int rowIndex, UjoProperty column) {
        final Object result 
        = (column==ROWS.P_VALUE)
        ?  getRow(rowIndex).readValueString(column, new UjoActionImpl(this))
        : (column==ROWS.P_DEFAULT)
        ?  getCoder().encodeValue(getRow(rowIndex).getProperty().getDefault(), false)
        : super.getValueAt(rowIndex, column)
        ;
        return result;
    }
    
    /** Returns an UjoCoder */
    protected UjoCoder getCoder() {
        return UjoManager.getInstance().getCoder();
    }    
    
}