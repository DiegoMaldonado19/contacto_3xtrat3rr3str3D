package com.dmaldonado.contacto_3xtrat3rr3str3d.model.analysis;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Language;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.ArrayDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.FunctionDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.ImportDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.Parameter;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.Program;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.StructDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.StructField;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.VariableDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.ArrayAccessExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.BinaryExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.CompositeLiteralExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.FunctionCallExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.IdentifierExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.IncrementExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.LiteralExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.MemberAccessExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.MethodCallExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.NewExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.ReadExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.UnaryExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.Assignment;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.Block;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.BreakStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.CaseClause;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.ContinueStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.DoWhileStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.ExpressionStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.ForStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.IfStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.IncrementStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.InputStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.PrintStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.ReturnStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.SwitchStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.WhileStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.errors.ErrorManager;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.symbols.ArraySymbol;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.symbols.FunctionSymbol;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.symbols.StructSymbol;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.symbols.Symbol;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.symbols.SymbolCategory;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.symbols.SymbolTable;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.symbols.VariableSymbol;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.types.DataType;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.types.TypeSystem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Semantic analysis: every visitX validates its rule and returns the DataType,
 * so the parent keeps composing the check upwards. On failure it reports the
 * error and returns ERROR instead of throwing.
 *
 * One instance per Program. The pipeline runs the imported .y files first and
 * the .pig last, all on the same SymbolTable, so the .pig sees what they declare.
 *
 * Besides checking, it leaves on the AST what the quadruple generator needs:
 * the symbol behind each name (and so its stack slot), the overload each call
 * resolved to, and the size of every frame.
 */
public class SemanticAnalyzer implements AstVisitor<DataType>
{
    private final ErrorManager errorManager;
    private final SymbolTable  symbolTable;

    /** The few rules that differ: where to declare, and whether a structura may be declared at all. */
    private Language            language;
    /** Function being analyzed; null inside MAIOR. */
    private FunctionDeclaration currentFunction;
    /** Loop nesting: interrumpe/perge are only valid when greater than zero. */
    private int                 loopDepth;
    /** elegir nesting: romper also leaves an elegir. */
    private int                 switchDepth;
    /**
     * True donde el enunciado prohibe declarar en PigLatin: el cuerpo de
     * si/dum/facere/per y el cuerpo de una funcion, cuyas variables van todas en
     * VARIABILES[ ]. Y? permite declarar en cualquier parte.
     */
    private boolean             declarationsForbidden;
    /** Only to give each per scope a distinct name in the symbol table graph. */
    private int                 forCounter;

    public SemanticAnalyzer(ErrorManager errorManager, SymbolTable symbolTable)
    {
        this.errorManager = errorManager;
        this.symbolTable  = symbolTable;
    }

    /* =================================================================
     * PROGRAM
     * ================================================================= */

    /** Paso 1 registra firmas, paso 2 recorre cuerpos: permite el uso adelantado. */
    @Override
    public DataType visitProgram(Program node)
    {
        language = node.getLanguage();

        for (AstNode global : node.getGlobals())
        {
            if (global instanceof StructDeclaration struct)
            {
                registerStruct(struct);
            }
        }
        for (FunctionDeclaration function : node.getFunctions())
        {
            registerFunctionSignature(function);
        }

        for (AstNode global : node.getGlobals())
        {
            if (global instanceof StructDeclaration struct)
            {
                validateStructFieldTypes(struct);   // already registered in pass 1
            }
            else
            {
                global.accept(this);
            }
        }
        for (FunctionDeclaration function : node.getFunctions())
        {
            function.accept(this);
        }

        symbolTable.openFrame("MAIOR");
        for (AstNode statement : node.getMainStatements())
        {
            statement.accept(this);
        }
        node.setFrameSize(symbolTable.closeFrame());
        return DataType.VOID;
    }

    /** The pipeline resolves imports before any analysis runs: nothing is left to check here. */
    @Override
    public DataType visitImportDeclaration(ImportDeclaration node)
    {
        return DataType.VOID;
    }

    /* =================================================================
     * DECLARATIONS
     * ================================================================= */
    @Override
    public DataType visitVariableDeclaration(VariableDeclaration node)
    {
        requireDeclarationPlace(node, node.getName());

        if (symbolTable.lookupLocal(node.getName()) != null)
        {
            error(node, "La variable '" + node.getName() + "' ya fue declarada en el ambito '"
                    + symbolTable.getCurrentScopeName() + "'.", node.getName());
            return DataType.ERROR;
        }

        // Un objeto se reporta por su novus: buscar la clase como structura
        // solo agregaria un "el tipo no existe" encima.
        if (node.getInitialValue() instanceof NewExpression object)
        {
            object.accept(this);
            declareVariable(node, DataType.ERROR, null);
            return DataType.ERROR;
        }

        DataType declaredType = node.getType();
        String   structName   = null;

        if (declaredType == DataType.ESTRUCTURA)
        {
            StructSymbol struct = symbolTable.lookupStruct(node.getTypeText());

            if (struct == null)
            {
                error(node, "El tipo '" + node.getTypeText() + "' no existe.", node.getTypeText());
                declareVariable(node, DataType.ERROR, null);
                return DataType.ERROR;
            }
            structName = struct.getName();
        }

        Expression value = node.getInitialValue();

        if (value != null)
        {
            DataType valueType = checkValue(value, declaredType, structName, false);

            if (!assignable(declaredType, structName, valueType, value.getStructName()))
            {
                error(node, "No se puede asignar un valor '"
                        + describe(valueType, value.getStructName()) + "' a la variable '"
                        + node.getName() + "' de tipo '" + node.getTypeText() + "'.",
                        node.getName());
            }
        }
        else if (declaredType == DataType.ESTRUCTURA && language == Language.PIG)
        {
            // Statement: "todos los atributos tengan un valor explicito". En Y?
            // "Persona alumno" si es valido: sus campos se llenan despues.
            error(node, "La variable '" + node.getName() + "' de tipo '" + node.getTypeText()
                    + "' debe inicializar todos sus atributos.", node.getName());
        }

        declareVariable(node, declaredType, structName);
        return declaredType;
    }

