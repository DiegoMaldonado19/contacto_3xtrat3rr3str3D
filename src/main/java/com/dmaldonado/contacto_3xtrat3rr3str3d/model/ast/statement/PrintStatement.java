package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import java.util.ArrayList;
import java.util.List;

/** &gt;&gt; "Bienvenido" &gt;&gt; comandante; -- translated to %OINK. */
public class PrintStatement extends AstNode
{
    private final List<Expression> values;

    public PrintStatement(List<Expression> values, int line, int column)
    {
        super(line, column);
        this.values = values;
    }

    public List<Expression> getValues()
    {
        return values;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitPrintStatement(this);
    }

    @Override
    public String getLabel()
    {
        return "SALIDA";
    }

    @Override
    public List<AstNode> getChildren()
    {
        return new ArrayList<>(values);
    }
}
