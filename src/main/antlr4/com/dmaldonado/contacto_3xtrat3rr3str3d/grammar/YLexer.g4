lexer grammar YLexer;

/*
 * Y? usa la sangria para delimitar bloques, como Python. ANTLR no trae eso:
 * NUEVA_LINEA consume el salto y la sangria de la linea siguiente, y desde su
 * accion se encolan los INDENT/DEDENT que el parser si vera.
 *
 * Los dos tokens se declaran en 'tokens' porque no tienen regla propia: nacen
 * en codigo, no del texto.
 */
tokens { INDENT, DEDENT }

@lexer::header {
import java.util.ArrayDeque;
import java.util.Deque;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.IntStream;
}

@lexer::members {
    /** Tokens fabricados que aun no se han entregado al parser. */
    private final Deque<Token>   pending = new ArrayDeque<>();

    /** Anchos de sangria abiertos; la cima es el bloque actual. */
    private final Deque<Integer> indents = new ArrayDeque<>();

    /**
     * Se entrega primero lo encolado. Cuando no hay nada encolado se pide el
     * siguiente token real: si es NUEVA_LINEA su accion habra llenado la cola,
     * y esos INDENT/DEDENT saldran en las llamadas siguientes, justo despues
     * del salto de linea que los provoco.
     */
    @Override
    public Token nextToken()
    {
        if (!pending.isEmpty())
        {
            return pending.poll();
        }

        Token token = super.nextToken();

        if (token.getType() == EOF)
        {
            // Un archivo puede terminar dentro de varios bloques abiertos: hay
            // que cerrarlos todos antes del EOF o el parser nunca reduce.
            while (!indents.isEmpty())
            {
                indents.pop();
                pending.add(makeToken(DEDENT));
            }
            pending.add(token);
            return pending.poll();
        }
        return token;
    }

    /**
     * El tabulador avanza hasta el siguiente multiplo de 8, como Python. Mezclar
     * tabuladores y espacios en el mismo archivo sigue siendo mala idea, pero al
     * menos aqui la cuenta es la que el usuario espera.
     */
    private void handleNewLine()
    {
        int next = _input.LA(1);

        // Linea en blanco o que solo lleva comentario: no abre ni cierra bloque.
        if (next == '\r' || next == '\n' || next == '/' || next == IntStream.EOF)
        {
            return;
        }

        String text  = getText();
        int    width = 0;

        for (int i = 0; i < text.length(); i++)
        {
            char character = text.charAt(i);

            if (character == ' ')
            {
                width++;
            }
            else if (character == '\t')
            {
                width += 8 - (width % 8);
            }
        }

        int current = indents.isEmpty() ? 0 : indents.peek();

        if (width > current)
        {
            indents.push(width);
            pending.add(makeToken(INDENT));
        }
        else
        {
            while (!indents.isEmpty() && indents.peek() > width)
            {
                indents.pop();
                pending.add(makeToken(DEDENT));
            }
        }
    }

    /** Token de ancho cero en la posicion actual: no tapa texto del archivo. */
    private Token makeToken(int type)
    {
        CommonToken token = new CommonToken(type, "");

        token.setLine(getLine());
        token.setCharPositionInLine(getCharPositionInLine());
        token.setStartIndex(getCharIndex());
        token.setStopIndex(getCharIndex() - 1);
        token.setChannel(DEFAULT_TOKEN_CHANNEL);
        return token;
    }
}

/* ---------- 1. Marcadores de seccion --------------------------------- */
SECCION_ESTRUCTURAS : '%estructuras' ;   // opcional
SECCION_FUNCIONES   : '%funciones' ;     // obligatoria

/* ---------- 2. Palabras reservadas ---------------------------------- */
// Declaracion
ESTRUCTURA : 'estructura' ;
DEFINIR    : 'definir' ;
RETORNAR   : 'retornar' ;

// Tipos
ENTERO_T   : 'entero' ;
CADENA_T   : 'cadena' ;
FLOTANTE_T : 'flotante' ;
CARACTER_T : 'caracter' ;
BOOL_T     : 'bool' ;
VERDADERO  : 'verdadero' ;
FALSO      : 'falso' ;