    private void declareVariable(VariableDeclaration node, DataType type, String structName)
    {
        VariableSymbol symbol = new VariableSymbol(node.getName(), type, node.getTypeText(),
                SymbolCategory.VARIABLE, symbolTable.getCurrentScopeName(),
                node.getInitialValue() != null, structName, false, node.getLine(), node.getColumn());

        symbolTable.declare(symbol);
        node.setSymbol(symbol);
    }

    @Override
    public DataType visitArrayDeclaration(ArrayDeclaration node)
    {
        requireDeclarationPlace(node, node.getName());

        if (symbolTable.lookupLocal(node.getName()) != null)
        {
            error(node, "El arreglo '" + node.getName() + "' ya fue declarado en el ambito '"
                    + symbolTable.getCurrentScopeName() + "'.", node.getName());
            return DataType.ERROR;
        }

        DataType elementType       = node.getElementType();
        String   elementStructName = null;

        if (node.getTypeText() == null)
        {
            error(node, "Falta el tipo del arreglo '" + node.getName()
                    + "' y no hay valores iniciales de los cuales deducirlo.", node.getName());
            elementType = DataType.ERROR;
        }
        else if (elementType == DataType.ESTRUCTURA)
        {
            StructSymbol struct = symbolTable.lookupStruct(node.getTypeText());

            if (struct == null)
            {
                error(node, "El tipo '" + node.getTypeText() + "' no existe.", node.getTypeText());
                declareArray(node, DataType.ERROR, null, -1);
                return DataType.ERROR;
            }
            elementStructName = struct.getName();
        }

        int size = -1;

        if (node.getSize() != null)
        {
            DataType sizeType = node.getSize().accept(this);

            if (sizeType != DataType.NUMERUS && sizeType != DataType.ERROR)
            {
                error(node, "El tamano del arreglo '" + node.getName()
                        + "' debe ser 'numerus', se recibio '" + sizeType + "'.",
                        node.getName());
            }
            Integer declared = extractInteger(node.getSize());

            if (declared != null && declared < 0)
            {
                error(node, "El tamano del arreglo '" + node.getName()
                        + "' no puede ser negativo.", node.getName());
            }
            else if (declared != null)
            {
                size = declared;
            }
        }

        List<Expression> values = node.getInitialValues();

        if (!values.isEmpty() && size >= 0 && values.size() != size)
        {
            error(node, "El arreglo '" + node.getName() + "' declara " + size
                    + " posiciones pero recibe " + values.size() + " valores.", node.getName());
        }
        for (Expression value : values)
        {
            DataType valueType = checkValue(value, elementType, elementStructName, false);

            if (!assignable(elementType, elementStructName, valueType, value.getStructName()))
            {
                error(value, "Valor de tipo '" + describe(valueType, value.getStructName())
                        + "' incompatible con el arreglo '" + node.getName() + "' de tipo '"
                        + node.getTypeText() + "'.", node.getName());
            }
        }

        declareArray(node, elementType, elementStructName, size);
        return elementType;
    }

    private void declareArray(ArrayDeclaration node, DataType elementType, String elementStructName,
                              int size)
    {
        ArraySymbol symbol = new ArraySymbol(node.getName(), elementType, node.getTypeText(),
                symbolTable.getCurrentScopeName(), size, node.getInitialValues().size(),
                elementStructName, node.getLine(), node.getColumn());

        symbolTable.declare(symbol);
        node.setSymbol(symbol);
    }

    /** Solo para una structura suelta: las de VARIABILES ya las registro el paso 1. */
    @Override
    public DataType visitStructDeclaration(StructDeclaration node)
    {
        registerStruct(node);
        validateStructFieldTypes(node);
        return DataType.ESTRUCTURA;
    }

    @Override
    public DataType visitStructField(StructField node)
    {
        return node.getType();
    }

    @Override
    public DataType visitParameter(Parameter node)
    {
        return node.getType();
    }

    @Override
    public DataType visitFunctionDeclaration(FunctionDeclaration node)
    {
        FunctionDeclaration previousFunction  = currentFunction;
        boolean             previousForbidden = declarationsForbidden;
        int                 previousDepth     = loopDepth;
        int                 previousSwitch    = switchDepth;

        currentFunction       = node;
        declarationsForbidden = false;   // VARIABILES[ ] es justo donde SI se declara
        loopDepth             = 0;
        switchDepth           = 0;

        symbolTable.openFrame(node.getName());

        if (node.returnsValue() && node.getReturnType() == DataType.ESTRUCTURA
                && symbolTable.lookupStruct(node.getReturnTypeText()) == null)
        {
            error(node, "El tipo de retorno '" + node.getReturnTypeText() + "' no existe.",
                    node.getReturnTypeText());
        }

        // Los parametros se declaran primero: ocupan los slots 1..n del marco,
        // que es donde el llamador escribe los argumentos.
        for (Parameter parameter : node.getParameters())
        {
            if (symbolTable.lookupLocal(parameter.getName()) != null)
            {
                error(parameter, "El parametro '" + parameter.getName() + "' esta repetido.",
                        parameter.getName());
                continue;
            }
            if (parameter.getType() == DataType.ESTRUCTURA
                    && symbolTable.lookupStruct(parameter.getTypeText()) == null)
            {
                error(parameter, "El tipo '" + parameter.getTypeText() + "' del parametro '"
                        + parameter.getName() + "' no existe.", parameter.getTypeText());
            }
            symbolTable.declare(parameterSymbol(parameter, node.getName()));
        }

        // Las structuras locales van primero: la siguiente variable ya puede usarlas.
        for (AstNode local : node.getLocalVariables())
        {
            if (local instanceof StructDeclaration struct)
            {
                registerStruct(struct);
            }
        }
        for (AstNode local : node.getLocalVariables())
        {
            if (local instanceof StructDeclaration struct)
            {
                validateStructFieldTypes(struct);
            }
            else
            {
                local.accept(this);
            }
        }

        // Enunciado: "dentro de las funciones solo se pueden definir variables
        // al principio". Pasada la seccion VARIABILES[ ], el cuerpo ya no admite
        // ninguna declaracion mas. El cuerpo comparte el ambito de los parametros:
        // una local no puede llamarse igual que uno de ellos.
        declarationsForbidden = true;
        visitStatements(node.getBody());

        int frameSize = symbolTable.closeFrame();

        if (node.getSymbol() != null)
        {
            node.getSymbol().setFrameSize(frameSize);
        }
        if (node.returnsValue() && !ReturnPathChecker.alwaysReturns(node.getBody()))
        {
            error(node, "La funcion '" + node.getName()
                    + "' no retorna un valor en todos sus caminos posibles.", node.getName());
        }

        currentFunction       = previousFunction;
        declarationsForbidden = previousForbidden;
        loopDepth             = previousDepth;
        switchDepth           = previousSwitch;
        return node.getReturnType();
    }

