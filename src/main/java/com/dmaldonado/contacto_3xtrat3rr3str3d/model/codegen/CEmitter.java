package com.dmaldonado.contacto_3xtrat3rr3str3d.model.codegen;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Cuartetas -> C que compila con gcc. Cada cuarteta es una linea de C, asi la
 * correspondencia se lee de un vistazo; el control de flujo es goto y
 * etiquetas, como en el codigo de tres direcciones.
 *
 * El prologo trae la memoria (stack, heap, P, H) y las funciones nativas. Cada
 * bloque function ... end de las cuartetas es una funcion C, con sus
 * temporales declarados como locales: eso es lo que hace segura la recursion.
 */
public final class CEmitter
{
    private static final Pattern TEMPORARY = Pattern.compile("t\\d+");
    private static final Pattern INTEGER   = Pattern.compile("-?\\d+");

    private static final Set<String> JUMPS = Set.of("goto", "if", "ifFalse");

    /** The natives take and return C values, as Docs/03 3.4 specifies. */
    private static final String RUNTIME = """
            #include <stdio.h>
            #include <stdlib.h>
            #include <string.h>

            #define MEMORY_SIZE  100000
            /* Ningun marco llega a ese tamano: el chequeo del stack deja ese margen. */
            #define FRAME_MARGIN 1000

            float stack[MEMORY_SIZE];   /* marcos de las funciones; P es la base del actual   */
            float heap[MEMORY_SIZE];    /* cadenas, arreglos y estructuras; H, la celda libre */
            float P;
            float H;

            /* ------------------------------------------------------------------
             * Nativas: el lenguaje las usa sin declararlas. Una cadena es una
             * secuencia de bytes UTF-8 en el heap, terminada en -1.
             * ------------------------------------------------------------------ */

            void checkMemory(void)
            {
                if (H > MEMORY_SIZE || P + FRAME_MARGIN > MEMORY_SIZE)
                {
                    printf("\\nError: memoria agotada (stack o heap).\\n");
                    exit(1);
                }
            }

            float reserve(int size)
            {
                float start = H;

                H = H + size;
                checkMemory();
                return start;
            }

            /* Copia un texto de C al heap y devuelve su direccion. */
            float textFromBytes(const char *text)
            {
                float start = reserve((int) strlen(text) + 1);
                int   k     = (int) start;

                for (int i = 0; text[i] != '\\0'; i++)
                {
                    heap[k++] = (unsigned char) text[i];
                }
                heap[k] = -1;
                return start;
            }

            /* Un littera es un code point; en la consola va en UTF-8. */
            void encodeUtf8(int code, char *text)
            {
                if (code < 0x80)
                {
                    text[0] = (char) code;
                    text[1] = '\\0';
                }
                else if (code < 0x800)
                {
                    text[0] = (char) (0xC0 | (code >> 6));
                    text[1] = (char) (0x80 | (code & 0x3F));
                    text[2] = '\\0';
                }
                else if (code < 0x10000)
                {
                    text[0] = (char) (0xE0 | (code >> 12));
                    text[1] = (char) (0x80 | ((code >> 6) & 0x3F));
                    text[2] = (char) (0x80 | (code & 0x3F));
                    text[3] = '\\0';
                }
                else
                {
                    text[0] = (char) (0xF0 | (code >> 18));
                    text[1] = (char) (0x80 | ((code >> 12) & 0x3F));
                    text[2] = (char) (0x80 | ((code >> 6) & 0x3F));
                    text[3] = (char) (0x80 | (code & 0x3F));
                    text[4] = '\\0';
                }
            }

            void printInt(float value)
            {
                printf("%d", (int) value);
            }

            void printFloat(float value)
            {
                printf("%g", value);
            }

            void printChar(float value)
            {
                char text[5];

                encodeUtf8((int) value, text);
                fputs(text, stdout);
            }

            void printBool(float value)
            {
                fputs(value != 0 ? "verum" : "falsus", stdout);
            }

            void printString(float pointer)
            {
                for (int i = (int) pointer; heap[i] != -1; i++)
                {
                    putchar((int) heap[i]);
                }
            }

            void printLine(void)
            {
                putchar('\\n');
            }

            float readInt(void)
            {
                int value = 0;

                if (scanf("%d", &value) != 1)
                {
                    value = 0;
                }
                return (float) value;
            }

            float readFloat(void)
            {
                float value = 0;

                if (scanf("%f", &value) != 1)
                {
                    value = 0;
                }
                return value;
            }

            float readChar(void)
            {
                char value = 0;

                if (scanf(" %c", &value) != 1)
                {
                    value = 0;
                }
                return (unsigned char) value;
            }

            /* Una linea completa: el espacio del formato salta el salto que dejo una lectura anterior. */
            float readString(void)
            {
                char text[1024];

                if (scanf(" %1023[^\\n]", text) != 1)
                {
                    text[0] = '\\0';
                }
                return textFromBytes(text);
            }

            int stringLength(float pointer)
            {
                int length = 0;

                for (int i = (int) pointer; heap[i] != -1; i++)
                {
                    length++;
                }
                return length;
            }

            float concat(float left, float right)
            {
                float start = reserve(stringLength(left) + stringLength(right) + 1);
                int   k     = (int) start;

                for (int i = (int) left; heap[i] != -1; i++)
                {
                    heap[k++] = heap[i];
                }
                for (int i = (int) right; heap[i] != -1; i++)
                {
                    heap[k++] = heap[i];
                }
                heap[k] = -1;
                return start;
            }

            /* Por contenido, no por direccion: dos literales iguales pueden vivir en celdas distintas. */
            float stringEquals(float left, float right)
            {
                int i = (int) left;
                int j = (int) right;

                while (heap[i] != -1 && heap[i] == heap[j])
                {
                    i++;
                    j++;
                }
                return heap[i] == heap[j];
            }

            float intToString(float value)
            {
                char text[16];

                snprintf(text, sizeof text, "%d", (int) value);
                return textFromBytes(text);
            }

            float floatToString(float value)
            {
                char text[32];

                snprintf(text, sizeof text, "%g", value);
                return textFromBytes(text);
            }

            float charToString(float value)
            {
                char text[5];

                encodeUtf8((int) value, text);
                return textFromBytes(text);
            }

            float boolToString(float value)
            {
                return textFromBytes(value != 0 ? "verum" : "falsus");
            }
            """;

