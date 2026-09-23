package com.dmaldonado.contacto_3xtrat3rr3str3d.model;

import com.dmaldonado.contacto_3xtrat3rr3str3d.grammar.PigLexer;
import com.dmaldonado.contacto_3xtrat3rr3str3d.grammar.PigParser;
import com.dmaldonado.contacto_3xtrat3rr3str3d.grammar.YLexer;
import com.dmaldonado.contacto_3xtrat3rr3str3d.grammar.YParser;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.analysis.PigAstBuilder;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.analysis.SemanticAnalyzer;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.analysis.YAstBuilder;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.Language;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.ImportDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.Program;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.codegen.CEmitter;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.codegen.Quadruple;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.codegen.QuadrupleGenerator;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.errors.CompilerError;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.errors.ErrorManager;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.errors.ErrorType;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.errors.SyntaxErrorListener;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.stack.ProcessStackListener;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.stack.ProcessStep;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.symbols.SymbolTable;
import com.dmaldonado.contacto_3xtrat3rr3str3d.util.FileManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Corre el pipeline completo y decide donde cortar: nada se grafica ni se
 * traduce si hay errores.
 *
 * Solo corta por errores LEXICOS, donde el flujo de tokens ya no es confiable.
 * Tras un error de sintaxis sigue: la auxiliar mezcla a proposito errores
 * sintacticos y semanticos en el mismo archivo, y cortar en el primero se
 * llevaria todos los semanticos del reporte.
 *
 * No sabe nada de la interfaz: no imprime, devuelve un CompilationResult.
 */
public class CompilerPipeline
{
    private static final Logger LOGGER = Logger.getLogger(CompilerPipeline.class.getName());

    /**
     * @param ast          null when the source did not get past the lexer.
     * @param symbolTable  empty when the AST was never built.
     * @param steps        empty only when the file was never parsed at all.
     * @param quadruples   empty unless the program is valid and has a main.
     * @param cCode        the C translation of the quadruples, or "".
     */
    public record CompilationResult(Program ast, SymbolTable symbolTable, List<ProcessStep> steps,
                                    List<CompilerError> errors, List<Quadruple> quadruples,
                                    String cCode)
    {
        /** Only a valid program may be graphed and translated. */
        public boolean isValid()
        {
            return errors.isEmpty() && ast != null;
        }
    }

    /** One front-end run: the AST (null after a lexical error) and the steps of its parse. */
    private record ParsedUnit(Program ast, List<ProcessStep> steps)
    {
    }

    /** A program and the file its errors are reported against. */
    private record Unit(String source, Program ast)
    {
    }

    /**
     * @param sourcePath where the source lives, for its language and to resolve
     *                   its imports; null for a file that was never saved.
     */
    public CompilationResult compile(String source, Path sourcePath)
    {
        ErrorManager errorManager = new ErrorManager();
        SymbolTable  symbolTable  = new SymbolTable();
        String       fileName     = sourcePath == null ? "" : sourcePath.getFileName().toString();
        String       extension    = extensionOf(fileName);

        errorManager.setSource(fileName);

        // ponytail: se reemplaza en la Fase 4, cuando Zetariano tenga front-end.
        if ("z".equals(extension))
        {
            errorManager.addSemantic("Los archivos Zetariano (.z) se compilan a partir de la Fase 4.",
                    fileName, 1, 1);
            return new CompilationResult(null, symbolTable, List.of(), errorManager.getErrors(),
                    List.of(), "");
        }

        Language   language = Objects.requireNonNullElse(Language.fromExtension(extension), Language.PIG);
        ParsedUnit root     = parse(source, language, errorManager);

        if (root.ast() == null)
        {
            return new CompilationResult(null, symbolTable, root.steps(), errorManager.getErrors(),
                    List.of(), "");
        }

        // Los importados van primero: el .pig necesita ver lo que declaran.
        List<Unit> units = loadImports(root.ast(), sourcePath, errorManager);
        units.add(new Unit(fileName, root.ast()));

        for (Unit unit : units)
        {
            errorManager.setSource(unit.source());
            guarded(() -> new SemanticAnalyzer(errorManager, symbolTable).visitProgram(unit.ast()),
                    errorManager);
        }

        List<Quadruple> quadruples = List.of();
        String          cCode      = "";

        // Solo un programa sin errores se traduce, y solo un .pig tiene MAIOR
        // que ejecutar: un .y se analiza y ahi termina.
        if (errorManager.getErrors().isEmpty() && language == Language.PIG)
        {
            quadruples = new QuadrupleGenerator().generate(units.stream().map(Unit::ast).toList(),
                                                           symbolTable.getGlobalSize());
            cCode      = CEmitter.emit(quadruples);
        }
        return new CompilationResult(root.ast(), symbolTable, root.steps(), errorManager.getErrors(),
                quadruples, cCode);
    }

