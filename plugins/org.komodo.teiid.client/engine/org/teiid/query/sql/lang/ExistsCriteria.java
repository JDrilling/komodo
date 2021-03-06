/* Generated By:JJTree: Do not edit this line. ExistsCriteria.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.teiid.query.sql.lang;

import org.komodo.spi.query.sql.lang.IExistsCriteria;
import org.teiid.query.parser.LanguageVisitor;
import org.teiid.query.parser.TeiidParser;

/**
 *
 */
public class ExistsCriteria extends Criteria
    implements PredicateCriteria, SubqueryContainer<QueryCommand>, IExistsCriteria<LanguageVisitor, QueryCommand> {

    private QueryCommand command;

    private boolean shouldEvaluate;

    private boolean negated;

    private SubqueryHint subqueryHint = new SubqueryHint();

    /**
     * @param p
     * @param id
     */
    public ExistsCriteria(TeiidParser p, int id) {
        super(p, id);
    }

    @Override
    public QueryCommand getCommand() {
        return this.command;
    }

    @Override
    public void setCommand(QueryCommand subqueryCommand){
        this.command = subqueryCommand;
    }

    /**
     * @return whether to evaluate
     */
    public boolean shouldEvaluate() {
        return shouldEvaluate;
    }
    
    /**
     * @param shouldEvaluate
     */
    public void setShouldEvaluate(boolean shouldEvaluate) {
        this.shouldEvaluate = shouldEvaluate;
    }

    @Override
    public boolean isNegated() {
        return negated;
    }
    
    @Override
    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    /**
     * @return subquery hint
     */
    public SubqueryHint getSubqueryHint() {
        return subqueryHint;
    }
    
    /**
     * @param subqueryHint
     */
    public void setSubqueryHint(SubqueryHint subqueryHint) {
        this.subqueryHint = subqueryHint;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.command == null) ? 0 : this.command.hashCode());
        result = prime * result + (this.negated ? 1231 : 1237);
        result = prime * result + (this.shouldEvaluate ? 1231 : 1237);
        result = prime * result + ((this.subqueryHint == null) ? 0 : this.subqueryHint.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        ExistsCriteria other = (ExistsCriteria)obj;
        if (this.command == null) {
            if (other.command != null) return false;
        } else if (!this.command.equals(other.command)) return false;
        if (this.negated != other.negated) return false;
        if (this.shouldEvaluate != other.shouldEvaluate) return false;
        if (this.subqueryHint == null) {
            if (other.subqueryHint != null) return false;
        } else if (!this.subqueryHint.equals(other.subqueryHint)) return false;
        return true;
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(LanguageVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExistsCriteria clone() {
        ExistsCriteria clone = new ExistsCriteria(this.parser, this.id);

        if(getCommand() != null)
            clone.setCommand(getCommand().clone());
        clone.setNegated(isNegated());
        clone.setShouldEvaluate(shouldEvaluate());
        if(getSubqueryHint() != null)
            clone.setSubqueryHint(getSubqueryHint().clone());

        return clone;
    }

}
/* JavaCC - OriginalChecksum=5cc7f321c1d5e22ab073aa2d0115c232 (do not edit this line) */
