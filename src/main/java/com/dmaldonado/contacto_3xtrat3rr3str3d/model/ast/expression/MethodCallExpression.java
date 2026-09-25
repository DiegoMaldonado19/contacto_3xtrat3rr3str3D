package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.symbols.FunctionSymbol;
import java.util.ArrayList;
import java.util.List;

/** misObjetos[9].hablar(x) -- chainable like any suffix: a.b[0].getNombre(). */
public class MethodCallExpression extends Expression
{
    private final Expression       owner;
    private final String           methodName;
    private final List<Expression> arguments;

    /** The method overload the call resolved to. Set by the semantic analyzer. */
    private FunctionSymbol function;

    public MethodCallExpression(Expression owner, String methodName, List<Expression> arguments,
                                int line, int column)
    {
        super(line, column);
        this.owner      = owner;
        this.methodName = methodName;
        this.arguments  = arguments;
    }

    public Expression getOwner()
    {
        return owner;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public List<Expression> getArguments()
    {
        return arguments;
    }

    public FunctionSymbol getFunction()
    {
        return function;
    }

    public void setFunction(FunctionSymbol function)
    {
        this.function = function;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitMethodCallExpression(this);
    }

    @Override
    public String getLabel()
    {
        return "." + methodName + "()";
    }

    @Override
    public List<AstNode> getChildren()
    {
        List<AstNode> children = new ArrayList<>();
        if (owner != null)
        {
            children.add(owner);
        }
        children.addAll(arguments);
        return children;
    }
}
