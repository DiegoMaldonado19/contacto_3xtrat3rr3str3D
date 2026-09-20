package com.dmaldonado.contacto_3xtrat3rr3str3d.model.analysis;

import com.dmaldonado.contacto_3xtrat3rr3str3d.grammar.PigParser;
import com.dmaldonado.contacto_3xtrat3rr3str3d.grammar.PigParserBaseVisitor;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.AstNode;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Expression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.ArrayDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.FunctionDeclaration;
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
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.UnaryExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.Assignment;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.Block;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.BreakStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.CallStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.ContinueStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.DoWhileStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.ForStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.IfStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.IncrementStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.InputStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.PrintStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.ReturnStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.WhileStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.types.DataType;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Construye el AST propio a partir del parse tree, con el visitor que genera
 * ANTLR: visitXxx(ctx) devuelve el nodo de esa regla, y el valor de retorno es
 * el unico canal entre una regla y su padre.
 *
 * Las reglas puente (un solo hijo) no se sobrescriben: visitChildren() heredado
 * ya devuelve el resultado del hijo.
 *
 * Recorrido completo: docs/05-Manual-Tecnico.md (6)
 */
public class AstBuilderVisitor extends PigParserBaseVisitor<AstNode>
{
    /** Entry point: walks the parse tree and returns the root of the AST. */
    public Program build(PigParser.ProgramaContext parseTree)
    {
        return visit(parseTree) instanceof Program program ? program : null;
    }

    /* =================================================================
     * PROGRAM
     * ================================================================= */
    @Override
    public AstNode visitPrograma(PigParser.ProgramaContext ctx)
    {
        List<AstNode> globals             = new ArrayList<>();
        List<FunctionDeclaration> methods = new ArrayList<>();
        List<AstNode> mainStatements      = new ArrayList<>();

        if (ctx.seccionVariables() != null)
        {
            for (PigParser.DeclaracionGlobalContext global : ctx.seccionVariables().declaracionGlobal())
            {
                add(globals, visit(global));
            }
        }
        if (ctx.seccionFunciones() != null)
        {
            for (PigParser.DeclaracionFuncionContext function : ctx.seccionFunciones().declaracionFuncion())
            {
                if (visit(function) instanceof FunctionDeclaration declaration)
                {
                    methods.add(declaration);
                }
            }
        }
        if (ctx.seccionPrincipal() != null)
        {
            for (PigParser.InstruccionContext statement : ctx.seccionPrincipal().instruccion())
            {
                add(mainStatements, visit(statement));
            }
        }
        return new Program(globals, methods, mainStatements, line(ctx), column(ctx));
    }

    /* =================================================================
     * DECLARATIONS
     * ================================================================= */
    @Override
    public AstNode visitDeclaracionVariableSimple(PigParser.DeclaracionVariableSimpleContext ctx)
    {
        Expression value = expression(ctx.expresion());

        if (value == null)
        {
            value = implicitBooleanValue(ctx.tipo());
        }
        return new VariableDeclaration(ctx.ID().getText(), ctx.tipo().getText(), value,
                line(ctx), column(ctx));
    }

    @Override
    public AstNode visitDeclaracionVariableEstructura(PigParser.DeclaracionVariableEstructuraContext ctx)
    {
        return new VariableDeclaration(ctx.ID().getText(), ctx.tipo().getText(),
                expression(ctx.literalCompuesto()), line(ctx), column(ctx));
    }

    /** "esto activo : verum;": el tipo es tambien el valor, se materializa aqui. */
    private Expression implicitBooleanValue(PigParser.TipoContext ctx)
    {
        String text = ctx.getText();

        if ("verum".equals(text) || "falsus".equals(text))
        {
            return new LiteralExpression(text, DataType.BOOLEANO, line(ctx), column(ctx));
        }
        return null;
    }

    @Override
    public AstNode visitDeclaracionArreglo(PigParser.DeclaracionArregloContext ctx)
    {
        List<Expression> values = expressionList(ctx.listaExpresiones());

        return new ArrayDeclaration(ctx.ID().getText(), expression(ctx.expresion()),
                arrayTypeText(ctx, values), values, line(ctx), column(ctx));
    }

