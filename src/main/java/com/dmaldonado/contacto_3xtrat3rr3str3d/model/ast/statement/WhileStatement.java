package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import java.util.ArrayList;
import java.util.List;

/** dum (x &lt; 100) { } finis; */
public class WhileStatement extends AstNode
{
    private final Expression condition;
    private final Block      body;

    public WhileStatement(Expression condition, Block body, int line, int column)
    {
        super(line, column);
        this.condition = condition;
        this.body      = body;
    }

    public Expression getCondition()
    {
        return condition;
    }

    public Block getBody()
    {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitWhileStatement(this);
    }

    @Override
    public String getLabel()
    {
        return "DUM";
    }

    @Override
    public List<AstNode> getChildren()
    {
        List<AstNode> children = new ArrayList<>();
        if (condition != null)
        {
            children.add(condition);
        }
        if (body != null)
        {
            children.add(body);
        }
        return children;
    }
}