    private CEmitter()
    {
    }

    public static String emit(List<Quadruple> code)
    {
        StringBuilder c = new StringBuilder(RUNTIME);

        c.append("\n/* ------------------------------------------------------------------\n")
         .append(" * El programa\n")
         .append(" * ------------------------------------------------------------------ */\n\n");

        // Prototipos: una funcion puede llamar a otra declarada despues, o a si misma.
        for (Quadruple quadruple : code)
        {
            if ("function".equals(quadruple.op()) && !"main".equals(quadruple.result()))
            {
                c.append("void ").append(quadruple.result()).append("(void);\n");
            }
        }

        int start = 0;

        for (int i = 0; i < code.size(); i++)
        {
            if ("end".equals(code.get(i).op()))
            {
                emitFunction(code.subList(start + 1, i), code.get(start).result(), c);
                start = i + 1;
            }
        }
        return c.toString();
    }

    /**
     * One C function. An unreferenced label is left out, so -Wall stays quiet;
     * a label that closes the function gets an empty statement, which C
     * requires after a label.
     */
    private static void emitFunction(List<Quadruple> body, String name, StringBuilder c)
    {
        Set<String> temporaries = new TreeSet<>(Comparator.comparingInt(t -> Integer.parseInt(t.substring(1))));
        Set<String> targets     = new HashSet<>();

        for (Quadruple quadruple : body)
        {
            Stream.of(quadruple.arg1(), quadruple.arg2(), quadruple.result())
                  .filter(operand -> TEMPORARY.matcher(operand).matches())
                  .forEach(temporaries::add);

            if (JUMPS.contains(quadruple.op()))
            {
                targets.add(quadruple.result());
            }
        }

        c.append('\n').append("main".equals(name) ? "int main(void)" : "void " + name + "(void)")
         .append("\n{\n");

        if (!temporaries.isEmpty())
        {
            c.append("    float ").append(String.join(", ", temporaries)).append(";\n\n");
        }

        for (int i = 0; i < body.size(); i++)
        {
            Quadruple quadruple = body.get(i);

            if (!"label".equals(quadruple.op()))
            {
                c.append("    ").append(translate(quadruple)).append('\n');
            }
            else if (targets.contains(quadruple.result()))
            {
                c.append(quadruple.result()).append(i == body.size() - 1 ? ": ;" : ":").append('\n');
            }
        }

        if ("main".equals(name))
        {
            c.append("    return 0;\n");
        }
        c.append("}\n");
    }

    private static String translate(Quadruple q)
    {
        return switch (q.op())
        {
            case "="                                             -> q.result() + " = " + q.arg1() + ";";
            case "+", "-", "*", "/", "<", ">", "<=", ">=", "==", "!=" ->
                    q.result() + " = " + q.arg1() + " " + q.op() + " " + q.arg2() + ";";
            case "div"     -> q.result() + " = (float) ((int) " + q.arg1() + " / (int) " + q.arg2() + ");";
            case "=[]"     -> q.result() + " = " + q.arg1() + "[" + index(q.arg2()) + "];";
            case "[]="     -> q.result() + "[" + index(q.arg1()) + "] = " + q.arg2() + ";";
            case "goto"    -> "goto " + q.result() + ";";
            case "if"      -> "if (" + q.arg1() + ") goto " + q.result() + ";";
            case "ifFalse" -> "if (!" + q.arg1() + ") goto " + q.result() + ";";
            case "call"    -> q.result() + "();";
            case "return"  -> "return;";
            default        -> nativeCall(q);
        };
    }

    /** Any other op names a native: result = op(arg1, arg2), leaving out the empty parts. */
    private static String nativeCall(Quadruple q)
    {
        String arguments = Stream.of(q.arg1(), q.arg2())
                                 .filter(argument -> !argument.isEmpty())
                                 .collect(Collectors.joining(", "));
        String call      = q.op() + "(" + arguments + ");";

        return q.result().isEmpty() ? call : q.result() + " = " + call;
    }

    /** An array subscript has to be an int in C; a constant already is one. */
    private static String index(String operand)
    {
        return INTEGER.matcher(operand).matches() ? operand : "(int) " + operand;
    }
}
