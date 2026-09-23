package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.Block;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.symbols.FunctionSymbol;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.types.DataType;
import java.util.ArrayList;
import java.util.List;

/**
 * actio atacarCerdos(...) { ... } finis;  and
 * ratio numerus calcularPoder(...) { ... } finis;
 *
 * localVariables holds the declarations written in the VARIABILES[ ] section,
 * kept apart from the body so the semantic analyzer can tell a variable
 * declared where the language allows it from one declared mid function.
 */
public class FunctionDeclaration extends AstNode
{
    private final String          name;
    private final String          returnTypeText;
    private final DataType        returnType;
    private final List<Parameter> parameters;
    private final List<AstNode>   localVariables;
    private final Block           body;
    private final boolean         returnsValue;

    /** Set by the semantic analyzer: null when the signature was rejected as a duplicate. */
    private FunctionSymbol symbol;

    public FunctionDeclaration(String name, String returnTypeText, List<Parameter> parameters,
                               List<AstNode> localVariables, Block body, boolean returnsValue,
                               int line, int column)
    {
        super(line, column);
        this.name           = name;
        this.returnTypeText = returnTypeText;
        this.returnType     = returnsValue ? DataType.fromText(returnTypeText) : DataType.VOID;
        this.parameters     = parameters;
        this.localVariables = localVariables;
        this.body           = body;
        this.returnsValue   = returnsValue;
    }

    public String getName()
    {
        return name;
    }

    public String getReturnTypeText()
    {
        return returnTypeText;
    }

    public DataType getReturnType()
    {
        return returnType;
    }

    public List<Parameter> getParameters()
    {
        return parameters;
    }

    /** Declarations of the VARIABILES[ ] section; empty when there is none. */
    public List<AstNode> getLocalVariables()
    {
        return localVariables;
    }

    public Block getBody()
    {
        return body;
    }

    public boolean returnsValue()
    {
        return returnsValue;
    }

    public FunctionSymbol getSymbol()
    {
        return symbol;
    }

    public void setSymbol(FunctionSymbol symbol)
    {
        this.symbol = symbol;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor)
    {
        return visitor.visitFunctionDeclaration(this);
    }

    @Override
    public String getLabel()
    {
        return (returnsValue ? "ratio " + returnTypeText + " " : "actio ") + name;
    }

    @Override
    public List<AstNode> getChildren()
    {
        List<AstNode> children = new ArrayList<>(parameters);
        children.addAll(localVariables);
        if (body != null)
        {
            children.add(body);
        }
        return children;
    }
}
