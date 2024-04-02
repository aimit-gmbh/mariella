package org.mariella.persistence.query;

public class BinaryCondition implements Expression {
    private Expression left;
    private Expression right;
    private String operator;

    public static BinaryCondition and(Expression left, Expression right) {
        BinaryCondition binaryCondition = new BinaryCondition();
        binaryCondition.setLeft(left);
        binaryCondition.setRight(right);
        binaryCondition.setOperator("AND");
        return binaryCondition;
    }

    public static BinaryCondition or(Expression left, Expression right) {
        BinaryCondition binaryCondition = new BinaryCondition();
        binaryCondition.setLeft(left);
        binaryCondition.setRight(right);
        binaryCondition.setOperator("OR");
        return binaryCondition;
    }

    public static BinaryCondition like(Expression left, Expression right) {
        BinaryCondition binaryCondition = new BinaryCondition();
        binaryCondition.setLeft(left);
        binaryCondition.setRight(right);
        binaryCondition.setOperator("LIKE");
        return binaryCondition;
    }

    public static BinaryCondition eq(Expression left, Expression right) {
        BinaryCondition binaryCondition = new BinaryCondition();
        binaryCondition.setLeft(left);
        binaryCondition.setRight(right);
        binaryCondition.setOperator("=");
        return binaryCondition;
    }

    public static BinaryCondition gt(Expression left, Expression right) {
        BinaryCondition binaryCondition = new BinaryCondition();
        binaryCondition.setLeft(left);
        binaryCondition.setRight(right);
        binaryCondition.setOperator(">");
        return binaryCondition;
    }

    public static BinaryCondition gteq(Expression left, Expression right) {
        BinaryCondition binaryCondition = new BinaryCondition();
        binaryCondition.setLeft(left);
        binaryCondition.setRight(right);
        binaryCondition.setOperator(">=");
        return binaryCondition;
    }

    public static BinaryCondition lt(Expression left, Expression right) {
        BinaryCondition binaryCondition = new BinaryCondition();
        binaryCondition.setLeft(left);
        binaryCondition.setRight(right);
        binaryCondition.setOperator("<");
        return binaryCondition;
    }

    public static BinaryCondition lteq(Expression left, Expression right) {
        BinaryCondition binaryCondition = new BinaryCondition();
        binaryCondition.setLeft(left);
        binaryCondition.setRight(right);
        binaryCondition.setOperator("<=");
        return binaryCondition;
    }

    public static BinaryCondition noteq(Expression left, Expression right) {
        BinaryCondition binaryCondition = new BinaryCondition();
        binaryCondition.setLeft(left);
        binaryCondition.setRight(right);
        binaryCondition.setOperator("<>");
        return binaryCondition;
    }

    public void printSql(StringBuilder b) {
        left.printSql(b);
        b.append(' ');
        b.append(operator);
        b.append(' ');
        right.printSql(b);
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

}
