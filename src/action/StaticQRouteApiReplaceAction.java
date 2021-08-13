package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import processor.staticmethod.QRouteReplaceStaticMethodDialog;
import view.dialog.CustomRefactorDialog;

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

        System.out.println("主线程：" + Thread.currentThread().getName());
        CustomRefactorDialog methodChoose = new QRouteReplaceStaticMethodDialog(project, psiClass);
        methodChoose.show();
    }
}
