package com.dmaldonado.contacto_3xtrat3rr3str3d.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Pure I/O helpers: no dialogs and no JavaFX.
 *
 * FileController owns the conversation with the user; this only touches the
 * disk, which is what makes both halves testable on their own.
 */
public final class FileManager
{
    private FileManager()
    {
    }

    public static String read(Path path) throws IOException
    {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public static void write(Path path, String content) throws IOException
    {
        if (path.getParent() != null)
        {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    /** Adds the extension when the user did not type it in the dialog. */
    public static Path ensureExtension(Path path, String extension)
    {
        String name = path.getFileName().toString();

        if (name.toLowerCase().endsWith("." + extension))
        {
            return path;
        }
        return path.resolveSibling(name + "." + extension);
    }

    /** Swaps the source extension for .c keeping the base name. */
    public static Path outputPathFor(Path sourcePath)
    {
        String name = sourcePath.getFileName().toString();
        int    dot  = name.lastIndexOf('.');
        String base = (dot > 0) ? name.substring(0, dot) : name;

        return sourcePath.resolveSibling(base + "." + Constants.OUTPUT_EXTENSION);
    }
}
