package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import java.util.List;

/** interrumpe; */
public class BreakStatement extends AstNode
{
    public BreakStatement(int line, int column)
    {
        super(line, column);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitBreakStatement(this);
    }

    @Override
    public String getLabel()
    {
        return "INTERRUMPE";
    }

    @Override
    public List<AstNode> getChildren()
    {
        return List.of();
    }
}
