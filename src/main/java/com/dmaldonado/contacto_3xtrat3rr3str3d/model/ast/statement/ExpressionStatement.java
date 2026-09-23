package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import java.util.List;

/**
 * atacarCerdos(10, 0.5);  misObjetos[9].hablar();  leer()  -- an expression used as a
 * statement, discarding its value.
 *
 * The grammar accepts any expression here so that a typo like "pergue;" reaches
 * the semantic analyzer, which is the one that decides what may stand alone.
 */
public class ExpressionStatement extends AstNode
{
    private final Expression expression;

    public ExpressionStatement(Expression expression, int line, int column)
    {
        super(line, column);
        this.expression = expression;
    }

    public Expression getExpression()
    {
        return expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitExpressionStatement(this);
    }

    @Override
    public String getLabel()
    {
        return "INSTRUCCION";
    }

    @Override
    public List<AstNode> getChildren()
    {
        return expression == null ? List.of() : List.of(expression);
    }
}
