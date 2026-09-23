package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.types.DataType;
import java.util.List;

/**
 * 10, 9.81, "texto", 'a', verum, falsus  (and verdadero, falso in Y?).
 *
 * The type comes straight from the grammar alternative that matched, so the
 * semantic analyzer never has to guess it back from the text.
 */
public class LiteralExpression extends Expression
{
    private final String   text;
    private final DataType type;

    public LiteralExpression(String text, DataType type, int line, int column)
    {
        super(line, column);
        this.text = text;
        this.type = type;
    }

    /** Raw text, quotes included for textum and littera. */
    public String getText()
    {
        return text;
    }

    public DataType getType()
    {
        return type;
    }

    /** The boolean true of any of the languages. */
    public boolean isTrue()
    {
        return type == DataType.BOOLEANO && ("verum".equals(text) || "verdadero".equals(text));
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitLiteralExpression(this);
    }

    @Override
    public String getLabel()
    {
        return text;
    }

    @Override
    public List<AstNode> getChildren()
    {
        return List.of();
    }
}
