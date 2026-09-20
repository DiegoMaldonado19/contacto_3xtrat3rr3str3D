package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.types.DataType;
import java.util.List;

/** esto fuerza : numerus, inside a function signature. */
public class Parameter extends AstNode
{
    private final String   name;
    private final String   typeText;
    private final DataType type;

    public Parameter(String name, String typeText, int line, int column)
    {
        super(line, column);
        this.name     = name;
        this.typeText = typeText;
        this.type     = DataType.fromText(typeText);
    }

    public String getName()
    {
        return name;
    }

    public String getTypeText()
    {
        return typeText;
    }

    public DataType getType()
    {
        return type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitParameter(this);
    }

    @Override
    public String getLabel()
    {
        return "param " + name + " : " + typeText;
    }

    @Override
    public List<AstNode> getChildren()
    {
        return List.of();
    }
}