    /* =================================================================
     * Front-ends
     * ================================================================= */

    private ParsedUnit parse(String source, Language language, ErrorManager errorManager)
    {
        CharStream chars = CharStreams.fromString(source == null ? "" : source);
        Lexer      lexer = switch (language)
        {
            case PIG -> new PigLexer(chars);
            case Y   -> new YLexer(chars);
        };
        lexer.removeErrorListeners();   // silence ANTLR's ConsoleErrorListener

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        // Con errores lexicos no se parsea: todo error de sintaxis posterior
        // seria ruido derivado de este.
        if (reportLexicalErrors(tokens, lexer.getVocabulary(), errorManager))
        {
            return new ParsedUnit(null, List.of());
        }

        return switch (language)
        {
            case PIG ->
            {
                PigParser                 parser = listenedBy(new PigParser(tokens), errorManager);
                PigParser.ProgramaContext tree   = parser.programa();
                yield finish(parser, tree, () -> new PigAstBuilder().build(tree), errorManager);
            }
            case Y ->
            {
                YParser                 parser = listenedBy(new YParser(tokens), errorManager);
                YParser.ProgramaContext tree   = parser.programa();
                yield finish(parser, tree, () -> new YAstBuilder().build(tree), errorManager);
            }
        };
    }

    private static <P extends Parser> P listenedBy(P parser, ErrorManager errorManager)
    {
        parser.removeErrorListeners();
        parser.addErrorListener(new SyntaxErrorListener(errorManager));
        return parser;
    }

    /** Los pasos van antes del AST: el archivo que no parsea es justo aquel cuyos pasos ERROR se leen. */
    private ParsedUnit finish(Parser parser, ParserRuleContext tree, Supplier<Program> builder,
                              ErrorManager errorManager)
    {
        ProcessStackListener stackListener = new ProcessStackListener(parser);
        ParseTreeWalker.DEFAULT.walk(stackListener, tree);

        return new ParsedUnit(guarded(builder, errorManager), stackListener.getSteps());
    }

    /**
     * No hay LexicalErrorListener a proposito: las reglas de error del lexer
     * cubren todo lo que no encaja, la ultima de ellas con "CARACTER_INVALIDO
     * : . ;", asi que el lexer nunca falla y un BaseErrorListener sobre el
     * jamas dispararia. El mensaje sale del tipo de token, no del texto; se
     * compara por NOMBRE para que sirva a todos los lexers.
     *
     * @return true if the stream holds any lexical error.
     */
    private boolean reportLexicalErrors(CommonTokenStream tokens, Vocabulary vocabulary,
                                        ErrorManager errorManager)
    {
        boolean found = false;

        for (Token token : tokens.getTokens())
        {
            String description = switch (String.valueOf(vocabulary.getSymbolicName(token.getType())))
            {
                case "TEXTO_SIN_CERRAR"           -> "Cadena sin cerrar: falta la comilla doble final.";
                case "CARACTER_SIN_CERRAR"        -> "Caracter sin cerrar: falta la comilla simple final.";
                case "COMENTARIO_SIN_CERRAR"      -> "Comentario de bloque sin cerrar: falta '*/'.";
                case "COMENTARIO_HASH_SIN_CERRAR" -> "Comentario de bloque sin cerrar: falta '##'.";
                case "CARACTER_INVALIDO"          -> "Simbolo no reconocido por el lenguaje.";
                default                           -> null;
            };

            if (description != null)
            {
                errorManager.addLexical(description, token.getText(),
                        token.getLine(), token.getCharPositionInLine() + 1);
                found = true;
            }
        }
        return found;
    }

