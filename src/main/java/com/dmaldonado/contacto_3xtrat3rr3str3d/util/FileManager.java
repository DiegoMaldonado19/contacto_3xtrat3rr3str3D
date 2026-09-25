package com.dmaldonado.contacto_3xtrat3rr3str3d.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

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

    /** A folder's entries, folders first and then by name, the way a file explorer lists them. */
    public static List<Path> list(Path folder) throws IOException
    {
        try (Stream<Path> entries = Files.list(folder))
        {
            return entries.sorted(Comparator.comparing((Path entry) -> !Files.isDirectory(entry))
                            .thenComparing(entry -> entry.getFileName().toString().toLowerCase()))
                          .toList();
        }
    }

    /** "Descargar" a whole folder: a copy of everything inside it, subfolders included. */
    public static void copyFolder(Path source, Path target) throws IOException
    {
        try (Stream<Path> paths = Files.walk(source))
        {
            for (Path path : (Iterable<Path>) paths::iterator)
            {
                Path destination = target.resolve(source.relativize(path).toString());

                if (Files.isDirectory(path))
                {
                    Files.createDirectories(destination);
                }
                else
                {
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /** The first file with that name under a folder; null when there is none. */
    public static Path find(Path folder, String fileName) throws IOException
    {
        try (Stream<Path> paths = Files.walk(folder))
        {
            return paths.filter(path -> path.getFileName().toString().equals(fileName))
                        .findFirst()
                        .orElse(null);
        }
    }

    /** "Pila.z" gives "z"; a name without a dot gives "". */
    public static String extensionOf(String fileName)
    {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
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
