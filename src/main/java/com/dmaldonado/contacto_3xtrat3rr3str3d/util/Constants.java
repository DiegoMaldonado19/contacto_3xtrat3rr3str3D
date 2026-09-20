package com.dmaldonado.contacto_3xtrat3rr3str3d.util;

import java.util.List;

/**
 * Values shared by the view and the file dialogs.
 *
 * The palette of the editor is NOT here: it lives in contacto.css, because
 * the one thing that reads it is the style sheet of the CodeArea.
 */
public final class Constants
{
    /** El lenguaje de entrada: un .pig importa los .y y .z que necesite. */
    public static final String MAIN_EXTENSION = "pig";

    /** Los tres lenguajes que el editor abre y colorea. */
    public static final List<String> SOURCE_EXTENSIONS = List.of("pig", "y", "z");

    /** La salida del compilador: C compilable con gcc. */
    public static final String OUTPUT_EXTENSION = "c";

    public static final String APP_TITLE = "Contacto 3xtrat3rr3str3D  -  Compilador a codigo de tres direcciones";

    private Constants()
    {
    }
}
