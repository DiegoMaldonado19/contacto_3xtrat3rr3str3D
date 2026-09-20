package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import java.util.List;

/** non x, -x */
public class UnaryExpression extends Expression
{
    private final String     operator;
    private final Expression operand;

    public UnaryExpression(String operator, Expression operand, int line, int column)
    {
        super(line, column);
        this.operator = operator;
        this.operand  = operand;
    }

    public String getOperator()
    {
        return operator;
    }

    public Expression getOperand()
    {
        return operand;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitUnaryExpression(this);
    }

    @Override
    public String getLabel()
    {
        return operator + " (unario)";
    }

    @Override
    public List<AstNode> getChildren()
    {
        return operand == null ? List.of() : List.of(operand);
    }
}
