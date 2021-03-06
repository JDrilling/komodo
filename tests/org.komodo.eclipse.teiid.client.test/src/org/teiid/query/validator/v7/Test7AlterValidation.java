/*
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
*/
package org.teiid.query.validator.v7;

import org.junit.Test;
import org.komodo.spi.runtime.version.TeiidVersion.Version;
import org.teiid.query.sql.AbstractTestFactory;
import org.teiid.query.sql.v7.Test7Factory;
import org.teiid.query.validator.AbstractTestAlterValidation;

/**
 *
 */
@SuppressWarnings( {"nls", "javadoc"} )
public class Test7AlterValidation extends AbstractTestAlterValidation {

    private Test7Factory factory;

    /**
     *
     */
    public Test7AlterValidation() {
        super(Version.TEIID_7_7.get());
    }

    @Override
    protected AbstractTestFactory getFactory() {
        if (factory == null)
            factory = new Test7Factory(getQueryParser());

        return factory;
    }

    @Test
    public void testValidateAlterProcedure() {
        helpValidate("alter procedure spTest8a as begin select 1; end",
                     new String[] {"spTest8a"},
                     getMetadataFactory().exampleBQTCached());
        helpValidate("alter procedure MMSP1 as begin select 1; end",
                     new String[] {"BEGIN\nSELECT 1;\nEND"},
                     getMetadataFactory().exampleBQTCached());
    }
}
