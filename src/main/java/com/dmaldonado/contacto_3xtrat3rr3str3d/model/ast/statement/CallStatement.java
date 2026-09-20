package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.FunctionCallExpression;
import java.util.List;

/** atacarCerdos(10, 0.5); -- a call used as a statement, discarding its value. */
public class CallStatement extends AstNode
{
    private final FunctionCallExpression call;

    public CallStatement(FunctionCallExpression call, int line, int column)
    {
        super(line, column);
        this.call = call;
    }

    public FunctionCallExpression getCall()
    {
        return call;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitCallStatement(this);
    }

    @Override
    public String getLabel()
    {
        return "LLAMADA";
    }

    @Override
    public List<AstNode> getChildren()
    {
        return call == null ? List.of() : List.of(call);
    }
}
