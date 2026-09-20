package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import java.util.List;

/** lider.nombre, mi_selva.animales -- chainable: variable.prop.prop. */
public class MemberAccessExpression extends Expression
{
    private final Expression owner;
    private final String     memberName;

    public MemberAccessExpression(Expression owner, String memberName, int line, int column)
    {
        super(line, column);
        this.owner      = owner;
        this.memberName = memberName;
    }

    public Expression getOwner()
    {
        return owner;
    }

    public String getMemberName()
    {
        return memberName;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitMemberAccessExpression(this);
    }

    @Override
    public String getLabel()
    {
        return "." + memberName;
    }

    @Override
    public List<AstNode> getChildren()
    {
        return owner == null ? List.of() : List.of(owner);
    }
}
