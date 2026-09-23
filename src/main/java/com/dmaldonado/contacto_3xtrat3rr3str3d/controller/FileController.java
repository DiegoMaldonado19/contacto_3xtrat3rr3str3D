package com.dmaldonado.contacto_3xtrat3rr3str3d.controller;

import com.dmaldonado.contacto_3xtrat3rr3str3d.util.Constants;
import com.dmaldonado.contacto_3xtrat3rr3str3d.util.FileManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * The three verbs the statement asks for: abrir, guardar and descargar.
 *
 * It remembers the current path, so "Guardar" reuses it and only the first save
 * of a brand new file opens a dialog. The generated C is written next to its
 * source with the same base name and the .c extension.
 */
public class FileController
{
    private static final Logger LOGGER = Logger.getLogger(FileController.class.getName());

    private final Window owner;
    private Path         currentPath;

    public FileController(Window owner)
    {
        this.owner = owner;
    }

    public void newFile()
    {
        currentPath = null;
    }

    public String getCurrentFileName()
    {
        return currentPath == null ? "Sin archivo" : currentPath.getFileName().toString();
    }

    /** Null for a file that was never saved: the compiler needs it to resolve imports. */
    public Path getCurrentPath()
    {
        return currentPath;
    }

    /** @return the content of the opened file, or null when it was cancelled. */
    public String open()
    {
        File selected = createChooser("Abrir archivo fuente",
                "Archivos fuente (*.pig, *.y, *.z)", Constants.SOURCE_EXTENSIONS)
                .showOpenDialog(owner);

        if (selected == null)
        {
            return null;
        }

        try
        {
            currentPath = selected.toPath();
            return FileManager.read(currentPath);
        }
        catch (IOException exception)
        {
            currentPath = null;   // it was never really opened
            report("No se pudo abrir el archivo", selected.toPath(), exception);
            return null;
        }
    }

    /** @return where it was written, or null if it was cancelled or failed. */
    public Path save(String content)
    {
        if (currentPath == null)
        {
            return saveAs(content, Constants.MAIN_EXTENSION, "Archivos fuente (*.pig, *.y, *.z)");
        }
        return write(currentPath, content);
    }

    public Path saveAs(String content, String extension, String description)
    {
        File selected = createChooser("Guardar archivo", description, List.of(extension))
                .showSaveDialog(owner);

        if (selected == null)
        {
            return null;
        }

        Path destination = FileManager.ensureExtension(selected.toPath(), extension);

        if (Constants.SOURCE_EXTENSIONS.contains(extension))
        {
            currentPath = destination;
        }
        return write(destination, content);
    }

    /** Downloads the generated C next to the source, with the same base name. */
    public Path saveGeneratedC(String content)
    {
        if (currentPath != null)
        {
            return write(FileManager.outputPathFor(currentPath), content);
        }
        return saveAs(content, Constants.OUTPUT_EXTENSION, "Codigo C (*.c)");
    }

    /**
     * A successful save reports through the status bar, not through a dialog:
     * a modal window on every save is one extra click every time, and while it
     * is open the main window cannot even be closed. A failure DOES open one,
     * because that is the case the user must not miss.
     */
    private Path write(Path path, String content)
    {
        try
        {
            FileManager.write(path, content);
            return path;
        }
        catch (IOException exception)
        {
            report("No se pudo guardar el archivo", path, exception);
            return null;
        }
    }

    private FileChooser createChooser(String title, String description, List<String> extensions)
    {
        FileChooser chooser = new FileChooser();

        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description,
                extensions.stream().map(extension -> "*." + extension).toList()));

        // FileChooser throws if the initial directory no longer exists, which is
        // exactly what happens when the last file was opened from a removed USB.
        if (currentPath != null && currentPath.getParent() != null
                && Files.isDirectory(currentPath.getParent()))
        {
            chooser.setInitialDirectory(currentPath.getParent().toFile());
        }
        return chooser;
    }

    /**
     * Rule 6 of CLAUDE.md: the dialog tells the user what failed in one line,
     * and the log keeps the whole exception. Logger prints the throwable with
     * its stack trace, so the message, the file and the line number are all
     * recorded and none of it is swallowed.
     */
    private void report(String message, Path path, IOException exception)
    {
        LOGGER.log(Level.SEVERE, message + ": " + path.toAbsolutePath(), exception);

        Alert alert = new Alert(Alert.AlertType.ERROR, message + ":\n" + exception.getMessage());

        alert.initOwner(owner);
        alert.showAndWait();
    }
}