    /** A "[] entero arr" parameter is an array: it has to be indexable inside the body. */
    private Symbol parameterSymbol(Parameter parameter, String functionName)
    {
        String structName = structNameOf(parameter.getType(), parameter.getTypeText());

        if (parameter.isArray())
        {
            return new ArraySymbol(parameter.getName(), parameter.getType(), parameter.getTypeText(),
                    functionName, -1, 0, structName, parameter.getLine(), parameter.getColumn());
        }
        return new VariableSymbol(parameter.getName(), parameter.getType(), parameter.getTypeText(),
                SymbolCategory.PARAMETER, functionName, true, structName, false,
                parameter.getLine(), parameter.getColumn());
    }

    /* =================================================================
     * STATEMENTS
     * ================================================================= */

    /**
     * En PigLatin no abre ambito: declarar dentro de un bloque de control ya es
     * error. En Y? si, porque ahi se declara en cualquier parte.
     */
    @Override
    public DataType visitBlock(Block node)
    {
        if (language == Language.Y)
        {
            symbolTable.openScope(symbolTable.getCurrentScopeName());
        }
        visitStatements(node);

        if (language == Language.Y)
        {
            symbolTable.closeScope();
        }
        return DataType.VOID;
    }

    private void visitStatements(Block node)
    {
        int unreachable = ReturnPathChecker.firstUnreachableIndex(node);

        if (unreachable >= 0)
        {
            error(node.getStatements().get(unreachable),
                    "Codigo inalcanzable: nunca se ejecutara esta instruccion.", "");
        }
        for (AstNode statement : node.getStatements())
        {
            statement.accept(this);
        }
    }

    @Override
    public DataType visitAssignment(Assignment node)
    {
        DataType targetType   = node.getTarget().accept(this);
        String   targetStruct = node.getTarget().getStructName();
        DataType valueType    = checkValue(node.getValue(), targetType, targetStruct,
                                           isArrayValue(node.getTarget()));

        if (!assignable(targetType, targetStruct, valueType, node.getValue().getStructName()))
        {
            error(node, "Asignacion invalida: no se puede guardar '"
                    + describe(valueType, node.getValue().getStructName())
                    + "' en un destino de tipo '" + describe(targetType, targetStruct) + "'.", "=");
        }
        return targetType;
    }

    @Override
    public DataType visitIfStatement(IfStatement node)
    {
        requireBooleanCondition(node.getCondition(), "si");

        boolean previousForbidden = declarationsForbidden;
        declarationsForbidden = true;

        node.getThenBranch().accept(this);

        if (node.getElseBranch() != null)
        {
            node.getElseBranch().accept(this);
        }
        declarationsForbidden = previousForbidden;
        return DataType.VOID;
    }

    @Override
    public DataType visitWhileStatement(WhileStatement node)
    {
        requireBooleanCondition(node.getCondition(), keyword("dum", "mientras"));
        walkLoopBody(node.getBody());
        return DataType.VOID;
    }

    @Override
    public DataType visitDoWhileStatement(DoWhileStatement node)
    {
        walkLoopBody(node.getBody());
        requireBooleanCondition(node.getCondition(), keyword("facere-dum", "hacer-mientras"));
        return DataType.VOID;
    }

    @Override
    public DataType visitForStatement(ForStatement node)
    {
        symbolTable.openScope("per_" + (++forCounter));

        // El iterador es la unica declaracion que el enunciado permite fuera de
        // VARIABILES[ ], asi que la bandera se limpia solo para el: dentro de una
        // funcion ya viene levantada y su "per (esto i : numerus 0; ...)" es valido.
        boolean previousForbidden = declarationsForbidden;
        declarationsForbidden     = false;

        if (node.getInitialization() != null)
        {
            node.getInitialization().accept(this);
        }
        declarationsForbidden = previousForbidden;

        requireBooleanCondition(node.getCondition(), keyword("per", "para"));

        if (node.getUpdate() != null)
        {
            node.getUpdate().accept(this);
        }
        walkLoopBody(node.getBody());
        symbolTable.closeScope();
        return DataType.VOID;
    }

    /** The case values have to be comparable with the discriminant, by the same rule as '=='. */
    @Override
    public DataType visitSwitchStatement(SwitchStatement node)
    {
        DataType discriminant = operandType(node.getDiscriminant(), "elegir");

        boolean previousForbidden = declarationsForbidden;
        declarationsForbidden = true;
        switchDepth++;

        for (CaseClause clause : node.getCases())
        {
            if (!clause.isDefault())
            {
                DataType value = operandType(clause.getValue(), "caso");

                if (discriminant != DataType.ERROR && value != DataType.ERROR
                        && TypeSystem.relationalResult(discriminant, value, "==") == DataType.ERROR)
                {
                    error(clause.getValue(), "El valor del caso es '" + value
                            + "' y no se puede comparar con '" + discriminant + "'.", "caso");
                }
            }
            clause.accept(this);
        }

        switchDepth--;
        declarationsForbidden = previousForbidden;
        return DataType.VOID;
    }

    @Override
    public DataType visitCaseClause(CaseClause node)
    {
        return node.getBody().accept(this);
    }

    @Override
    public DataType visitReturnStatement(ReturnStatement node)
    {
        String keyword = keyword("reddere", "retornar");

        if (currentFunction == null)
        {
            error(node, "'" + keyword + "' solo puede usarse dentro de una funcion.", keyword);
            return DataType.ERROR;
        }

        if (node.getValue() == null)
        {
            if (currentFunction.returnsValue())
            {
                error(node, "La funcion '" + currentFunction.getName() + "' debe retornar '"
                        + currentFunction.getReturnTypeText() + "'.", keyword);
            }
            return DataType.VOID;
        }

        String   expectedStruct = structNameOf(currentFunction.getReturnType(),
                                               currentFunction.getReturnTypeText());
        DataType returnedType   = checkValue(node.getValue(), currentFunction.getReturnType(),
                                             expectedStruct, false);

        if (!currentFunction.returnsValue())
        {
            error(node, "La funcion '" + currentFunction.getName() + "' "
                    + keyword("fue declarada con 'actio'", "no declara '-> tipo'")
                    + " y no puede retornar un valor.", keyword);
        }
        else if (!assignable(currentFunction.getReturnType(), expectedStruct, returnedType,
                             node.getValue().getStructName()))
        {
            error(node, "Se retorna '"
                    + describe(returnedType, node.getValue().getStructName())
                    + "' pero la funcion '" + currentFunction.getName() + "' declara '"
                    + currentFunction.getReturnTypeText() + "'.", keyword);
        }
        return returnedType;
    }

