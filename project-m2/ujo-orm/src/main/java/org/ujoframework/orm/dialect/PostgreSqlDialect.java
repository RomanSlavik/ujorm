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

package org.ujoframework.orm.dialect;

import java.io.IOException;
import org.ujoframework.orm.Query;
import org.ujoframework.orm.SqlDialect;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaIndex;

/** PostgreSQL (http://www.postgresql.org/) */
public class PostgreSqlDialect extends SqlDialect {

    @Override
    public String getJdbcUrl() {
        return "";
    }

    @Override
    public String getJdbcDriver() {
        return "org.postgresql.Driver";
    }

    /** Print SQL 'CREATE SCHEMA' */
    @Override
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("CREATE SCHEMA ");
        out.append(schema);
        return out;
    }

    /** PostgreSql dialect uses a database type OID (instead of the BLBO). */
    @Override
    protected String getColumnType(final MetaColumn column) {
        switch (MetaColumn.DB_TYPE.of(column)) {
            case BLOB:
            case CLOB:
            case BINARY:
                return "BYTEA"; // The 'bytea' data type allows storage of binary strings.
            default:
                return super.getColumnType(column);
        }
    }

    /**
     * Print an INDEX for the parameter column.
     * @return More statements separated by the ';' charactes are enabled
     */
    @Override
    public Appendable printIndex(final MetaIndex index, final Appendable out) throws IOException {
        super.printIndex(index,out);
        if (MetaIndex.UNIQUE.of(index)) {
            printIndexCondition(index, out);
        }
        return out;
    }

    /**
     * Create an PARTIAL INDEX for exclude NULL values.
     * The behaviour is similar like the PostgreSQL or ORACLE databases.
     * <br>NOTE: you can disable the feature by the overwriting current method by an empty body.
     */
    public Appendable printIndexCondition(final MetaIndex index, final Appendable out) throws IOException {
        String prefix = " WHERE ";
        for (MetaColumn column : MetaIndex.COLUMNS.of(index)) {
            if (!column.hasDefaultValue()) {
                out.append(prefix);
                out.append(MetaColumn.NAME.of(column));
                out.append(" IS NOT NULL");
                prefix = " AND ";
            }
        }
        return out;
    }

    /** Print an OFFSET of the statement SELECT. */
    @Override
    public void printOffset(Query query, Appendable out) throws IOException {
        if (query.isOffset()) {
            out.append(" OFFSET " + query.getOffset());
        }
    }

}