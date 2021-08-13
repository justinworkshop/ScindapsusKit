package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.Query;
import org.apache.http.util.TextUtils;
import utils.Util;

import java.util.Collection;

public class ReplaceMemberMethod extends AnAction {
    String targetMethod;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // 1.提取用户选择的方法名
        Project project = anActionEvent.getProject();
        // 得到编辑区对象
        Editor editor = anActionEvent.getData(PlatformDataKeys.EDITOR);
        if (null == editor) {
            return;
        }

        // 获取用户选择的字符
        SelectionModel model = editor.getSelectionModel();
        targetMethod = model.getSelectedText();
        if (TextUtils.isEmpty(targetMethod)) {
            targetMethod = Messages.showInputDialog(project, "请输入方法名", "未输入", Messages.getInformationIcon());
            if (TextUtils.isEmpty(targetMethod)) {
                Util.showPopupBalloon(editor, "用户没有输入方法名", 5);
                return;
            }
        }

        // 2.根据方法名找到对应class
        // 根据编辑器找到PsiFill
        PsiFile psiFileInEditor = PsiUtilBase.getPsiFileInEditor(editor, project);
        // 根据PsiFile找到PsiClass
        PsiClass targetClass = Util.getTargetClass(editor, psiFileInEditor);

        String currentFieldName = getCurrentFieldName(editor, targetMethod);
        PsiField fieldByName = targetClass.findFieldByName(currentFieldName, true);
        PsiType type = fieldByName.getType();


        System.out.printf("" + fieldByName);

        replaceMethod(project, "TimeManager", lineContent.trim());
        test(project);
    }

    private String lineContent;

    private String getCurrentFieldName(Editor editor, String targetMethod) {
        Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();
        // 得到一行开始和结束的位置
        int lineNum = document.getLineNumber(offset);
        int lineStartOffset = document.getLineStartOffset(lineNum);
        int lineEndOffset = document.getLineEndOffset(lineNum);
        String methodMatching = "." + targetMethod;
        // 得到一行的所有字符串
        lineContent = document.getText(new TextRange(lineStartOffset, lineEndOffset - 1));

        if (!TextUtils.isEmpty(lineContent) && lineContent.contains(methodMatching)) {
            // 提取method的成员变量
            int endPosition = lineContent.indexOf(methodMatching);
            String fieldName = lineContent.substring(0, endPosition);
            //return "timeManager"
            return fieldName.trim();
        }
        return null;
    }

    private void replaceMethod(Project project, String className, String oldLineContent) {
        // QRoute.api(INearbyCardManagerUtils.class).updateNearbyPeopleCard(app, uin, event, notify);
        StringBuilder stringBuilder = new StringBuilder("QRoute.api(I")
                .append(className)
                .append(".class)");

        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                PsiCallExpression expression = (PsiCallExpression) JavaPsiFacade.getElementFactory(project).createExpressionFromText(stringBuilder.toString(), null);
                expression = (PsiCallExpression) JavaCodeStyleManager.getInstance(project).shortenClassReferences(expression);


                PsiCallExpression oldExpression = (PsiCallExpression) JavaPsiFacade.getElementFactory(project).createExpressionFromText(oldLineContent, null);
                oldExpression = (PsiCallExpression) JavaCodeStyleManager.getInstance(project).shortenClassReferences(oldExpression);

                System.out.println("oldExpression: >>> " + oldExpression.toString());
                System.out.println("expression: >>> " + expression.toString());
                PsiElement replace = oldExpression.replace(expression);
                System.out.println(replace);
            }
        });
    }

    public void test(Project project) {
        PsiClass newClass = JavaPsiFacade.getInstance(project).findClass("com.example.firsttest" + "." + "TimeManager", GlobalSearchScope.allScope(project));
        PsiMethod[] methods = newClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            System.out.println("" + i + ", " + methods[i].toString());
            Query<PsiReference> ref = MethodReferencesSearch.search(methods[i]);
            ref = ref.allowParallelProcessing();
            Collection<PsiReference> allRef = ref.findAll();
            for (PsiReference psiReference : allRef) {
                System.out.println("这里用到了：" + psiReference.getElement().toString());
            }
        }

    }

}