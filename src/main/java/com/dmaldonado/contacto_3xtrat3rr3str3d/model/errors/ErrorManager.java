package com.dmaldonado.contacto_3xtrat3rr3str3d.model.errors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Acumulador unico del pipeline: el listener de sintaxis, el barrido de
 * caracteres invalidos y el analizador semantico escriben aqui.
 *
 * Un solo dueno de la lista, porque el pipeline decide donde cortar segun QUE
 * tipo de errores aparecieron.
 */
public class ErrorManager
{
    private final List<CompilerError> errors = new ArrayList<>();

    /** File the next errors belong to; the pipeline switches it for each import. */
    private String source = "";

    public void setSource(String source)
    {
        this.source = source;
    }

    private void add(ErrorType type, String description, String lexeme, int line, int column)
    {
        errors.add(new CompilerError(type, description, lexeme, line, column, source));
    }

    public void addLexical(String description, String lexeme, int line, int column)
    {
        add(ErrorType.LEXICAL, description, lexeme, line, column);
    }

    public void addSyntactic(String description, String lexeme, int line, int column)
    {
        add(ErrorType.SYNTACTIC, description, lexeme, line, column);
    }

    public void addSemantic(String description, String lexeme, int line, int column)
    {
        add(ErrorType.SEMANTIC, description, lexeme, line, column);
    }

    public boolean hasErrorsOf(ErrorType type)
    {
        return errors.stream().anyMatch(error -> error.getType() == type);
    }

    /** Sorted by file and position, which is the order the user reads them in. */
    public List<CompilerError> getErrors()
    {
        List<CompilerError> sorted = new ArrayList<>(errors);
        sorted.sort(Comparator.comparing(CompilerError::getSource)
                              .thenComparingInt(CompilerError::getLine)
                              .thenComparingInt(CompilerError::getColumn));
        return Collections.unmodifiableList(sorted);
    }
}
