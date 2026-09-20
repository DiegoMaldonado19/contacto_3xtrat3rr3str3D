package com.dmaldonado.contacto_3xtrat3rr3str3d.view;

import com.dmaldonado.contacto_3xtrat3rr3str3d.grammar.PigLexer;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 * El editor colorea con el PigLexer REAL. El catedratico exigio que el color
 * saliera del analisis lexico propio y no de un plugin:
 * RichTextFX solo pinta los spans.
 *
 * Por eso es la unica clase de la vista que importa ANTLR, y lo que compara es
 * el TIPO de token, nunca el texto de la palabra.
 */
public final class HighlightingCodeArea {
    private static final Logger LOGGER = Logger.getLogger(HighlightingCodeArea.class.getName());

    private static final Set<Integer> KEYWORD_TOKENS = Set.of(
            PigLexer.VARIABILES, PigLexer.VARIABILES_LOCAL, PigLexer.MUNERA,
            PigLexer.MAIOR, PigLexer.FIN_PROGRAMA, PigLexer.ESTO,
            PigLexer.SERIES, PigLexer.STRUCTURA, PigLexer.FINIS,
            PigLexer.SI, PigLexer.ALITER, PigLexer.DUM,
            PigLexer.FACERE, PigLexer.PER, PigLexer.PERGE,
            PigLexer.INTERRUMPE, PigLexer.ACTIO, PigLexer.RATIO,
            PigLexer.REDDERE, PigLexer.NON);

    private static final Set<Integer> TYPE_TOKENS = Set.of(
            PigLexer.NUMERUS, PigLexer.DECIMALIS, PigLexer.TEXTUM,
            PigLexer.LITTERA, PigLexer.BOOL, PigLexer.VERUM,
            PigLexer.FALSUS);

    private HighlightingCodeArea() {
    }

    public static CodeArea create() {
        CodeArea codeArea = new CodeArea();

        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.getStyleClass().add("source-editor");
        codeArea.textProperty().addListener((observable, oldText, newText) -> applyHighlighting(codeArea, newText));

        return codeArea;
    }

    /**
     * Regla 6 de CLAUDE.md. Sin este try/catch una excepcion aqui muere dentro
     * del listener de textProperty: el editor deja de colorear en cada tecla y
     * no queda rastro de por que. Se registra con la excepcion completa (que
     * lleva mensaje, archivo y numero de linea) y el texto se queda sin estilo
     * en vez de dejar el editor mudo.
     */
    private static void applyHighlighting(CodeArea codeArea, String text) {
        try {
            codeArea.setStyleSpans(0, computeHighlighting(text));
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "No se pudo colorear el texto del editor.", exception);
        }
    }

    /**
     * Package private para que el self check lo llame sin abrir una ventana:
     * setStyleSpans exige que los spans cubran el texto EXACTAMENTE.
     *
     * ponytail: se re-lexa el documento entero en cada tecla. Si alguna vez se
     * siente lento, alimentarlo desde codeArea.multiPlainChanges().
     */
    static StyleSpans<Collection<String>> computeHighlighting(String text) {
        // create() lanza si no se agrego ni un span, y el editor arranca vacio.
        if (text.isEmpty()) {
            return StyleSpans.<Collection<String>>singleton(Collections.emptyList(), 0);
        }

        PigLexer lexer = new PigLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners(); // half typed code is the normal state here

        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();

        // getAllTokens() conserva el canal HIDDEN: por eso la gramatica manda
        // comentarios y espacios ahi en vez de descartarlos con skip. Y como
        // ninguna regla usa skip, el flujo es contiguo y cubre todo el texto.
        //
        // La longitud sale del texto del token y NO de stopIndex - startIndex:
        // CharStreams.fromString indexa por puntos de codigo y String por
        // chars, asi que con un emoji los indices del lexer se desalinean y
        // todo lo que va detras se colorearia corrido.
        for (Token token : lexer.getAllTokens()) {
            builder.add(Collections.singleton(styleClassFor(token.getType())),
                    token.getText().length());
        }
        return builder.create();
    }

    private static String styleClassFor(int tokenType) {
        if (KEYWORD_TOKENS.contains(tokenType)) {
            return "keyword";
        }
        if (TYPE_TOKENS.contains(tokenType)) {
            return "type";
        }

        return switch (tokenType) {
            case PigLexer.ENTERO, PigLexer.DECIMAL -> "number";
            case PigLexer.TEXTO, PigLexer.CARACTER -> "string";
            case PigLexer.COMENTARIO_LINEA,
                    PigLexer.COMENTARIO_BLOQUE ->
                "comment";
            case PigLexer.TEXTO_SIN_CERRAR,
                    PigLexer.CARACTER_SIN_CERRAR,
                    PigLexer.COMENTARIO_SIN_CERRAR,
                    PigLexer.CARACTER_INVALIDO ->
                "invalid";
            default -> "plain";
        };
    }
}
