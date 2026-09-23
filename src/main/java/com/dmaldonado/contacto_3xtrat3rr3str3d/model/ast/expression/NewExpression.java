package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import java.util.ArrayList;
import java.util.List;

/** novus Persona(12, "Profesor") -- an instance of a class defined in a .z file. */
public class NewExpression extends Expression
{
    private final String           className;
    private final List<Expression> arguments;

    public NewExpression(String className, List<Expression> arguments, int line, int column)
    {
        super(line, column);
        this.className = className;
        this.arguments = arguments;
    }

    public String getClassName()
    {
        return className;
    }

    public List<Expression> getArguments()
    {
        return arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitNewExpression(this);
    }

    @Override
    public String getLabel()
    {
        return "novus " + className + "()";
    }

    @Override
    public List<AstNode> getChildren()
    {
        return new ArrayList<>(arguments);
    }
}