    /** Sin tipo explicito se deduce del primer valor; sin valores, lo reporta la semantica. */
    private String arrayTypeText(PigParser.DeclaracionArregloContext ctx, List<Expression> values)
    {
        if (ctx.tipo() != null)
        {
            return ctx.tipo().getText();
        }
        if (!values.isEmpty() && values.get(0) instanceof LiteralExpression literal)
        {
            return literal.getType().getLatinName();
        }
        return null;
    }

    @Override
    public AstNode visitCampoSimple(PigParser.CampoSimpleContext ctx)
    {
        return new StructField(ctx.ID().getText(), ctx.tipo().getText(), false,
                line(ctx), column(ctx));
    }

    @Override
    public AstNode visitCampoArreglo(PigParser.CampoArregloContext ctx)
    {
        return new StructField(ctx.ID().getText(), ctx.tipo().getText(), true,
                line(ctx), column(ctx));
    }

    @Override
    public AstNode visitDeclaracionEstructura(PigParser.DeclaracionEstructuraContext ctx)
    {
        List<StructField> fields = new ArrayList<>();

        for (PigParser.AtributoEstructuraContext attribute : ctx.atributoEstructura())
        {
            if (visit(attribute) instanceof StructField field)
            {
                fields.add(field);
            }
        }
        return new StructDeclaration(ctx.ID().getText(), fields, line(ctx), column(ctx));
    }

    @Override
    public AstNode visitParametro(PigParser.ParametroContext ctx)
    {
        return new Parameter(ctx.ID().getText(), ctx.tipo().getText(), line(ctx), column(ctx));
    }

    /* =================================================================
     * FUNCTIONS
     * ================================================================= */
    @Override
    public AstNode visitFuncionSinRetorno(PigParser.FuncionSinRetornoContext ctx)
    {
        return new FunctionDeclaration(ctx.ID().getText(), "void",
                parameters(ctx.listaParametros()), localVariables(ctx.cuerpoFuncion()),
                block(ctx.cuerpoFuncion()), false, line(ctx), column(ctx));
    }

    @Override
    public AstNode visitFuncionConRetorno(PigParser.FuncionConRetornoContext ctx)
    {
        return new FunctionDeclaration(ctx.ID().getText(), ctx.tipo().getText(),
                parameters(ctx.listaParametros()), localVariables(ctx.cuerpoFuncion()),
                block(ctx.cuerpoFuncion()), true, line(ctx), column(ctx));
    }

    /** El cuerpo guarda solo instrucciones; VARIABILES[ ] sale aparte en localVariables(). */
    @Override
    public AstNode visitCuerpoFuncion(PigParser.CuerpoFuncionContext ctx)
    {
        return new Block(statements(ctx.instruccion()), line(ctx), column(ctx));
    }

    private List<AstNode> localVariables(PigParser.CuerpoFuncionContext ctx)
    {
        List<AstNode> declarations = new ArrayList<>();

        if (ctx != null && ctx.seccionVariablesLocales() != null)
        {
            for (PigParser.DeclaracionGlobalContext declaration : ctx.seccionVariablesLocales().declaracionGlobal())
            {
                add(declarations, visit(declaration));
            }
        }
        return declarations;
    }

    @Override
    public AstNode visitBloque(PigParser.BloqueContext ctx)
    {
        return new Block(statements(ctx.instruccion()), line(ctx), column(ctx));
    }

    /* =================================================================
     * STATEMENTS
     * ================================================================= */
    @Override
    public AstNode visitAsignacionSimple(PigParser.AsignacionSimpleContext ctx)
    {
        return new Assignment(expression(ctx.destino()), expression(ctx.expresion()),
                line(ctx), column(ctx));
    }

    @Override
    public AstNode visitAsignacionEstructura(PigParser.AsignacionEstructuraContext ctx)
    {
        return new Assignment(expression(ctx.destino()), expression(ctx.literalCompuesto()),
                line(ctx), column(ctx));
    }

    @Override
    public AstNode visitInstruccionIncremento(PigParser.InstruccionIncrementoContext ctx)
    {
        return new IncrementStatement(expression(ctx.destino()),
                ctx.INCREMENTO() != null ? "++" : "--", line(ctx), column(ctx));
    }

