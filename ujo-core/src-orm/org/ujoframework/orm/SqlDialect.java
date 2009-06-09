/*
 *  Copyright 2009 Paul Ponec
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
package org.ujoframework.orm;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaPKey;
import org.ujoframework.orm.metaModel.MetaTable;
import org.ujoframework.orm.metaModel.MetaView;
import org.ujoframework.criterion.ValueCriterion;
import org.ujoframework.criterion.Operator;

/**
 * SQL dialect API
 * @author Pavel Ponec
 */
@SuppressWarnings("unchecked")
abstract public class SqlDialect {

    /** The table key for a common sequence emulator. */
    public static final String COMMON_SEQ_TABLE_KEY = "<ALL>";

    protected OrmHandler ormHandler;

    /** Set the OrmHandler - for internal use only. */
    public void setHandler(OrmHandler ormHandler) {
        this.ormHandler = ormHandler;
    }

    /** Returns a default JDBC Driver */
    abstract public String getJdbcUrl();

    /** Returns a JDBC Driver */
    abstract public String getJdbcDriver();

    /** Print SQL 'CREATE SCHEMA' */
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("CREATE SCHEMA IF NOT EXISTS ");
        out.append(schema);
        return out;
    }

    /** Print SQL 'SET SCHEMA'. The method is not used yet. */
    @Deprecated
    public Appendable printDefaultSchema(String schema, Appendable out) throws IOException {
        out.append("SET SCHEMA ");
        out.append(schema);
        return out;
    }

    /** Print a full SQL table name by sample: SCHEMA.TABLE  */
    public void printFullName(final MetaTable table, final Appendable out) throws IOException {
        final String tableSchema = MetaTable.SCHEMA.of(table);
        final String tableName = MetaTable.NAME.of(table);

        if (isValid(tableSchema)) {
            out.append(tableSchema);
            out.append('.');
        }
        out.append(tableName);
    }

    /** Print a full SQL table alias name by sample: SCHEMA.TABLE ALIAS */
    public void printTableAlias(final MetaTable table, final Appendable out) throws IOException {
        printFullName(table, out);
        out.append(' ');
        out.append(table.getAlias());
    }


    /** Print a full SQL column alias name by sample: TABLE_ALIAS.COLUMN */
    public Appendable printColumnAlias(final MetaColumn column, final Appendable out) throws IOException {
        final MetaTable table = MetaColumn.TABLE.of(column);

        //printColumnAlias(table, out);
        out.append(table.getAlias());
        out.append('.');
        out.append(MetaColumn.NAME.of(column));
        
        return out;
    }

    /** Print a SQL sript to create table */
    public Appendable printTable(MetaTable table, Appendable out) throws IOException {
        out.append("CREATE TABLE ");
        printFullName(table, out);
        String separator = "\n\t( ";
        for (MetaColumn column : MetaTable.COLUMNS.getList(table)) {
            out.append(separator);
            separator = "\n\t, ";

            if (column.isForeignKey()) {
                printFKColumnsDeclaration(column, out);
            } else {
                printColumnDeclaration(column, null, out);
            }
        }
        out.append("\n\t)");
        return out;
    }

    /** Print foreign key */
    public Appendable printForeignKey(MetaTable table, Appendable out) throws IOException {
        for (MetaColumn column : MetaTable.COLUMNS.getList(table)) {
            if (column.isForeignKey()) {
                printForeignKey(column, table, out);
            }
        }
        return out;
    }

    /** Print foreign key for the parameter column */
    public Appendable printForeignKey(MetaColumn column, MetaTable table, Appendable out) throws IOException {
        final UjoProperty property = column.getProperty();
        final MetaTable foreignTable = ormHandler.findTableModel(property.getType());
        MetaPKey foreignKeys = MetaTable.PK.of(foreignTable);

        out.append("ALTER TABLE ");
        printFullName(table, out);
        out.append("\n\tADD FOREIGN KEY");

        List<MetaColumn> columns = MetaPKey.COLUMNS.of(foreignKeys);
        int columnsSize = columns.size();

        for (int i=0; i<columnsSize; ++i) {
            out.append(i==0 ? "(" : ", ");
            final String name = column.getForeignColumnName(i);
            out.append(name);
        }

        out.append(")\n\tREFERENCES ");
        printFullName(foreignTable, out);
        String separator = "(";

        for (MetaColumn fkColumn : MetaPKey.COLUMNS.of(foreignKeys)) {
            out.append(separator);
            separator = ", ";
            out.append(MetaColumn.NAME.of(fkColumn));
        }

        out.append(")");
        //out.append("\n\tON DELETE CASCADE");
        return out;
    }

    /**
     *  Print a SQL to create column
     * @param column Database Column
     * @param aName The name parameter is not mandatory, in case a null value the column name is used.
     * @throws java.io.IOException
     */
    public Appendable printColumnDeclaration(MetaColumn column, String aName, Appendable out) throws IOException {

        String name = aName!=null ? aName : MetaColumn.NAME.of(column);
        out.append(name);
        out.append(' ');
        out.append(MetaColumn.DB_TYPE.of(column).name());

        if (!MetaColumn.MAX_LENGTH.isDefault(column)) {
            out.append("(" + MetaColumn.MAX_LENGTH.of(column));
            if (!MetaColumn.PRECISION.isDefault(column)) {
                out.append(", " + MetaColumn.PRECISION.of(column));
            }
            out.append(")");
        }
        if (!MetaColumn.MANDATORY.isDefault(column)) {
            out.append(" NOT NULL");
        }
        if (MetaColumn.PRIMARY_KEY.of(column) && aName == null) {
            out.append(" PRIMARY KEY");
        }
        return out;
    }

    /** Print a SQL to create foreign keys. */
    public Appendable printFKColumnsDeclaration(MetaColumn column, Appendable out) throws IOException {

        List<MetaColumn> columns = column.getForeignColumns();

        for (int i=0; i<columns.size(); ++i) {
            MetaColumn col = columns.get(i);
            out.append(i==0 ? "" : "\n\t, ");
            String name = column.getForeignColumnName(i);
            printColumnDeclaration(col, name, out);
        }
        return out;
    }

    /** Print an SQL INSERT statement.  */
    public Appendable printInsert(OrmUjo bo, Appendable out) throws IOException {

        MetaTable table = ormHandler.findTableModel((Class) bo.getClass());
        StringBuilder values = new StringBuilder();

        out.append("INSERT INTO ");
        printFullName(table, out);
        out.append(" (");

        printTableColumns(MetaTable.COLUMNS.getList(table), values, out);

        out.append(") VALUES (");
        out.append(values);
        out.append(")");

        return out;
    }

    /** Print an SQL UPDATE statement.  */
    public Appendable printUpdate
        ( MetaTable table
        , List<MetaColumn> changedColumns
        , CriterionDecoder decoder
        , Appendable out
        ) throws IOException
    {
        out.append("UPDATE ");
        printTableAlias(table, out);
        out.append("\n\tSET ");

        for (int i=0; i<changedColumns.size(); i++) {
            MetaColumn ormColumn = changedColumns.get(i);
            if (ormColumn.isPrimaryKey()) {
                throw new IllegalStateException("Primary key can not be changed: " + ormColumn);
            }
            out.append(i==0 ? "" :  ", ");
            out.append(MetaColumn.NAME.of(ormColumn));
            out.append("=? ");
        }
        out.append("\n\tWHERE ");
        out.append(decoder.getWhere());
        return out;
    }

    /** Print an SQL DELETE statement. */
    public Appendable printDelete
        ( MetaTable table
        , CriterionDecoder decoder
        , Appendable out
        ) throws IOException
    {
        out.append("DELETE FROM ");
        printTableAlias(table, out);
        out.append(" WHERE ");
        out.append(decoder.getWhere());

        return out;
    }

    /** Returns an SQL criterion template. */
    @SuppressWarnings("unchecked")
    public String getCriterionTemplate(ValueCriterion crit) {

        switch (crit.getOperator()) {
            case EQ:
                return "{0}={1}";
            case NOT_EQ:
                return "{0}<>{1}";
            case GT:
                return "{0}>{1}";
            case GE:
                return "{0}>={1}";
            case LT:
                return "{0}<{1}";
            case LE:
                return "{0}<={1}";
            case EQUALS_CASE_INSENSITIVE:
                return "UPPER({0})={1}";
            case STARTS_CASE_INSENSITIVE:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS_CASE_INSENSITIVE:
                return "UPPER({0}) LIKE {1}";
            case STARTS:
            case ENDS:
            case CONTAINS:
                return "{0} LIKE {1}";
            case X_FIXED:
                return crit.evaluate(null)
                    ? "true"  // "1=1"
                    : "false" // "1=0"
                    ;
            case REGEXP: 
            case NOT_REGEXP:
            default:
                throw new UnsupportedOperationException("Unsupported: " + crit.getOperator());
        }
    }

    /**
     * Print table columns
     * @param columns List of tablel columns
     * @param values Print columns include alias.
     * @param out Table columns output.
     * @throws java.io.IOException
     */
    public void printTableColumns(List<MetaColumn> columns, Appendable values, Appendable out) throws IOException {
        String separator = "";
        boolean select = values==null; // SELECT
        for (MetaColumn column : columns) {
            if (column.isForeignKey()) {
                for (int i = 0; i < column.getForeignColumns().size(); ++i) {
                    out.append(separator);
                    if (select) {
                        out.append(MetaColumn.TABLE.of(column).getAlias());
                        out.append('.');
                    }
                    out.append(column.getForeignColumnName(i));
                    if (values != null) {
                        values.append(separator);
                        values.append("?");
                    }
                    separator = ", ";
                }
            } else if (column.isColumn()) {
                out.append(separator);
                if (select) {
                    printColumnAlias(column, out);
                } else {
                    out.append(MetaColumn.NAME.of(column));
                }
                if (values != null) {
                    values.append(separator);
                    values.append("?");
                }
                separator = ", ";
            }
        }
    }


    /** Print a conditon phrase by the criterion.
     * @return A value criterion to assign into the SQL query.
     */
    public ValueCriterion printCriterion(ValueCriterion crit, Appendable out) throws IOException {
        Operator operator = crit.getOperator();
        UjoProperty property = crit.getLeftNode();
        Object right = crit.getRightNode();

        MetaColumn column = (MetaColumn) ormHandler.findColumnModel(property);

        if (right==null ) {
            String columnName = MetaColumn.NAME.of(column);
            switch (operator) {
                case EQ:
                case EQUALS_CASE_INSENSITIVE:
                    out.append(columnName);
                    out.append(" IS NULL");
                    return null;
                case NOT_EQ:
                    out.append(columnName);
                    out.append(" IS NOT NULL");
                    return null;
                default:
                    throw new UnsupportedOperationException("Comparation the NULL value is forbiden by a operator: " + operator);
            }
        }

        String template = getCriterionTemplate(crit);
        if (template == null) {
            throw new UnsupportedOperationException("Unsupported SQL operator: " + operator);
        }

        if (crit.isConstant()) {
            out.append( template );
        } else if (right instanceof UjoProperty) {
            final UjoProperty rightProperty = (UjoProperty) right;
            final MetaColumn col2 = (MetaColumn) ormHandler.findColumnModel(rightProperty);

            if (!rightProperty.isDirect()) {
                throw new UnsupportedOperationException("Two tables is not supported yet");
            }
            if (col2.isForeignKey()) {
                throw new UnsupportedOperationException("Foreign key is not supported yet");
            }
            if (true) {
                String f = String.format(template, column.getAliasName(), col2.getAliasName());
                out.append(f);
            }
        } else if (column.isForeignKey()) {
           printForeignKey(crit, column, template, out);
           return crit;
        } else if (right instanceof List) {
            throw new UnsupportedOperationException("List is not supported yet: " + operator);
        } else {
            String f = MessageFormat.format(template, column.getAliasName(), "?");
            out.append(f);
            return crit;
        }
        return null;
    }

    /** Print all items of the foreign key */
    public void printForeignKey
        ( final ValueCriterion crit
        , final MetaColumn column
        , final String template
        , final Appendable out
        ) throws IOException
    {
        int size = column.getForeignColumns().size();
        for (int i=0; i<size; i++) {
            if (i>0) {
                out.append(' ');
                out.append(crit.getOperator().name());
                out.append(' ');
            }

            String f = MessageFormat.format(template, column.getForeignColumnName(i), "?");
            out.append(f);
        }
    }

    /** Print a SQL SELECT by table model and query
     * @param query The UJO query
     * @param count only count of items is required;
     */
    public Appendable printSelect
        ( final MetaTable table
        , final Query query
        , final boolean count
        , final Appendable out
        ) throws IOException {

        return table.isView()
            ? printSelectView(table, query, count, out)
            : printSelectTable(query, count, out)
            ;
    }


    /** Print SQL view SELECT
     * @param query The UJO query
     * @param count only count of items is required;
     */
    protected Appendable printSelectView(MetaTable table, Query query, boolean count, Appendable out) throws IOException {
        MetaView select = MetaTable.SELECT_MODEL.of(table);
        String where = query.getDecoder().getWhere();
        List<UjoProperty> order = query.getOrder();

        for (UjoProperty p : select.readProperties()) {
            String value = (String) p.of(select);

            if (p==MetaView.SELECT && count) {
                out.append(p.toString());
                out.append( "COUNT(*)" );
            } else if (p==MetaView.WHERE && value.length()+where.length()>0) {
                out.append(p.toString());
                out.append( value );
                out.append( value.isEmpty() || where.isEmpty() ? "" : " AND " );
                out.append( where );
            } else if (p==MetaView.ORDER && !order.isEmpty()){
                out.append(p.toString());
                out.append( value );
                out.append( value.isEmpty() || order.isEmpty() ? "" : " AND " );
                printSelectOrder(query, out);
            } else if (value.length()>0) {
                out.append(p.toString());
                out.append( value );
            }
        }
        return out;
    }


    /** Print SQL database SELECT
     * @param query The UJO query
     * @param count only count of items is required;
     */
    protected Appendable printSelectTable(Query query, boolean count, Appendable out) throws IOException {
        out.append("SELECT ");
        if (count) {
            out.append("COUNT(*)");
        } else {
            printTableColumns(query.getColumns(), null, out);
        }
        out.append("\n\tFROM ");

        if (query.getCriterion() != null) {
            CriterionDecoder ed = query.getDecoder();

            MetaTable[] tables = ed.getTables(query.getTableModel());

            for (int i=0; i<tables.length; ++i) {
                MetaTable table = tables[i];
                if (i>0) out.append(", ");
                printTableAlias(table, out);
            }

            String sql = ed.getWhere();
            if (!sql.isEmpty()) {
                out.append(" WHERE ");
                out.append(ed.getWhere());
            }
        } else {
            out.append(MetaTable.NAME.of(query.getTableModel()));
        }
        if (!count && !query.getOrder().isEmpty()) {
            printSelectOrder(query, out);
        }
        return out;
    }


    /** Print SQL ORDER BY */
    public void printSelectOrder(Query query, Appendable out) throws IOException {
        
        out.append(" ORDER BY ");
        final List<UjoProperty> props = query.getOrder();
        for (int i=0; i<props.size(); i++) {
            MetaColumn column = query.readOrderColumn(i);
            boolean ascending = props.get(i).isAscending();
            if (i>0) {
                out.append(", ");
            }
            printColumnAlias(column, out);
            if (!ascending) {
                out.append(" DESC");
            }
        }
    }

    /** Print full sequence name */
    protected Appendable printSequenceName(final UjoSequencer sequence, final Appendable out) throws IOException {
        out.append(sequence.getSequenceName());
        return out;
    }

    /** Print SQL CREATE SEQUENCE. */
    public Appendable printCreateSequence(final UjoSequencer sequence, final Appendable out) throws IOException {
        out.append("CREATE SEQUENCE ");
        printSequenceName(sequence, out);
        out.append(" START WITH " + sequence.getIncrement());
        out.append(" CACHE " + sequence.getInitDbCache());
        return out;
    }

    /** Print SQL ALTER SEQUENCE to modify INCREMENT. */
    public Appendable printAlterSequenceIncrement(final UjoSequencer sequence, final Appendable out) throws IOException {
        out.append("ALTER SEQUENCE ");
        printSequenceName(sequence, out);
        out.append(" INCREMENT BY " + sequence.getIncrement());
        return out;
    }

    /** Print the NEXT SQL SEQUENCE. */
    public Appendable printSeqNextValue(final UjoSequencer sequence, final Appendable out) throws IOException {
        out.append("SELECT NEXTVAL('");
        printSequenceName(sequence, out);
        out.append("')");
        return out;
    }

    /** Print SQL NEXT SEQUENCE Update or print none. The method is intended for an emulator of the sequence. */
    public Appendable printSeqNextValueUpdate(final UjoSequencer sequence, final Appendable out) throws IOException {
        return out;
    }

    /** Returns true, if the argument text is not null and not empty. */
    protected boolean isValid(final CharSequence text) {
        final boolean result = text!=null && text.length()>0;
        return result;
    }

    /** Print the new line. */
    final public void println(final Appendable out) throws IOException {
        out.append('\n');
    }

    /** Print SQL 'CREATE SCHEMA' */
    public Appendable printCommit(Appendable out) throws IOException {
        out.append("COMMIT");
        return out;
    }


}
