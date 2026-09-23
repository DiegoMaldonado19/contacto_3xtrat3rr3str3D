package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.types.DataType;
import java.util.List;

/**
 * esto fuerza : numerus, inside a function signature.
 *
 * array marks the Y? form "[] entero miArray": the function receives the
 * address of the caller's array, never a copy.
 */
public class Parameter extends AstNode
{
    private final String   name;
    private final String   typeText;
    private final DataType type;
    private final boolean  array;

    public Parameter(String name, String typeText, boolean array, int line, int column)
    {
        super(line, column);
        this.name     = name;
        this.typeText = typeText;
        this.type     = DataType.fromText(typeText);
        this.array    = array;
    }

    public boolean isArray()
    {
        return array;
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
        return "param " + name + " : " + (array ? "[] " : "") + typeText;
    }

    @Override
    public List<AstNode> getChildren()
    {
        return List.of();
    }
}
