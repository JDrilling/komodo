/*
 * JBoss, Home of Professional Open Source.
*
* See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
*
* See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
*/
package org.teiid.query.validator.v86;

import org.komodo.spi.runtime.version.ITeiidVersion;
import org.komodo.spi.runtime.version.TeiidVersion.Version;
import org.teiid.query.validator.v85.Test85FunctionMetadataValidator;

/**
 *
 */
@SuppressWarnings( "javadoc" )
public class Test86FunctionMetadataValidator extends Test85FunctionMetadataValidator {

    protected Test86FunctionMetadataValidator(ITeiidVersion teiidVersion) {
        super(teiidVersion);
    }

    public Test86FunctionMetadataValidator() {
        this(Version.TEIID_8_6.get());
    }

}