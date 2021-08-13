package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import processor.methodextract.MethodExtractDialog;
import utils.Util;
import view.dialog.CustomRefactorDialog;

public class ExtraInterfaceAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        Editor editor = anActionEvent.getData(PlatformDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        final PsiClass topLevelClass = Util.getTargetClass(editor, psiFile);

//        DepsConfigDialog dialog = new DepsConfigDialog(project);
//        dialog.setAfterOkAction(new Runnable() {
//            @Override
//            public void run() {
//                //先进行下依赖的分析计算
//                List<PsiMethod> needExtract = null;
////                try {
////                    needExtract = ProgressManager.getInstance().run(new Task.WithResult<List<PsiMethod>, Exception>(project, "依赖查找中", true) {
////                        @Override
////                        protected List<PsiMethod> compute(@NotNull ProgressIndicator indicator) throws Exception {
////                            return MethodApiGetter.genClassInterface(topLevelClass);
////                        }
////                    });
////                } catch (Exception exception) {
////                    exception.printStackTrace();
////                }
//                MyRefactoringDialog methodChoose = new MethodExtractDialog(project, topLevelClass, needExtract);
//                methodChoose.show();
//            }
//        });
//        dialog.show();

        CustomRefactorDialog methodChoose = new MethodExtractDialog(project, topLevelClass, null);
        methodChoose.show();
    }
}
