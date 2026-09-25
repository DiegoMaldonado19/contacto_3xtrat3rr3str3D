package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import java.util.ArrayList;
import java.util.List;

/**
 * &gt;&gt; "Bienvenido" &gt;&gt; comandante;  imprimir(x)  println(x)  print(x)
 *
 * newline is false only for Zetariano's print: every other form ends the line.
 */
public class PrintStatement extends AstNode
{
    private final List<Expression> values;
    private final boolean          newline;

    public PrintStatement(List<Expression> values, boolean newline, int line, int column)
    {
        super(line, column);
        this.values  = values;
        this.newline = newline;
    }

    public List<Expression> getValues()
    {
        return values;
    }

    public boolean endsLine()
    {
        return newline;
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
