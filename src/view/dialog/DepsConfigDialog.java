package view.dialog;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import swing.inputset.InputFileSet;

import javax.swing.*;
import java.awt.*;

public class DepsConfigDialog extends DialogWrapper {

    private final InputFileSet fileSetForm;
    private Runnable afterOkRunnable;

    public DepsConfigDialog(@Nullable Project project) {
        super(project);
        fileSetForm = new InputFileSet(project);
        setTitle("选择需要定义的内部文件范围");
        init();
    }

    @Override
    protected @Nullable
    JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(fileSetForm.getRoot());
        return panel;
    }

    @Override
    protected void doOKAction() {
        if (!fileSetForm.onOkEvent()) {
            super.doOKAction();
            afterOkRunnable.run();
        }
    }

    public void setAfterOkAction(Runnable runnable) {
        this.afterOkRunnable = runnable;
    }
}