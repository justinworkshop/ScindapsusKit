package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * QRoute文件规范检查
 * 1.获取文件名
 * 2.获取当前待类的包名、类名
 * 3.生成对应Impl的包名、类名
 * 4.在Project中check是否存在此包名、类名的文件
 */
public class CheckQRouteRule extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        // 1.获取编辑区当前的PsiFile
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

        // 2.获取包名，类名
        String packageName = "";
        String className = psiFile.getName().split(".java")[0];
        // 包名/Import/WhiteBlank/PsiClass
        for (PsiElement psiElement : psiFile.getChildren()) {
            if (psiElement instanceof PsiPackageStatement) {
                PsiPackageStatement psiPackageStatement = (PsiPackageStatement) psiElement;
                packageName = psiPackageStatement.getPackageName();
                System.out.printf("qualifiedName:" + packageName);
            }

            if (psiElement instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) psiElement;
                psiClass.getMethods();
                String qualifiedName = psiClass.getQualifiedName();
                System.out.printf("qualifiedName:" + qualifiedName);
            }
        }

        // 3.生成对应Impl的包名、类名
        try {
            String implPkg = packageName + ".impl";
            String implClzName = className.split("I")[1] + "Impl";
            System.out.println("Impl类全限定名：" + implPkg + "." + implClzName);
        } catch (Exception e) {

        }


        // 4.检查文件是否存在
        VirtualFile virtualFile = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        findFileByName(packageName, className, project);


        // 测试获取project所有文件，这里包括jar内的文件，并且只有文件名，不是很合适。
//        findProjectAllFiles(project);
    }

    /**
     * 根据指定包名&类名查找文件
     *
     * @param packageName : com.example.firsttes
     * @param fileName    : MainActivity
     * @param project
     */
    private void findFileByName(String packageName, String fileName, Project project) {
        PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(packageName);
        boolean result = psiPackage.containsClassNamed(fileName);
        String msg = result ? "检查通过" : "未找到文件";
        Messages.showMessageDialog(project, msg, "QRoute规则检查", Messages.getInformationIcon());
    }

    private void findProjectAllFiles(Project project) {
        String[] allFilenames = FilenameIndex.getAllFilenames(project);
        if (allFilenames == null) {
            Messages.showInputDialog(project, "未找到文件", "QRoute规则检查", Messages.getInformationIcon());
        }

        for (int i = 0; i < allFilenames.length; i++) {
            String filename = allFilenames[i];
            if (filename.contains(".java") || filename.contains(".kt")) {
                System.out.printf("fileName " + i + ": " + filename);
                PsiFile[] filesByName = FilenameIndex.getFilesByName(project, filename, GlobalSearchScope.allScope(project));
            }
        }
    }
}
