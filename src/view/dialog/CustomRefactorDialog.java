package view.dialog;

import com.intellij.java.refactoring.JavaRefactoringBundle;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.JavaRefactoringSettings;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.classMembers.DelegatingMemberInfoModel;
import com.intellij.refactoring.extractInterface.ExtractInterfaceHandler;
import com.intellij.refactoring.extractInterface.ExtractInterfaceProcessor;
import com.intellij.refactoring.extractSuperclass.ExtractSuperBaseProcessor;
import com.intellij.refactoring.extractSuperclass.JavaExtractSuperBaseDialog;
import com.intellij.refactoring.ui.MemberSelectionPanel;
import com.intellij.refactoring.util.DocCommentPolicy;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import view.ui.StaticMemberSelectionPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public abstract class CustomRefactorDialog extends JavaExtractSuperBaseDialog {

    public static List<PsiMethod> outDependentList;

    public CustomRefactorDialog(@Nullable Project project, PsiClass clz, List<MemberInfo> infos, List<PsiMethod> hasOutDeps) {
        super(project, clz, infos, ExtractInterfaceHandler.getRefactoringName());
        CustomRefactorDialog.outDependentList = hasOutDeps;
        init();
        myDocCommentPanel.setVisible(false);
    }

    @Override
    protected String getClassNameLabelText() {
        return isExtractSuperclass()
                ? RefactoringBundle.message("interface.name.prompt")
                : RefactoringBundle.message("rename.implementation.class.to");
    }

    @Override
    protected String getPackageNameLabelText() {
        return isExtractSuperclass()
                ? RefactoringBundle.message("package.for.new.interface")
                : RefactoringBundle.message("package.for.original.class");
    }

    @NotNull
    @Override
    protected String getEntityName() {
        return RefactoringBundle.message("extractSuperInterface.interface");
    }

    @Override
    protected String getTopLabelText() {
        return RefactoringBundle.message("extract.interface.from");
    }

    protected String getTableTitle() {
        return RefactoringBundle.message("members.to.form.interface");
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        final MemberSelectionPanel memberSelectionPanel = new StaticMemberSelectionPanel(getTableTitle(),
                myMemberInfos, "Have deps", outDependentList);
        memberSelectionPanel.getTable()
                .setMemberInfoModel(new DelegatingMemberInfoModel<PsiMember, MemberInfo>(memberSelectionPanel.getTable().getMemberInfoModel()) {
                    @Override
                    public Boolean isFixedAbstract(MemberInfo member) {
                        return Boolean.TRUE;
                    }
                });
        panel.add(memberSelectionPanel, BorderLayout.CENTER);

        panel.add(myDocCommentPanel, BorderLayout.EAST);

        return panel;
    }

    @Override
    protected String getDocCommentPanelName() {
        return JavaRefactoringBundle.message("extractSuperInterface.javadoc");
    }

    @Override
    protected String getExtractedSuperNameNotSpecifiedMessage() {
        return RefactoringBundle.message("no.interface.name.specified");
    }

    @Override
    protected int getDocCommentPolicySetting() {
        return JavaRefactoringSettings.getInstance().EXTRACT_INTERFACE_JAVADOC;
    }

    @Override
    protected void setDocCommentPolicySetting(int policy) {
        JavaRefactoringSettings.getInstance().EXTRACT_INTERFACE_JAVADOC = policy;
    }


    /**
     * Action 拦截
     * @return null
     */
    @Override
    protected ExtractSuperBaseProcessor createProcessor() {
        return new ExtractInterfaceProcessor(myProject, false, getTargetDirectory(), getExtractedSuperName(),
                mySourceClass, getSelectedMemberInfos().toArray(new MemberInfo[0]),
                new DocCommentPolicy(getDocCommentPolicy()));
    }

    @Override
    protected String getHelpId() {
        return HelpID.EXTRACT_INTERFACE;
    }


    @Override
    protected abstract void doAction();


    @Override
    protected void customizeRadiobuttons(Box box, ButtonGroup buttonGroup) {
        box.setVisible(false);
    }
}
