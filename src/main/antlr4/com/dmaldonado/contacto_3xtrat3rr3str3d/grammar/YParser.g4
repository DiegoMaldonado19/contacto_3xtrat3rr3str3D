parser grammar YParser;

options { tokenVocab = YLexer; }

/*
 * ESQUELETO (fase 1). Hoy solo existe para que ANTLR genere el visitor y para
 * comprobar que el lexer entrega INDENT/DEDENT donde corresponde.
 *
 * La gramatica real se escribe en la fase 4:
 *   Docs/04-Fase-4-Y-y-Zetariano.md
 *
 * Reglas ya decididas y que no hay que volver a discutir:
 *   - %estructuras es opcional, %funciones es obligatoria
 *   - las estructuras SOLO se declaran con 'estructura Nombre:' y campos
 *     indentados (aclaracion de la auxiliar, 5/09/2026)
 *   - los arreglos se pasan como '[] entero arr' y las estructuras como
 *     '{} MiEstructura s', ambos por referencia
 */
programa : .*? EOF ;