// Control de flujo
SI         : 'si' ;
ENTONCES   : 'entonces' ;
SINO       : 'sino' ;
CONTRARIO  : 'contrario' ;
ELEGIR     : 'elegir' ;
CASO       : 'caso' ;
SIEMPRE    : 'siempre' ;
ROMPER     : 'romper' ;
CONTINUAR  : 'continuar' ;
PARA       : 'para' ;
MIENTRAS   : 'mientras' ;
HACER      : 'hacer' ;

// Funciones del sistema: no se declaran
IMPRIMIR   : 'imprimir' ;
LEER       : 'leer' ;

/* ---------- 3. Operadores de 2 caracteres --------------------------- */
FLECHA      : '->' ;                 // tipo de retorno: definir f() -> entero
INCREMENTO  : '++' ;
DECREMENTO  : '--' ;
IGUALDAD    : '==' ;
DIFERENTE   : '!=' ;
MENOR_IGUAL : '<=' ;
MAYOR_IGUAL : '>=' ;
AND         : '&&' ;
OR          : '||' ;

/* ---------- 4. Operadores de 1 caracter ----------------------------- */
MAS        : '+' ;
MENOS      : '-' ;
POR        : '*' ;
DIVISION   : '/' ;
MENOR      : '<' ;
MAYOR      : '>' ;
ASIGNACION : '=' ;
NEGACION   : '!' ;

/* ---------- 5. Signos de puntuacion --------------------------------- */
PAR_IZQ    : '(' ;
PAR_DER    : ')' ;
LLAVE_IZQ  : '{' ;
LLAVE_DER  : '}' ;
COR_IZQ    : '[' ;
COR_DER    : ']' ;
PUNTO_COMA : ';' ;
DOS_PUNTOS : ':' ;
COMA       : ',' ;
PUNTO      : '.' ;

/* ---------- 6. Literales -------------------------------------------- */
DECIMAL   : DIGITO+ '.' DIGITO+ ;    // antes que ENTERO
ENTERO    : DIGITO+ ;
TEXTO     : '"' ( ESCAPE | ~["\\\r\n] )* '"' ;
CARACTER  : '\'' ( ESCAPE | ~['\\\r\n] ) '\'' ;

/* ---------- 7. Identificadores -------------------------------------- */
ID : ( LETRA | '_' ) ( LETRA | DIGITO | '_' )* ;

/* ---------- 8. Sangria y ruido ---------------------------------------
 * NUEVA_LINEA se queda con el salto Y con la sangria de la linea siguiente,
 * porque para decidir el INDENT hay que verlas juntas.
 *
 * ponytail: la sangria dentro de parentesis o corchetes abiertos tambien
 * cuenta. Ningun ejemplo del enunciado parte una expresion en varias lineas;
 * si aparece, llevar la cuenta de la profundidad y salir temprano.
 * -------------------------------------------------------------------- */
NUEVA_LINEA : ( '\r'? '\n' | '\r' ) [ \t]* { handleNewLine(); } -> channel(HIDDEN) ;

COMENTARIO_LINEA  : '//' ~[\r\n]* -> channel(HIDDEN) ;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> channel(HIDDEN) ;
ESPACIOS          : [ \t\f]+      -> channel(HIDDEN) ;

/* ---------- 9. Errores lexicos --------------------------------------- */
TEXTO_SIN_CERRAR      : '"'  ( ESCAPE | ~["\\\r\n] )* ;
CARACTER_SIN_CERRAR   : '\'' ( ESCAPE | ~['\\\r\n] )? ;
COMENTARIO_SIN_CERRAR : '/*' ( ~'*' | '*' ~'/' )* '*'? ;
CARACTER_INVALIDO     : . ;

/* ---------- Fragmentos ---------------------------------------------- */
fragment LETRA  : [a-zA-ZáéíóúÁÉÍÓÚñÑ] ;
fragment DIGITO : [0-9] ;
fragment ESCAPE : '\\' [nrt"'\\] ;
