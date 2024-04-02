package org.mariella.persistence.query;

public abstract class Join implements FromClauseElement {
    private FromClauseElement left;
    private FromClauseElement right;

    protected abstract void printJoin(StringBuilder b);

    public FromClauseElement getLeft() {
        return left;
    }

    public void setLeft(FromClauseElement leftTableReference) {
        this.left = leftTableReference;
    }

    public FromClauseElement getRight() {
        return right;
    }

    public void setRight(FromClauseElement rightExpression) {
        this.right = rightExpression;
    }

    @Override
    public void printFromClause(StringBuilder b) {
        left.printFromClause(b);
        printJoin(b);
        right.printFromClause(b);
    }

}
