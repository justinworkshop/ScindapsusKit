package processor.staticmethod;

import apiOperator.ApiGen;
import apiOperator.ApiImplGen;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.extractSuperclass.ExtractSuperBaseDialog;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ClassNameUtils;
import view.dialog.CustomRefactorDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StaticMethodExtractDialog extends CustomRefactorDialog {

    public StaticMethodExtractDialog(@Nullable Project project, PsiClass clz, List<PsiMethod> hasOutDeps) {
        super(project, clz, collectMembers(clz), hasOutDeps);
    }

    private static List<MemberInfo> collectMembers(PsiClass clz) {
        return MemberInfo.extractClassMembers(clz, member -> {
            if (member instanceof PsiMethod) {
                if (PsiUtil.isLanguageLevel9OrHigher(member)) {
                    return true;
                }
                return member.hasModifierProperty(PsiModifier.PUBLIC)
                        && (PsiUtil.isLanguageLevel8OrHigher(member) || member.hasModifierProperty(PsiModifier.STATIC));
            }
            return false;
        }, true);
    }

    @Override
    protected void doAction() {
        Collection<MemberInfo> extractMember = getSelectedMemberInfos();
        String packName = getTargetPackageName();
        String newClz = getExtractedSuperName();
        PsiDirectory dir = getTargetDirectory();
        PsiClass curClz = mySourceClass;

        List<PsiMethod> methodList = new ArrayList<>();
        for (MemberInfo info : extractMember) {
            if (info.getMember() instanceof PsiMethod) {
                methodList.add((PsiMethod) info.getMember());
            }
        }

        //????????????????????????
        CommandProcessor.getInstance().executeCommand(myProject, () -> {
            try {
                preparePackage();
            }
            catch (IncorrectOperationException | ExtractSuperBaseDialog.OperationFailedException e) {
                myPackageNameField.requestFocusInWindow();
            }
        }, RefactoringBundle.message("create.directory"), null);

        ProgressManager.getInstance().run(new Task.Backgroundable(myProject, "????????????", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                WriteCommandAction.runWriteCommandAction(myProject, new Runnable() {
                    @Override
                    public void run() {
                        PsiClass psiClass = ApiGen.getInterfaceWithMemberInfo(myProject, mySourceClass, extractMember, dir, packName, newClz, ClassNameUtils.getQRouteApi(myProject), false);

                        PsiClassType type = PsiType.getTypeByName(packName + "." + newClz, psiClass.getProject(), GlobalSearchScope.allScope(psiClass.getProject()));

                        ApiImplGen.genStaticInterfaceImpl(myProject, methodList, dir, packName, mySourceClass.getName(), type);
                        ApplicationManager.getApplication().invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                StaticMethodExtractDialog.this.closeOKAction();
                            }
                        });
                    }
                });

            }
        });
    }
}
