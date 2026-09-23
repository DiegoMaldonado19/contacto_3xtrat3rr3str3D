package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import java.util.ArrayList;
import java.util.List;

/** caso 1: ...  or  siempre: ...  inside an elegir. */
public class CaseClause extends AstNode
{
    private final Expression value;
    private final Block      body;

    public CaseClause(Expression value, Block body, int line, int column)
    {
        super(line, column);
        this.value = value;
        this.body  = body;
    }

    /** Null for the default case (siempre). */
    public Expression getValue()
    {
        return value;
    }

    public boolean isDefault()
    {
        return value == null;
    }

    public Block getBody()
    {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitCaseClause(this);
    }

    @Override
    public String getLabel()
    {
        return isDefault() ? "SIEMPRE" : "CASO";
    }

    @Override
    public List<AstNode> getChildren()
    {
        List<AstNode> children = new ArrayList<>();
        if (value != null)
        {
            children.add(value);
        }
        if (body != null)
        {
            children.add(body);
        }
        return children;
    }
}
