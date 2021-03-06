/*************************************************************************************
 * JBoss, Home of Professional Open Source.
* See the COPYRIGHT.txt file distributed with this work for information
* regarding copyright ownership. Some portions may be licensed
* to Red Hat, Inc. under one or more contributor license agreements.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
* 02110-1301 USA.
 ************************************************************************************/
package org.komodo.spi.query.sql.lang;

import java.util.List;

import org.komodo.spi.query.sql.ILanguageVisitor;

/**
 *
 */
public interface ISetQuery<QC extends IQueryCommand, 
                                                O extends IOrderBy,
                                                Q extends IQuery,
                                                E extends IExpression, 
                                                LV extends ILanguageVisitor> extends IQueryCommand<O, Q, E, LV> {

    /**
     * Enumerator of types of operation
     */
    public enum Operation {
        /** Represents UNION of two queries */
        UNION,
        /** Represents intersection of two queries */
        INTERSECT,
        /** Represents set difference of two queries */
        EXCEPT
    }
    
    /**
     * Is an all query
     * 
     * @return true if all
     */
    boolean isAll();
    
    /**
     * Set flag that this is an all query
     * 
     * @param value
     */
    void setAll(boolean value);

    /**
     * Get operation for this set
     * 
     * @return Operation as defined in this class
     */
    Operation getOperation();

    /**
     * Get left side of the query
     * 
     * @return left part of query
     */
    QC getLeftQuery();

    /**
     * Set the left side of the query
     * 
     * @param query
     */
    void setLeftQuery(QC query);

    /**
     * Get right side of the query
     * 
     * @return right part of query
     */
    QC getRightQuery();

    /**
     * Set the right side of the query
     * 
     * @param query
     */
    void setRightQuery(QC query);

    /**
     * @return the left and right queries as a list.  This list cannot be modified.
     */
    List<QC> getQueryCommands();

}