    @Override
    public DataType visitBreakStatement(BreakStatement node)
    {
        if (loopDepth == 0 && switchDepth == 0)
        {
            String keyword = keyword("interrumpe", "romper");
            error(node, "'" + keyword + "' solo puede usarse dentro de un ciclo"
                    + keyword("", " o un elegir") + ".", keyword);
        }
        return DataType.VOID;
    }

    @Override
    public DataType visitContinueStatement(ContinueStatement node)
    {
        if (loopDepth == 0)
        {
            String keyword = keyword("perge", "continuar");
            error(node, "'" + keyword + "' solo puede usarse dentro de un ciclo.", keyword);
        }
        return DataType.VOID;
    }

    @Override
    public DataType visitPrintStatement(PrintStatement node)
    {
        String keyword = keyword(">>", "imprimir");

        for (Expression value : node.getValues())
        {
            DataType type = operandType(value, keyword);

            if (type == DataType.ESTRUCTURA)
            {
                error(value, "No se puede imprimir una structura completa; imprima sus atributos.",
                        keyword);
            }
            else if (type == DataType.VOID)
            {
                error(value, "'" + nameOf(value) + "' no retorna un valor que se pueda imprimir.",
                        keyword);
            }
        }
        return DataType.VOID;
    }

    @Override
    public DataType visitInputStatement(InputStatement node)
    {
        // A bare "<<" is valid: it reads and discards. There is nothing to check.
        if (node.getTarget() == null)
        {
            return DataType.VOID;
        }

        DataType type = node.getTarget().accept(this);

        if (type == DataType.ESTRUCTURA || isArrayValue(node.getTarget()))
        {
            error(node, "Solo se puede leer un valor primitivo desde la entrada, no una structura "
                    + "ni un arreglo completo.", "<<");
        }
        return DataType.VOID;
    }

    /**
     * Only what has an effect may stand alone: a call, a method call, leer() or
     * ++/--. Anything else is reported once, without descending into it, so
     * "pergue;" gives exactly one error.
     */
    @Override
    public DataType visitExpressionStatement(ExpressionStatement node)
    {
        Expression expression = node.getExpression();

        if (expression instanceof FunctionCallExpression || expression instanceof MethodCallExpression
                || expression instanceof ReadExpression || expression instanceof IncrementExpression)
        {
            expression.accept(this);
            return DataType.VOID;
        }

        String text = nameOf(expression);
        error(node, "'" + text + "' no es una instruccion valida: solo una llamada, 'leer()' o "
                + "'++'/'--' pueden ir solas.", text);
        return DataType.ERROR;
    }

    @Override
    public DataType visitIncrementStatement(IncrementStatement node)
    {
        DataType type = operandType(node.getTarget(), node.getOperator());

        if (TypeSystem.incrementResult(type) == DataType.ERROR && type != DataType.ERROR)
        {
            error(node, "El operador '" + node.getOperator()
                    + "' solo aplica a numerus o decimalis, no a '" + type + "'.",
                    node.getOperator());
        }
        return type;
    }

    /* =================================================================
     * EXPRESSIONS
     * ================================================================= */
    @Override
    public DataType visitBinaryExpression(BinaryExpression node)
    {
        String   operator = node.getOperator();
        DataType left     = operandType(node.getLeft(), operator);
        DataType right    = operandType(node.getRight(), operator);

        DataType result = switch (operator)
        {
            case "+", "-", "*", "/"               -> TypeSystem.arithmeticResult(left, right, operator);
            case "==", "!=", "<", ">", "<=", ">=" -> TypeSystem.relationalResult(left, right, operator);
            case "&&", "||"                       -> TypeSystem.logicalResult(left, right);
            default                               -> DataType.ERROR;
        };

        if (result == DataType.ERROR && left != DataType.ERROR && right != DataType.ERROR)
        {
            // La pista solo aplica si hay un textum de por medio: sin esta guarda
            // salia hasta en "'Carro' == 'Carro'", donde no viene a cuento.
            boolean concatenation = (left == DataType.TEXTUM || right == DataType.TEXTUM)
                    && !"+".equals(operator);

            error(node, "Operacion invalida: '"
                    + describe(left, node.getLeft().getStructName()) + "' " + operator + " '"
                    + describe(right, node.getRight().getStructName()) + "'."
                    + (concatenation ? " Recuerde que textum solo admite '+'." : ""),
                    operator);
        }
        return typeCheck(node, result);
    }

    /** "-" is the minus sign; any other operator ("non", "!") is logical negation. */
    @Override
    public DataType visitUnaryExpression(UnaryExpression node)
    {
        DataType operand = operandType(node.getOperand(), node.getOperator());
        DataType result  = "-".equals(node.getOperator())
                ? TypeSystem.unaryMinusResult(operand)
                : TypeSystem.negationResult(operand);

        if (result == DataType.ERROR && operand != DataType.ERROR)
        {
            error(node, "El operador '" + node.getOperator() + "' no puede aplicarse a '"
                    + operand + "'.", node.getOperator());
        }
        return typeCheck(node, result);
    }

    @Override
    public DataType visitIncrementExpression(IncrementExpression node)
    {
        DataType operand = operandType(node.getTarget(), node.getOperator());
        DataType result  = TypeSystem.incrementResult(operand);

        if (result == DataType.ERROR && operand != DataType.ERROR)
        {
            error(node, "El operador '" + node.getOperator()
                    + "' solo aplica a numerus o decimalis, no a '" + operand + "'.",
                    node.getOperator());
        }
        return typeCheck(node, result);
    }

    @Override
    public DataType visitLiteralExpression(LiteralExpression node)
    {
        return typeCheck(node, node.getType());
    }

