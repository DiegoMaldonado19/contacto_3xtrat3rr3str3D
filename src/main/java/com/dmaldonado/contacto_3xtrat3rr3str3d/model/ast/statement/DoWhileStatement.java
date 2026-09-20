package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import java.util.ArrayList;
import java.util.List;

/** facere { } dum (x &lt; 10); */
public class DoWhileStatement extends AstNode
{
    private final Block      body;
    private final Expression condition;

    public DoWhileStatement(Block body, Expression condition, int line, int column)
    {
        super(line, column);
        this.body      = body;
        this.condition = condition;
    }

    public Block getBody()
    {
        return body;
    }

    public Expression getCondition()
    {
        return condition;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitDoWhileStatement(this);
    }

    @Override
    public String getLabel()
    {
        return "FACERE DUM";
    }

    @Override
    public List<AstNode> getChildren()
    {
        List<AstNode> children = new ArrayList<>();
        if (body != null)
        {
            children.add(body);
        }
        if (condition != null)
        {
            children.add(condition);
        }
        return children;
    }
}
