package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import processor.staticmethod.StaticMethodApiGen;
import processor.staticmethod.StaticMethodReplaceDialog;
import view.dialog.DepsConfigDialog;
import view.dialog.MyRefactoringDialog;

import java.util.List;

public class StaticQRouteApiReplaceAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        PsiElement element = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
        // 根据方法名获得class, MainActivity
        PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        if (psiClass == null) {
            Messages.showMessageDialog(project, "请选择一个类", "Error", Messages.getErrorIcon());
        }

//        DepsConfigDialog dialog = new DepsConfigDialog(project);
//        dialog.setAfterOkAction(new Runnable() {
//            @Override
//            public void run() {
                //先进行下依赖的分析计算

                List<PsiMethod> needExtract = null;
                try {
                    needExtract = ProgressManager.getInstance().run(new Task.WithResult<List<PsiMethod>, Exception>(project, "依赖查找中", true) {
                        @Override
                        protected List<PsiMethod> compute(@NotNull ProgressIndicator indicator) throws Exception {
                            return StaticMethodApiGen.getExtractMethodList(psiClass);
                        }
                    });
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                MyRefactoringDialog methodChoose = new StaticMethodReplaceDialog(project, psiClass, needExtract);
                methodChoose.show();
//            }
//        });
//        dialog.show();
//        ProgressManager.getInstance().run(new Task.Modal(project, "查找替换", true) {
//            @Override
//            public void run(@NotNull ProgressIndicator indicator) {
//                indicator.setText("查找替换中");
//                indicator.setIndeterminate(true);
//                ApiReplaceOperator.replaceStaticCall(stmt, newClz);
//
//                ApplicationManager.getApplication().invokeLater(() -> {
//                    Messages.showMessageDialog(project, "替换成功", "Information", Messages.getInformationIcon());
//                });
//            }
//        });

    }
}
