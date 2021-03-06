/* Generated By:JJTree: Do not edit this line. ReturnStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.teiid.query.sql.proc;

import org.komodo.spi.annotation.Since;
import org.komodo.spi.query.sql.proc.IReturnStatement;
import org.komodo.spi.runtime.version.TeiidVersion.Version;
import org.teiid.query.parser.LanguageVisitor;
import org.teiid.query.parser.TeiidParser;
import org.teiid.query.sql.symbol.Expression;

/**
 *
 */
@Since(Version.TEIID_8_0)
public class ReturnStatement extends AssignmentStatement implements IReturnStatement<Expression, LanguageVisitor> {

    /**
     * @param p
     * @param id
     */
    public ReturnStatement(TeiidParser p, int id) {
        super(p, id);
    }

    /**
     * Return the type for this statement, this is one of the types
     * defined on the statement object.
     * @return The statement type
     */
    @Override
    public StatementType getType() {
        return StatementType.TYPE_RETURN;
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(LanguageVisitor visitor) {
        visitor.visit(this);
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public ReturnStatement clone() {
        ReturnStatement clone = new ReturnStatement(this.parser, this.id);

        if(getExpression() != null)
            clone.setExpression(getExpression().clone());
        if(getCommand() != null)
            clone.setCommand(getCommand().clone());
        if(getVariable() != null)
            clone.setVariable(getVariable().clone());
        if(getValue() != null)
            clone.setValue(getValue().clone());

        return clone;
    }

}
/* JavaCC - OriginalChecksum=783a3e8bb13999eaeee6f7dc50bef2b1 (do not edit this line) */