    @Override
    public DataType visitIdentifierExpression(IdentifierExpression node)
    {
        Symbol symbol = symbolTable.lookup(node.getName());

        if (symbol == null)
        {
            // Las funciones viven fuera de los ambitos: sin esta consulta, usar
            // una sin parentesis diria "no declarada" en vez de la pista util.
            error(node, symbolTable.lookupFunctions(node.getName()).isEmpty()
                    ? "La variable '" + node.getName() + "' no ha sido declarada."
                    : "'" + node.getName() + "' es una funcion; para llamarla escriba '"
                            + node.getName() + "(...)'.", node.getName());
            return typeCheck(node, DataType.ERROR);
        }
        if (symbol instanceof StructSymbol)
        {
            error(node, "'" + node.getName() + "' es una structura, no una variable.",
                    node.getName());
            return typeCheck(node, DataType.ERROR);
        }

        node.setSymbol(symbol);

        if (symbol instanceof VariableSymbol variable)
        {
            node.setStructName(variable.getStructName());
        }
        else if (symbol instanceof ArraySymbol array)
        {
            // Reporta el tipo del ELEMENTO, que es lo que numeros[i] necesita.
            // Operar el arreglo pelado lo corta antes operandType().
            node.setStructName(array.getElementStructName());
        }
        return typeCheck(node, symbol.getType());
    }

    @Override
    public DataType visitArrayAccessExpression(ArrayAccessExpression node)
    {
        DataType indexType = node.getIndex().accept(this);

        if (indexType != DataType.NUMERUS && indexType != DataType.ERROR)
        {
            error(node, "El indice de un arreglo debe ser 'numerus', se recibio '"
                    + indexType + "'.", "[]");
        }

        DataType elementType = node.getArray().accept(this);

        // El nombre de la structura sobrevive al indice: sin eso no se resuelve
        // una cadena como mi_selva.animales[1].nombre.
        node.setStructName(node.getArray().getStructName());

        if (elementType == DataType.ERROR)
        {
            return typeCheck(node, DataType.ERROR);
        }
        if (!isArrayValue(node.getArray()))
        {
            error(node, "'" + nameOf(node.getArray()) + "' no es un arreglo.", nameOf(node.getArray()));
            return typeCheck(node, DataType.ERROR);
        }
        if (node.getArray() instanceof IdentifierExpression identifier
                && identifier.getSymbol() instanceof ArraySymbol array)
        {
            Integer index = extractInteger(node.getIndex());

            if (index != null && index < 0)
            {
                error(node, "Indice fuera de rango: las posiciones de un arreglo "
                        + "empiezan en 0.", "[]");
            }
            else if (index != null && array.getSize() >= 0 && index >= array.getSize())
            {
                error(node, "Indice fuera de rango: el arreglo '" + array.getName()
                        + "' tiene " + array.getSize() + " posiciones.", "[]");
            }
        }
        return typeCheck(node, elementType);
    }

    @Override
    public DataType visitMemberAccessExpression(MemberAccessExpression node)
    {
        Expression owner = node.getOwner();

        // Un error en el dueno ya fue reportado: el acceso no suma otro.
        if (owner.accept(this) == DataType.ERROR)
        {
            return typeCheck(node, DataType.ERROR);
        }
        if (isArrayValue(owner))
        {
            error(node, "'" + nameOf(owner) + "' es un arreglo: indexe una posicion antes de "
                    + "acceder a '." + node.getMemberName() + "'.", node.getMemberName());
            return typeCheck(node, DataType.ERROR);
        }

        String structName = owner.getStructName();

        if (structName == null)
        {
            error(node, "El acceso '." + node.getMemberName()
                    + "' solo es valido sobre una variable de tipo structura.",
                    node.getMemberName());
            return typeCheck(node, DataType.ERROR);
        }

        StructSymbol struct = symbolTable.lookupStruct(structName);

        if (struct == null || !struct.hasAttribute(node.getMemberName()))
        {
            error(node, "La structura '" + structName + "' no tiene el atributo '"
                    + node.getMemberName() + "'.", node.getMemberName());
            return typeCheck(node, DataType.ERROR);
        }

        VariableSymbol attribute = struct.getAttribute(node.getMemberName());
        node.setStructName(attribute.getStructName());
        return typeCheck(node, attribute.getType());
    }

    /**
     * Resolucion de sobrecarga: los argumentos se visitan UNA vez (un literal
     * { } espera al parametro elegido, que es quien le da su tipo), luego se
     * eligen los candidatos por aridad y, si queda mas de uno, por
     * compatibilidad, prefiriendo el que coincide exacto en mas posiciones.
     */
    @Override
    public DataType visitFunctionCallExpression(FunctionCallExpression node)
    {
        List<FunctionSymbol> overloads = symbolTable.lookupFunctions(node.getName());
        List<Expression>     arguments = node.getArguments();
        List<DataType>       types     = new ArrayList<>();

        for (Expression argument : arguments)
        {
            types.add(argument instanceof CompositeLiteralExpression ? null : argument.accept(this));
        }

        if (overloads.isEmpty())
        {
            error(node, "La funcion '" + node.getName() + "' no ha sido declarada.", node.getName());
            return typeCheck(node, DataType.ERROR);
        }

        List<FunctionSymbol> candidates = overloads.stream()
                .filter(function -> function.getParameterCount() == arguments.size())
                .toList();

        if (candidates.isEmpty())
        {
            if (overloads.size() == 1)
            {
                FunctionSymbol function = overloads.get(0);

                error(node, "La funcion '" + function.getSignature() + "' espera "
                        + function.getParameterCount() + " argumento(s) y recibio "
                        + arguments.size() + ".", node.getName());
                node.setStructName(function.getReturnStructName());
                return typeCheck(node, function.getType());
            }
            error(node, "Ninguna version de '" + node.getName() + "' recibe " + arguments.size()
                    + " argumento(s). Versiones: " + signatures(overloads) + ".", node.getName());
            return typeCheck(node, DataType.ERROR);
        }

        FunctionSymbol function = candidates.size() == 1
                ? candidates.get(0)
                : bestOverload(candidates, arguments, types);

        if (function == null)
        {
            error(node, "Ninguna version de '" + node.getName() + "' acepta esos argumentos. "
                    + "Versiones: " + signatures(overloads) + ".", node.getName());
            return typeCheck(node, DataType.ERROR);
        }
        node.setFunction(function);

        for (int i = 0; i < arguments.size(); i++)
        {
            VariableSymbol parameter    = function.getParameters().get(i);
            Expression     argument     = arguments.get(i);
            DataType       argumentType = types.get(i) == null
                    ? checkValue(argument, parameter.getType(), parameter.getStructName(), parameter.isArray())
                    : checkShape(argument, types.get(i), parameter.isArray());

            if (!assignable(parameter.getType(), parameter.getStructName(), argumentType,
                            argument.getStructName()))
            {
                error(argument, "El argumento " + (i + 1) + " de '" + node.getName()
                        + "' debe ser '" + parameter.getTypeText() + "' y es '"
                        + describe(argumentType, argument.getStructName()) + "'.", node.getName());
            }
        }

        node.setStructName(function.getReturnStructName());
        return typeCheck(node, function.getType());
    }

