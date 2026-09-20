parser grammar ZParser;

options { tokenVocab = ZLexer; }

/*
 * ESQUELETO (fase 1). Hoy solo existe para que ANTLR genere el visitor.
 *
 * La gramatica real se escribe en la fase 4:
 *   Docs/04-Fase-4-Y-y-Zetariano.md
 *
 * Puntos que ya se sabe que van a doler y hay que probar con un caso dedicado:
 *   - dangling else con 'if' sin llaves anidados; el 'else' se asocia al 'if'
 *     mas cercano
 *   - 'switch' con fallthrough: el 'break' es opcional
 *   - 'for ( ; ; )' con los tres parametros vacios
 */
programa : .*? EOF ;
