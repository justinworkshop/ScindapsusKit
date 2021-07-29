package view.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.extractSuperclass.JavaExtractSuperBaseDialog;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class CustomRefactorDialog extends JavaExtractSuperBaseDialog {
    public CustomRefactorDialog(Project project, PsiClass sourceClass, List<MemberInfo> members, String refactoringName) {
        super(project, sourceClass, members, refactoringName);
        init();
    }

    @Override
    protected String getDocCommentPanelName() {
        return null;
    }

    @Override
    protected String getExtractedSuperNameNotSpecifiedMessage() {
        return null;
    }

    @Override
    protected BaseRefactoringProcessor createProcessor() {
        return null;
    }

    @Override
    protected int getDocCommentPolicySetting() {
        return 0;
    }

    @Override
    protected void setDocCommentPolicySetting(int i) {

    }

    @Override
    protected String getTopLabelText() {
        return null;
    }

    @Override
    protected String getClassNameLabelText() {
        return null;
    }

    @Override
    protected String getPackageNameLabelText() {
        return null;
    }

    @NotNull
    @Override
    protected String getEntityName() {
        return null;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return null;
    }
}