    @Override
    public AstNode visitInstruccionLlamada(PigParser.InstruccionLlamadaContext ctx)
    {
        return visit(ctx.llamadaFuncion()) instanceof FunctionCallExpression call
                ? new CallStatement(call, line(ctx), column(ctx))
                : null;
    }

    /** Pliega la cadena aliter de derecha a izquierda: si(a) A aliter(b) B => If(a,A,If(b,B)). */
    @Override
    public AstNode visitInstruccionSi(PigParser.InstruccionSiContext ctx)
    {
        // bloque(1) solo existe si un aliter sin condicion cierra la cadena.
        AstNode elseBranch = ctx.ALITER() != null ? block(ctx.bloque(1)) : null;

        List<PigParser.AliterCondicionalContext> chain = ctx.aliterCondicional();

        for (int i = chain.size() - 1; i >= 0; i--)
        {
            PigParser.AliterCondicionalContext link = chain.get(i);
            elseBranch = new IfStatement(expression(link.expresion()), block(link.bloque()),
                    elseBranch, line(link), column(link));
        }

        return new IfStatement(expression(ctx.expresion()), block(ctx.bloque(0)),
                elseBranch, line(ctx), column(ctx));
    }

    @Override
    public AstNode visitInstruccionMientras(PigParser.InstruccionMientrasContext ctx)
    {
        return new WhileStatement(expression(ctx.expresion()), block(ctx.bloque()),
                line(ctx), column(ctx));
    }

    @Override
    public AstNode visitInstruccionHacerMientras(PigParser.InstruccionHacerMientrasContext ctx)
    {
        return new DoWhileStatement(block(ctx.bloque()), expression(ctx.expresion()),
                line(ctx), column(ctx));
    }

    @Override
    public AstNode visitActualizacionUnaria(PigParser.ActualizacionUnariaContext ctx)
    {
        return new IncrementStatement(expression(ctx.destino()),
                ctx.INCREMENTO() != null ? "++" : "--", line(ctx), column(ctx));
    }

    @Override
    public AstNode visitActualizacionAsignacion(PigParser.ActualizacionAsignacionContext ctx)
    {
        return new Assignment(expression(ctx.destino()), expression(ctx.expresion()),
                line(ctx), column(ctx));
    }

    @Override
    public AstNode visitInstruccionPara(PigParser.InstruccionParaContext ctx)
    {
        return new ForStatement(child(ctx.inicializacionPara()), expression(ctx.expresion()),
                child(ctx.actualizacionPara()), block(ctx.bloque()), line(ctx), column(ctx));
    }

    @Override
    public AstNode visitInstruccionRetorno(PigParser.InstruccionRetornoContext ctx)
    {
        return new ReturnStatement(expression(ctx.expresion()), line(ctx), column(ctx));
    }

    @Override
    public AstNode visitInstruccionInterrumpe(PigParser.InstruccionInterrumpeContext ctx)
    {
        return new BreakStatement(line(ctx), column(ctx));
    }

    @Override
    public AstNode visitInstruccionPerge(PigParser.InstruccionPergeContext ctx)
    {
        return new ContinueStatement(line(ctx), column(ctx));
    }

    @Override
    public AstNode visitInstruccionSalida(PigParser.InstruccionSalidaContext ctx)
    {
        List<Expression> values = new ArrayList<>();

        for (PigParser.ExpresionContext value : ctx.expresion())
        {
            Expression printed = expression(value);
            if (printed != null)
            {
                values.add(printed);
            }
        }
        return new PrintStatement(values, line(ctx), column(ctx));
    }

    /** Without a destino the read discards the value: expression() gives null. */
    @Override
    public AstNode visitInstruccionEntrada(PigParser.InstruccionEntradaContext ctx)
    {
        return new InputStatement(expression(ctx.destino()), line(ctx), column(ctx));
    }

    /* =================================================================
     * ASSIGNMENT TARGETS
     * ================================================================= */
    @Override
    public AstNode visitDestino(PigParser.DestinoContext ctx)
    {
        Expression current = new IdentifierExpression(ctx.ID().getText(), line(ctx), column(ctx));

        for (PigParser.SufijoDestinoContext suffix : ctx.sufijoDestino())
        {
            if (suffix instanceof PigParser.AccesoIndiceContext index)
            {
                current = new ArrayAccessExpression(current, expression(index.expresion()),
                        line(index), column(index));
            }
            else
            {
                PigParser.AccesoAtributoContext member = (PigParser.AccesoAtributoContext) suffix;
                current = new MemberAccessExpression(current, member.ID().getText(),
                        line(member), column(member));
            }
        }
        return current;
    }