    /** Null when no candidate accepts every argument. */
    private FunctionSymbol bestOverload(List<FunctionSymbol> candidates, List<Expression> arguments,
                                        List<DataType> types)
    {
        FunctionSymbol best      = null;
        int            bestScore = -1;

        for (FunctionSymbol candidate : candidates)
        {
            int score = 0;

            for (int i = 0; i < arguments.size() && score >= 0; i++)
            {
                VariableSymbol parameter = candidate.getParameters().get(i);
                Expression     argument  = arguments.get(i);
                DataType       type      = types.get(i);

                if (type == null)   // a { } literal fits any single structura parameter
                {
                    score = parameter.getType() == DataType.ESTRUCTURA && !parameter.isArray()
                            ? score : -1;
                }
                else if (type != DataType.ERROR
                        && (isArrayValue(argument) != parameter.isArray()
                            || !assignable(parameter.getType(), parameter.getStructName(), type,
                                           argument.getStructName())))
                {
                    score = -1;
                }
                else if (type == parameter.getType()
                        && Objects.equals(argument.getStructName(), parameter.getStructName()))
                {
                    score++;
                }
            }
            if (score > bestScore)
            {
                best      = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    private static String signatures(List<FunctionSymbol> overloads)
    {
        return overloads.stream().map(FunctionSymbol::getSignature).collect(Collectors.joining(", "));
    }

    /** Objects arrive with Zetariano: until a .z class exists, no expression has methods. */
    @Override
    public DataType visitMethodCallExpression(MethodCallExpression node)
    {
        DataType owner = node.getOwner().accept(this);

        visitArguments(node.getArguments());

        if (owner != DataType.ERROR)
        {
            error(node, "El metodo '" + node.getMethodName() + "()' no existe: solo los objetos, "
                    + "cuyas clases se importan de un archivo .z, tienen metodos.",
                    node.getMethodName());
        }
        return typeCheck(node, DataType.ERROR);
    }

    @Override
    public DataType visitNewExpression(NewExpression node)
    {
        visitArguments(node.getArguments());

        error(node, "La clase '" + node.getClassName() + "' no existe: los objetos se crean con "
                + "clases importadas de un archivo .z.", node.getClassName());
        return typeCheck(node, DataType.ERROR);
    }

    /** The error of a misplaced { } belongs to the owner call, not to each argument. */
    private void visitArguments(List<Expression> arguments)
    {
        for (Expression argument : arguments)
        {
            if (!(argument instanceof CompositeLiteralExpression))
            {
                argument.accept(this);
            }
        }
    }

    /** Alone it reads text; checkValue gives it the type of its destination first. */
    @Override
    public DataType visitReadExpression(ReadExpression node)
    {
        return typeCheck(node, DataType.TEXTUM);
    }

    /** Solo se alcanza con un { } fuera de lugar: checkValue desvia los validos. */
    @Override
    public DataType visitCompositeLiteralExpression(CompositeLiteralExpression node)
    {
        error(node, "Un literal '{ ... }' solo puede usarse al declarar o asignar una "
                + "structura, o como valores iniciales de un arreglo.", "{}");

        for (Expression value : node.getValues())
        {
            value.accept(this);
        }
        return typeCheck(node, DataType.ERROR);
    }

    /* =================================================================
     * Registration (pass 1)
     * ================================================================= */
    private void registerStruct(StructDeclaration node)
    {
        requireDeclarationPlace(node, node.getName());

        // Enunciado: PigLatin ya no define estructuras propias. Se registra igual,
        // para que cada uso posterior no sume un "el tipo no existe".
        if (language == Language.PIG)
        {
            error(node, "La structura '" + node.getName() + "' debe declararse en un archivo .y "
                    + "e importarse: PigLatin ya no define estructuras propias.", node.getName());
        }

        if (symbolTable.lookupLocal(node.getName()) != null)
        {
            error(node, "La structura '" + node.getName() + "' ya fue declarada en el ambito '"
                    + symbolTable.getCurrentScopeName() + "'.", node.getName());
            return;
        }

        StructSymbol struct = new StructSymbol(node.getName(), symbolTable.getCurrentScopeName(),
                node.getLine(), node.getColumn());

        for (StructField field : node.getFields())
        {
            VariableSymbol attribute = new VariableSymbol(field.getName(), field.getType(),
                    field.getTypeText(), SymbolCategory.ATTRIBUTE, node.getName(), false,
                    structNameOf(field.getType(), field.getTypeText()), field.isArray(),
                    field.getLine(), field.getColumn());

            if (!struct.addAttribute(attribute))
            {
                error(field, "El atributo '" + field.getName()
                        + "' esta repetido en la structura '" + node.getName() + "'.",
                        field.getName());
            }
        }
        symbolTable.declare(struct);
    }

    /** Paso 2: ya estan todas registradas, un campo puede citar una declarada despues. */
    private void validateStructFieldTypes(StructDeclaration node)
    {
        for (StructField field : node.getFields())
        {
            if (field.getType() == DataType.ESTRUCTURA
                    && symbolTable.lookupStruct(field.getTypeText()) == null)
            {
                error(field, "El tipo '" + field.getTypeText() + "' del atributo '"
                        + field.getName() + "' no existe.", field.getTypeText());
            }
        }
    }

    private void registerFunctionSignature(FunctionDeclaration node)
    {
        List<VariableSymbol> parameters = new ArrayList<>();

        for (Parameter parameter : node.getParameters())
        {
            parameters.add(new VariableSymbol(parameter.getName(), parameter.getType(),
                    parameter.getTypeText(), SymbolCategory.PARAMETER, node.getName(), true,
                    structNameOf(parameter.getType(), parameter.getTypeText()), parameter.isArray(),
                    parameter.getLine(), parameter.getColumn()));
        }

        FunctionSymbol function = new FunctionSymbol(node.getName(), node.getReturnType(),
                node.getReturnTypeText(), symbolTable.getCurrentScopeName(), parameters,
                node.returnsValue(), structNameOf(node.getReturnType(), node.getReturnTypeText()),
                node.getLine(), node.getColumn());

        // Sobrecarga permitida: solo choca otra version con los mismos tipos.
        if (!symbolTable.declareFunction(function))
        {
            error(node, "La funcion '" + function.getSignature()
                    + "' ya fue declarada con los mismos parametros.", node.getName());
            return;
        }
        node.setSymbol(function);
    }

    /* =================================================================
     * Structure literals
     * ================================================================= */

    /**
     * Valida un literal contra su structura, en sus dos formas: con nombre,
     * { nombre: "Yennifer", edad: 999 }, donde el orden no importa, o
     * posicional, {"Valeria", 25, {"Avenida Central", 500}}, que es la del
     * enunciado y va en el orden en que se declararon los atributos.
     */
    private void validateStructLiteral(CompositeLiteralExpression literal, StructSymbol struct)
    {
        if (struct == null)
        {
            return;
        }
        if (literal.isNamed())
        {
            validateNamedLiteral(literal, struct);
            return;
        }

        List<VariableSymbol> attributes = new ArrayList<>(struct.getAttributes().values());
        List<Expression>     values     = literal.getValues();

        if (values.size() != attributes.size())
        {
            error(literal, "La structura '" + struct.getName() + "' tiene " + attributes.size()
                    + " atributo(s) y el literal trae " + values.size() + " valor(es).",
                    struct.getName());
        }
        for (int i = 0; i < Math.min(values.size(), attributes.size()); i++)
        {
            validateAttributeValue(values.get(i), attributes.get(i));
        }
    }

    /**
     * El mapa 'pending' cumple las tres reglas a la vez: nombre obligatorio,
     * todos los atributos presentes, y el orden no importa.
     */
    private void validateNamedLiteral(CompositeLiteralExpression literal, StructSymbol struct)
    {
        Map<String, VariableSymbol> pending = new LinkedHashMap<>(struct.getAttributes());

        for (int i = 0; i < literal.getFieldNames().size(); i++)
        {
            String         fieldName = literal.getFieldNames().get(i);
            Expression     value     = literal.getValues().get(i);
            VariableSymbol attribute = pending.remove(fieldName);

            if (attribute == null)
            {
                error(value, "'" + fieldName + "' no es un atributo de '"
                        + struct.getName() + "'.", fieldName);
                continue;
            }
            validateAttributeValue(value, attribute);
        }

        for (String missing : pending.keySet())
        {
            error(literal, "Falta el atributo obligatorio '" + missing + "' de '"
                    + struct.getName() + "'.", missing);
        }
    }

    /** One value of a literal against the attribute it fills. A nested { } recurses through checkValue. */
    private void validateAttributeValue(Expression value, VariableSymbol attribute)
    {
        if (attribute.isArray())
        {
            validateArrayAttribute(value, attribute);
            return;
        }

        DataType valueType = checkValue(value, attribute.getType(), attribute.getStructName(), false);

        if (!assignable(attribute.getType(), attribute.getStructName(), valueType,
                        value.getStructName()))
        {
            error(value, "El atributo '" + attribute.getName() + "' es '" + attribute.getTypeText()
                    + "' y recibio '" + describe(valueType, value.getStructName()) + "'.",
                    attribute.getName());
        }
    }

    /**
     * An array attribute takes either an existing array of the same element
     * type, the form of the statement ({"Carlos", mis_enteros}), or the
     * PigLatin form "animales: Animal[7]" that creates one of that size.
     */
    private void validateArrayAttribute(Expression value, VariableSymbol attribute)
    {
        // "Animal[7]" indexa el nombre del TIPO, no una variable: no se resuelve
        // contra la tabla de simbolos, solo se valida el tamano.
        if (value instanceof ArrayAccessExpression access
                && access.getArray() instanceof IdentifierExpression typeName
                && typeName.getName().equals(attribute.getTypeText()))
        {
            DataType sizeType = access.getIndex().accept(this);

            if (sizeType != DataType.NUMERUS && sizeType != DataType.ERROR)
            {
                error(access, "El tamano del arreglo '" + attribute.getName()
                        + "' debe ser 'numerus', se recibio '" + sizeType + "'.",
                        attribute.getName());
            }

            typeName.setComputedType(attribute.getType());
            typeName.setStructName(attribute.getStructName());
            access.setComputedType(attribute.getType());
            access.setStructName(attribute.getStructName());
            return;
        }

        DataType valueType = checkValue(value, attribute.getType(), attribute.getStructName(), true);

        if (!assignable(attribute.getType(), attribute.getStructName(), valueType,
                        value.getStructName()))
        {
            error(value, "El atributo '" + attribute.getName() + "' es un arreglo de '"
                    + attribute.getTypeText() + "' y recibio '"
                    + describe(valueType, value.getStructName()) + "'.", attribute.getName());
        }
    }

    /* =================================================================
     * Support
     * ================================================================= */

    /**
     * Verifica un valor contra su destino: desvia el literal { } al validador
     * de structuras, le da a leer() el tipo que se espera, y es el UNICO lugar
     * que distingue un arreglo completo de un valor.
     */
    private DataType checkValue(Expression value, DataType targetType, String targetStruct,
                                boolean targetIsArray)
    {
        if (value instanceof CompositeLiteralExpression literal)
        {
            if (targetIsArray)
            {
                error(literal, "Un literal '{ ... }' no puede asignarse a un arreglo completo: "
                        + "asigne cada posicion por separado.", "{}");
                return typeCheck(literal, DataType.ERROR);
            }
            if (targetType == DataType.ESTRUCTURA)
            {
                validateStructLiteral(literal, symbolTable.lookupStruct(targetStruct));
                literal.setComputedType(DataType.ESTRUCTURA);
                literal.setStructName(targetStruct);
                return DataType.ESTRUCTURA;
            }
        }
        if (value instanceof ReadExpression read && targetType.isPrimitive() && !targetIsArray)
        {
            return typeCheck(read, targetType);
        }
        return checkShape(value, value.accept(this), targetIsArray);
    }

    /** A whole array only goes where an array is expected, and the other way round. */
    private DataType checkShape(Expression value, DataType type, boolean targetIsArray)
    {
        if (type == DataType.ERROR || isArrayValue(value) == targetIsArray)
        {
            return type;
        }

        String name = nameOf(value);

        error(value, targetIsArray
                ? "Se esperaba un arreglo y se recibio un valor '" + describe(type, value.getStructName()) + "'."
                : "El arreglo '" + name + "' no es un valor primitivo; use '" + name + "[indice]'.",
                name);
        return typeCheck(value, DataType.ERROR);
    }

    /**
     * Enunciado: "no se consideran primitivos los arreglos de datos". El nombre
     * pelado de un arreglo se indexa, no se opera.
     */
    private DataType operandType(Expression operand, String operator)
    {
        DataType type = operand.accept(this);

        if (type != DataType.ERROR && isArrayValue(operand))
        {
            String name = nameOf(operand);

            error(operand, "El arreglo '" + name + "' no es un valor primitivo y no puede operarse "
                    + "con '" + operator + "'; use '" + name + "[indice]'.", name);
            return typeCheck(operand, DataType.ERROR);
        }
        return type;
    }

    /**
     * Is this a whole array rather than one of its elements? A name bound to an
     * array, or an array attribute. Only meaningful once the expression was visited.
     */
    private boolean isArrayValue(Expression expression)
    {
        if (expression instanceof IdentifierExpression identifier)
        {
            return identifier.getSymbol() instanceof ArraySymbol;
        }
        if (expression instanceof MemberAccessExpression member)
        {
            StructSymbol   struct    = symbolTable.lookupStruct(member.getOwner().getStructName());
            VariableSymbol attribute = struct == null ? null : struct.getAttribute(member.getMemberName());

            return attribute != null && attribute.isArray();
        }
        return false;
    }

    /** How an expression is named in a message: the variable or attribute, when there is one. */
    private static String nameOf(Expression expression)
    {
        if (expression instanceof IdentifierExpression identifier)
        {
            return identifier.getName();
        }
        if (expression instanceof MemberAccessExpression member)
        {
            return member.getMemberName();
        }
        return expression.getLabel();
    }

    /**
     * Same as TypeSystem.isAssignable, plus the rule TypeSystem cannot express:
     * two different structuras are both ESTRUCTURA, but a Persona is not a Carro.
     */
    private boolean assignable(DataType target, String targetStruct,
                               DataType source, String sourceStruct)
    {
        if (!TypeSystem.isAssignable(target, source))
        {
            return false;
        }
        if (target == DataType.ESTRUCTURA && source == DataType.ESTRUCTURA)
        {
            return targetStruct != null && targetStruct.equals(sourceStruct);
        }
        return true;
    }

    private void requireBooleanCondition(Expression condition, String statement)
    {
        if (condition == null)
        {
            return;
        }

        DataType type = condition.accept(this);

        if (type != DataType.BOOLEANO && type != DataType.ERROR)
        {
            error(condition, "La condicion de '" + statement
                    + "' debe ser booleana, se recibio '" + type + "'.", statement);
        }
    }

    /**
     * Enunciado de PigLatin: las variables solo se definen al principio de la
     * funcion. Reporta, pero el simbolo se declara igual para no encadenar un
     * "no declarada" en cada uso posterior. Y? declara en cualquier parte.
     *
     * MAIOR> queda fuera a proposito (desviacion declarada en docs/05): alli
     * visitProgram nunca levanta la bandera.
     */
    private void requireDeclarationPlace(AstNode node, String name)
    {
        if (declarationsForbidden && language == Language.PIG)
        {
            error(node, "'" + name + "' no puede declararse aqui. Las declaraciones van "
                    + "al inicio de la funcion, en la seccion VARIABILES[ ].", name);
        }
    }

    private void walkLoopBody(Block body)
    {
        boolean previousForbidden = declarationsForbidden;
        declarationsForbidden = true;
        loopDepth++;

        body.accept(this);

        loopDepth--;
        declarationsForbidden = previousForbidden;
    }

    /** The same statement is spelled differently in each language: the message echoes the user's. */
    private String keyword(String pig, String y)
    {
        return language == Language.Y ? y : pig;
    }

    /**
     * Constant value of an integer expression, or null when it is not constant.
     * Integer and not int: -1 is a valid index, so it cannot double as the
     * "not constant" marker.
     */
    private Integer extractInteger(Expression expression)
    {
        if (expression instanceof LiteralExpression literal
                && literal.getType() == DataType.NUMERUS)
        {
            try
            {
                return Integer.parseInt(literal.getText());
            }
            catch (NumberFormatException ignored)
            {
                return null;   // out of int range
            }
        }
        if (expression instanceof UnaryExpression unary && "-".equals(unary.getOperator()))
        {
            Integer value = extractInteger(unary.getOperand());
            return value == null ? null : -value;
        }
        // Statement: an index "que se puede evaluar al hacer la verificacion
        // de semantica" has to be range checked, so numeros[1 + 1] is folded.
        if (expression instanceof BinaryExpression binary)
        {
            Integer left  = extractInteger(binary.getLeft());
            Integer right = extractInteger(binary.getRight());

            if (left == null || right == null)
            {
                return null;
            }

            return switch (binary.getOperator())
            {
                case "+" -> left + right;
                case "-" -> left - right;
                case "*" -> left * right;
                case "/" -> right == 0 ? null : left / right;
                default  -> null;
            };
        }
        return null;
    }

    /** Como se nombra un tipo en un mensaje: "Carro", no "structura". */
    private String describe(DataType type, String structName)
    {
        return (type == DataType.ESTRUCTURA && structName != null)
                ? structName : type.toString();
    }

    private String structNameOf(DataType type, String typeText)
    {
        return type == DataType.ESTRUCTURA ? typeText : null;
    }

    private DataType typeCheck(Expression node, DataType type)
    {
        node.setComputedType(type);
        return type;
    }

    private void error(AstNode node, String description, String lexeme)
    {
        errorManager.addSemantic(description, lexeme, node.getLine(), node.getColumn());
    }
}
