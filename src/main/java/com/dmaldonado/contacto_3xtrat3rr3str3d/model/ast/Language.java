package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast;

/**
 * Source language of a Program. The three languages share one AST; this is
 * what lets the semantic analyzer apply the few rules that differ, such as
 * where a variable may be declared.
 */
public enum Language
{
    PIG("pig"),
    Y("y");

    private final String extension;

    Language(String extension)
    {
        this.extension = extension;
    }

    /** @return null when the extension is not a language the compiler reads. */
    public static Language fromExtension(String extension)
    {
        for (Language language : values())
        {
            if (language.extension.equals(extension))
            {
                return language;
            }
        }
        return null;
    }
}
