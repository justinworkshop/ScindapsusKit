package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import utils.ClassNameUtils;
import utils.Util;

public class CreateExtendClass extends AnAction {
    private PsiElementFactory elementFactory;
    private Project project;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        project = anActionEvent.getProject();
        elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
        Editor editor = anActionEvent.getData(PlatformDataKeys.EDITOR);
        PsiFile psiFileInEditor = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiClass targetClass = Util.getTargetClass(editor, psiFileInEditor);
        testCreateClass(project, targetClass);

        testCreateClassFile(targetClass);
    }

    private void testCreateClass(Project project, PsiClass psiClass) {
        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                // 创建父类
                PsiClass parentPsiClass = createParentClass("Animal");
                // 创建子类
                PsiClass childPsiClass = createChildClass("Dog", parentPsiClass);
                psiClass.add(parentPsiClass);
                psiClass.add(childPsiClass);
            }
        });
    }

    private PsiClass createParentClass(String className) {
        PsiClass parentPsiClass = elementFactory.createClass(className);
        return parentPsiClass;
    }

    private PsiClass createChildClass(String className, PsiClass parentPsiClass) {
        PsiClass chileClass = elementFactory.createClass(className);
        // 父类的类型
        PsiClassType parentType = PsiClassType.getTypeByName(parentPsiClass.getName(), project, GlobalSearchScope.fileScope(parentPsiClass.getContainingFile()));
        // 继承父类
        chileClass.getExtendsList().add(elementFactory.createReferenceElementByType(parentType));
        return chileClass;
    }

    private void testCreateClassFile(PsiClass targetClass) {
        PsiDirectory directory = PsiDirectoryFactory.getInstance(project).createDirectory(project.getProjectFile());
        directory = targetClass.getContainingFile().getParent();
        System.out.printf("directory: " + directory.toString());
        JavaDirectoryService javaDirectoryService = JavaDirectoryService.getInstance();
        // 创建Class
        javaDirectoryService.createClass(directory, "Hello");
        // 创建Interface
        javaDirectoryService.createInterface(ClassNameUtils.createDirectory(directory, "api"), "ICallback");
    }

    private void test() {

    }
}
