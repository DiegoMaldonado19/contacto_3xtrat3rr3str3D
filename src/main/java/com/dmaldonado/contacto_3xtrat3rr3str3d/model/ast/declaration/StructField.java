package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.types.DataType;
import java.util.List;

/**
 * One attribute inside a structura.
 *
 * A PigLatin array field carries no size (series animales : Animal;) because
 * the dimension is only given when the variable is declared. A Y? array field
 * does (entero miArray[10]), and the size is constant by the statement.
 */
public class StructField extends AstNode
{
    private final String   name;
    private final String   typeText;
    private final DataType type;
    private final boolean  array;
    /** -1 when the field is not an array or its size is given later. */
    private final int      size;

    public StructField(String name, String typeText, boolean array, int size, int line, int column)
    {
        super(line, column);
        this.name     = name;
        this.typeText = typeText;
        this.type     = DataType.fromText(typeText);
        this.array    = array;
        this.size     = size;
    }

    public int getSize()
    {
        return size;
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

    public boolean isArray()
    {
        return array;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitStructField(this);
    }

    @Override
    public String getLabel()
    {
        return (array ? "series " : "esto ") + name + " : " + typeText;
    }

    @Override
    public List<AstNode> getChildren()
    {
        return List.of();
    }
}