    /**
     * El AST ya se construye aunque haya errores de sintaxis, y un null-guard
     * olvidado ante un arbol reparado no debe tumbar la interfaz: con errores
     * sintacticos previos se registra completo y se sigue con lo reunido. Sin
     * ellos es un bug real, y se relanza.
     */
    private static <T> T guarded(Supplier<T> phase, ErrorManager errorManager)
    {
        try
        {
            return phase.get();
        }
        catch (RuntimeException exception)
        {
            if (!errorManager.hasErrorsOf(ErrorType.SYNTACTIC))
            {
                throw exception;
            }
            LOGGER.log(Level.WARNING, "Fase interrumpida por un arbol reparado tras errores de sintaxis",
                    exception);
            return null;
        }
    }

    /* =================================================================
     * Imports
     * ================================================================= */

    /**
     * Resuelve cada import contra la carpeta del .pig y lo compila con su propio
     * front-end. No hace falta detectar ciclos: solo el .pig importa, y un .pig
     * no se puede importar. Un archivo importado dos veces se lee una.
     */
    private List<Unit> loadImports(Program root, Path rootPath, ErrorManager errorManager)
    {
        List<Unit> units      = new ArrayList<>();
        Set<Path>  seen       = new HashSet<>();
        String     rootSource = rootPath == null ? "" : rootPath.getFileName().toString();

        for (ImportDeclaration declaration : root.getImports())
        {
            String   extension = declaration.getExtension();
            Language language  = Language.fromExtension(extension);

            if (rootPath == null)
            {
                importError(declaration, "Guarde el archivo para poder resolver sus importaciones.",
                        errorManager);
                break;
            }
            if ("z".equals(extension))
            {
                // ponytail: se reemplaza en la Fase 4, cuando Zetariano tenga front-end.
                importError(declaration, "Los archivos Zetariano (.z) se podran importar a partir "
                        + "de la Fase 4.", errorManager);
                continue;
            }
            if (language == null || language == Language.PIG)
            {
                importError(declaration, "Solo se pueden importar archivos .y o .z, no '." + extension
                        + "'.", errorManager);
                continue;
            }

            Path file = rootPath.toAbsolutePath().getParent()
                    .resolve(declaration.getRelativePath()).normalize();

            if (!seen.add(file))
            {
                continue;
            }
            if (!Files.isRegularFile(file))
            {
                importError(declaration, "No se encontro el archivo importado '"
                        + declaration.getRelativePath() + "'.", errorManager);
                continue;
            }

            String content;

            try
            {
                content = FileManager.read(file);
            }
            catch (IOException exception)
            {
                LOGGER.log(Level.SEVERE, "No se pudo leer el archivo importado " + file, exception);
                importError(declaration, "No se pudo leer '" + declaration.getRelativePath() + "': "
                        + exception.getMessage(), errorManager);
                continue;
            }

            String source = file.getFileName().toString();

            errorManager.setSource(source);
            Program program = parse(content, language, errorManager).ast();
            errorManager.setSource(rootSource);

            if (program != null)
            {
                units.add(new Unit(source, program));
            }
        }
        return units;
    }

    private static void importError(ImportDeclaration declaration, String description,
                                    ErrorManager errorManager)
    {
        errorManager.addSemantic(description, declaration.getRelativePath(),
                declaration.getLine(), declaration.getColumn());
    }

    private static String extensionOf(String fileName)
    {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }
}
