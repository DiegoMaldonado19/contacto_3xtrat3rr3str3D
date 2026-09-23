package com.dmaldonado.contacto_3xtrat3rr3str3d.controller;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.CompilerPipeline;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.CompilerPipeline.CompilationResult;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.errors.CompilerError;
import com.dmaldonado.contacto_3xtrat3rr3str3d.util.Constants;
import com.dmaldonado.contacto_3xtrat3rr3str3d.view.AstTreeBuilder;
import com.dmaldonado.contacto_3xtrat3rr3str3d.view.MainView;
import java.nio.file.Path;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

/**
 * EL PUENTE (MVC). Unico punto donde la vista y el modelo se encuentran: la
 * vista no sabe de ANTLR y CompilerPipeline no sabe de JavaFX.
 *
 * No hay un CompilerController envolviendo a CompilerPipeline: el modelo ya corre
 * el pipeline y ya devuelve un CompilationResult agnostico de la interfaz.
 */
public class ApplicationController
{
    private final MainView         view;
    private final Stage            stage;
    private final CompilerPipeline compiler = new CompilerPipeline();
    private final FileController   files;

    private CompilationResult lastResult;

    public ApplicationController(MainView view, Stage stage)
    {
        this.view  = view;
        this.stage = stage;
        this.files = new FileController(stage);

        registerEvents();
        showFileName();
    }

    private void registerEvents()
    {
        view.getCompileButton().setOnAction(event -> compile());
        view.getNewButton().setOnAction(event -> newFile());
        view.getOpenButton().setOnAction(event -> open());
        view.getSaveButton().setOnAction(event -> save());
        view.getExportButton().setOnAction(event -> exportGeneratedC());

        // Doble clic en un error lleva el cursor hasta el.
        view.getErrorTable().setRowFactory(table ->
        {
            TableRow<CompilerError> row = new TableRow<>();

            row.setOnMouseClicked(event ->
            {
                if (event.getClickCount() == 2 && !row.isEmpty())
                {
                    jumpTo(row.getItem());
                }
            });
            return row;
        });
    }

    /* ================================================================
     * Compilation
     * ================================================================ */

    private void compile()
    {
        lastResult = compiler.compile(view.getCodeArea().getText(), files.getCurrentPath());

        view.showErrors(lastResult.errors());
        view.showSymbols(lastResult.symbolTable().getAllSymbols());

        // El enunciado prohibe graficar y generar codigo de un programa con
        // errores: se limpian ambos paneles en vez de dejar uno viejo en pantalla.
        view.showAst(lastResult.isValid() ? AstTreeBuilder.build(lastResult.ast()) : null);
        view.showQuadruples(lastResult.quadruples());
        view.showGeneratedC(lastResult.cCode());

        // Un .y valido no trae C: no tiene MAIOR que ejecutar.
        boolean translated = !lastResult.cCode().isEmpty();
        view.getExportButton().setDisable(!translated);

        // La pila se muestra compile o no: el archivo invalido es justo aquel
        // cuyos pasos ERROR hay que leer.
        view.getProcessStackPanel().load(lastResult.steps());

        view.setStatus(summaryOf(lastResult), lastResult.isValid());
        view.showResultTab(translated);
    }

    private static String summaryOf(CompilationResult result)
    {
        String steps = result.steps().size() + " paso(s) de analisis";

        if (result.isValid())
        {
            return "Compilacion exitosa  |  " + result.symbolTable().getAllSymbols().size()
                 + " simbolo(s)  |  " + result.quadruples().size() + " cuarteta(s)  |  " + steps;
        }
        return "Compilacion con " + result.errors().size() + " error(es)  |  " + steps;
    }

    private void jumpTo(CompilerError error)
    {
        CodeArea codeArea = view.getCodeArea();

        // Un error de un archivo importado no esta en este editor: se avisa donde esta.
        if (!error.getSource().isEmpty() && !error.getSource().equals(files.getCurrentFileName()))
        {
            view.setStatus("El error esta en el archivo importado " + error.getSource());
            return;
        }
        if (codeArea.getParagraphs().isEmpty())
        {
            return;
        }

        // El error es 1-based y el editor 0-based, y hay que acotar: un error al
        // final del archivo puede traer una columna mayor al largo de su linea.
        int paragraph = Math.max(0, Math.min(error.getLine() - 1, codeArea.getParagraphs().size() - 1));
        int column    = Math.max(0, Math.min(error.getColumn() - 1, codeArea.getParagraphLength(paragraph)));

        codeArea.moveTo(paragraph, column);
        codeArea.requestFollowCaret();
        codeArea.requestFocus();
    }

    /* ================================================================
     * Files
     * ================================================================ */

    private void newFile()
    {
        files.newFile();
        view.getCodeArea().clear();
        view.setStatus("Nuevo archivo");
        showFileName();
    }

    private void open()
    {
        String content = files.open();

        if (content != null)
        {
            view.getCodeArea().replaceText(content);
            showFileName();
            compile();
        }
    }

    private void save()
    {
        Path saved = files.save(view.getCodeArea().getText());

        if (saved != null)
        {
            showFileName();
            view.setStatus("Guardado en " + saved.toAbsolutePath());
        }
    }

    private void exportGeneratedC()
    {
        if (lastResult == null || lastResult.cCode().isEmpty())
        {
            return;
        }

        Path saved = files.saveGeneratedC(view.getGeneratedCText());   // lo que el usuario ve

        if (saved != null)
        {
            view.setStatus("Codigo C descargado en " + saved.toAbsolutePath());
        }
    }

    private void showFileName()
    {
        String name = files.getCurrentFileName();

        view.setFileName(name);
        stage.setTitle(Constants.APP_TITLE + "  -  " + name);
    }
}
