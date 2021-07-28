package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import processor.methodextract.MethodApiGetter;
import processor.methodextract.MethodExtractDialog;
import view.dialog.DepsConfigDialog;
import view.dialog.MyRefactoringDialog;

import java.util.List;

public class ExtraInterfaceAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
        PsiClass topLevelClass = PsiUtil.getTopLevelClass(psiElement);

        DepsConfigDialog dialog = new DepsConfigDialog(project);
        dialog.setAfterOkAction(new Runnable() {
            @Override
            public void run() {
                //先进行下依赖的分析计算
                List<PsiMethod> needExtract = null;
//                try {
//                    needExtract = ProgressManager.getInstance().run(new Task.WithResult<List<PsiMethod>, Exception>(project, "依赖查找中", true) {
//                        @Override
//                        protected List<PsiMethod> compute(@NotNull ProgressIndicator indicator) throws Exception {
//                            return MethodApiGetter.genClassInterface(topLevelClass);
//                        }
//                    });
//                } catch (Exception exception) {
//                    exception.printStackTrace();
//                }
                MyRefactoringDialog methodChoose = new MethodExtractDialog(project, topLevelClass, needExtract);
                methodChoose.show();
            }
        });
        dialog.show();
    }
}