    /* =================================================================
     * EXPRESSIONS
     *
     * The six precedence rules all share the shape  A : B (op B)* , so a
     * single method folds them, LEFT associative:  a - b - c => ((a - b) - c)
     * ================================================================= */
    @Override public AstNode visitExpresionOr(PigParser.ExpresionOrContext ctx)                         { return fold(ctx); }
    @Override public AstNode visitExpresionAnd(PigParser.ExpresionAndContext ctx)                       { return fold(ctx); }
    @Override public AstNode visitExpresionIgualdad(PigParser.ExpresionIgualdadContext ctx)             { return fold(ctx); }
    @Override public AstNode visitExpresionRelacional(PigParser.ExpresionRelacionalContext ctx)         { return fold(ctx); }
    @Override public AstNode visitExpresionAditiva(PigParser.ExpresionAditivaContext ctx)               { return fold(ctx); }
    @Override public AstNode visitExpresionMultiplicativa(PigParser.ExpresionMultiplicativaContext ctx) { return fold(ctx); }

    /** Con un solo operando el bucle no corre y el hijo pasa intacto. */
    private AstNode fold(ParserRuleContext ctx)
    {
        Expression left = expression(ctx.getChild(0));

        for (int i = 1; i + 1 < ctx.getChildCount(); i += 2)
        {
            String operator  = ctx.getChild(i).getText();
            Expression right = expression(ctx.getChild(i + 1));
            left = new BinaryExpression(left, operator, right, line(ctx), column(ctx));
        }
        return left;
    }

    @Override
    public AstNode visitUnariaNegacionLogica(PigParser.UnariaNegacionLogicaContext ctx)
    {
        return new UnaryExpression("non", expression(ctx.expresionUnaria()), line(ctx), column(ctx));
    }

    @Override
    public AstNode visitUnariaNegativo(PigParser.UnariaNegativoContext ctx)
    {
        return new UnaryExpression("-", expression(ctx.expresionUnaria()), line(ctx), column(ctx));
    }

    @Override
    public AstNode visitUnariaPrefija(PigParser.UnariaPrefijaContext ctx)
    {
        return new IncrementExpression(expression(ctx.expresionUnaria()),
                ctx.INCREMENTO() != null ? "++" : "--", true, line(ctx), column(ctx));
    }

    /** Each suffix wraps the previous one, so arr[i].prop[j] chains correctly. */
    @Override
    public AstNode visitExpresionSufijo(PigParser.ExpresionSufijoContext ctx)
    {
        Expression current = expression(ctx.expresionPrimaria());

        for (PigParser.SufijoExpresionContext suffix : ctx.sufijoExpresion())
        {
            if (suffix instanceof PigParser.SufijoIndiceContext index)
            {
                current = new ArrayAccessExpression(current, expression(index.expresion()),
                        line(index), column(index));
            }
            else if (suffix instanceof PigParser.SufijoAtributoContext member)
            {
                current = new MemberAccessExpression(current, member.ID().getText(),
                        line(member), column(member));
            }
            else
            {
                String operator = suffix instanceof PigParser.SufijoIncrementoContext ? "++" : "--";
                current = new IncrementExpression(current, operator, false,
                        line(suffix), column(suffix));
            }
        }
        return current;
    }

    /* -------- Primary expressions (leaves of the AST) -------- */
    @Override
    public AstNode visitPrimariaEntero(PigParser.PrimariaEnteroContext ctx)
    {
        return new LiteralExpression(ctx.getText(), DataType.NUMERUS, line(ctx), column(ctx));
    }

    @Override
    public AstNode visitPrimariaDecimal(PigParser.PrimariaDecimalContext ctx)
    {
        return new LiteralExpression(ctx.getText(), DataType.DECIMALIS, line(ctx), column(ctx));
    }

