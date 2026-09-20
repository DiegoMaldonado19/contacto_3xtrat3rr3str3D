package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import java.util.ArrayList;
import java.util.List;

/** reddere total;   reddere; */
public class ReturnStatement extends AstNode
{
    private final Expression value;

    public ReturnStatement(Expression value, int line, int column)
    {
        super(line, column);
        this.value = value;
    }

    /** Null when the function returns nothing. */
    public Expression getValue()
    {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitReturnStatement(this);
    }

    @Override
    public String getLabel()
    {
        return "REDDERE";
    }

    @Override
    public List<AstNode> getChildren()
    {
        List<AstNode> children = new ArrayList<>();
        if (value != null)
        {
            children.add(value);
        }
        return children;
    }
}