    @Override
    public AstNode visitPrimariaTexto(PigParser.PrimariaTextoContext ctx)
    {
        return new LiteralExpression(ctx.getText(), DataType.TEXTUM, line(ctx), column(ctx));
    }

    @Override
    public AstNode visitPrimariaCaracter(PigParser.PrimariaCaracterContext ctx)
    {
        return new LiteralExpression(ctx.getText(), DataType.LITTERA, line(ctx), column(ctx));
    }

    @Override
    public AstNode visitPrimariaVerdadero(PigParser.PrimariaVerdaderoContext ctx)
    {
        return new LiteralExpression(ctx.getText(), DataType.BOOLEANO, line(ctx), column(ctx));
    }

    @Override
    public AstNode visitPrimariaFalso(PigParser.PrimariaFalsoContext ctx)
    {
        return new LiteralExpression(ctx.getText(), DataType.BOOLEANO, line(ctx), column(ctx));
    }

    @Override
    public AstNode visitPrimariaIdentificador(PigParser.PrimariaIdentificadorContext ctx)
    {
        return new IdentifierExpression(ctx.getText(), line(ctx), column(ctx));
    }

    /** Parentheses add no node: the AST already carries the grouping. */
    @Override
    public AstNode visitPrimariaAgrupacion(PigParser.PrimariaAgrupacionContext ctx)
    {
        return visit(ctx.expresion());
    }

    @Override
    public AstNode visitLiteralConNombre(PigParser.LiteralConNombreContext ctx)
    {
        List<String> fieldNames = new ArrayList<>();
        List<Expression> values = new ArrayList<>();

        for (PigParser.CampoLiteralContext field : ctx.campoLiteral())
        {
            fieldNames.add(field.ID().getText());
            values.add(expression(field.expresion()));
        }
        return new CompositeLiteralExpression(fieldNames, values, line(ctx), column(ctx));
    }

    @Override
    public AstNode visitLiteralPosicional(PigParser.LiteralPosicionalContext ctx)
    {
        return new CompositeLiteralExpression(null, expressionList(ctx.listaExpresiones()),
                line(ctx), column(ctx));
    }

    @Override
    public AstNode visitLlamadaFuncion(PigParser.LlamadaFuncionContext ctx)
    {
        return new FunctionCallExpression(ctx.ID().getText(),
                expressionList(ctx.listaExpresiones()), line(ctx), column(ctx));
    }

    /* =================================================================
     * Support
     * ================================================================= */
    private AstNode child(ParseTree ctx)
    {
        return ctx == null ? null : visit(ctx);
    }

    private Expression expression(ParseTree ctx)
    {
        return child(ctx) instanceof Expression expression ? expression : null;
    }

    private Block block(ParseTree ctx)
    {
        return child(ctx) instanceof Block block ? block : null;
    }

    private List<AstNode> statements(List<PigParser.InstruccionContext> contexts)
    {
        List<AstNode> statements = new ArrayList<>();

        for (PigParser.InstruccionContext statement : contexts)
        {
            add(statements, visit(statement));
        }
        return statements;
    }

    private List<Expression> expressionList(PigParser.ListaExpresionesContext ctx)
    {
        List<Expression> values = new ArrayList<>();

        if (ctx != null)
        {
            for (PigParser.ExpresionContext item : ctx.expresion())
            {
                Expression value = expression(item);
                if (value != null)
                {
                    values.add(value);
                }
            }
        }
        return values;
    }

    private List<Parameter> parameters(PigParser.ListaParametrosContext ctx)
    {
        List<Parameter> declared = new ArrayList<>();

        if (ctx != null)
        {
            for (PigParser.ParametroContext parameter : ctx.parametro())
            {
                if (visit(parameter) instanceof Parameter declaration)
                {
                    declared.add(declaration);
                }
            }
        }
        return declared;
    }

    private void add(List<AstNode> target, AstNode node)
    {
        if (node != null)
        {
            target.add(node);
        }
    }

    private int line(ParserRuleContext ctx)
    {
        Token token = ctx.getStart();
        return token == null ? 0 : token.getLine();
    }

    private int column(ParserRuleContext ctx)
    {
        Token token = ctx.getStart();
        return token == null ? 0 : token.getCharPositionInLine() + 1;
    }
}
