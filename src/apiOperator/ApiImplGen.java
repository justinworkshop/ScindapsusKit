package apiOperator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import utils.ClassNameUtils;
import utils.PsiUtils;

import java.util.List;
import java.util.StringJoiner;

public class ApiImplGen {

    public static void genStaticInterfaceImpl(Project project, List<PsiMethod> methodList, PsiDirectory dir,
                                    String packageName, String oldClzName, PsiClassType superClz) {
        String implName = ClassNameUtils.staticMethodImplName(oldClzName);
        PsiClass implClz = JavaPsiFacade.getElementFactory(project).createClass(implName);
        for (PsiMethod method : methodList) {
            String methodName = method.getName();
            PsiType returnType = method.getReturnType();
            if (method.isConstructor()) {
                methodName = ClassNameUtils.NEW_INSTANCE_NAME;
                returnType = PsiType.getTypeByName(method.getContainingClass().getQualifiedName(), project, GlobalSearchScope.allScope(project));
            }
            PsiMethod newMethod = JavaPsiFacade.getElementFactory(project).createMethod(methodName, returnType, method.getContext());
            newMethod.getModifierList().replace(PsiUtils.getModifierForInterfaceMethod(newMethod, false));
            newMethod.getParameterList().replace(method.getParameterList());

            newMethod = (PsiMethod) JavaCodeStyleManager.getInstance(project).shortenClassReferences(newMethod);
            PsiParameterList list = method.getParameterList();
            StringJoiner argJoiner = new StringJoiner(", ");
            for (PsiParameter parameter : list.getParameters()) {
                argJoiner.add(parameter.getName());
            }
            StringBuilder stringBuilder = new StringBuilder();
            if (!method.isConstructor()) {
                stringBuilder.append(oldClzName)
                        .append(".")
                        .append(method.getName())
                        .append("(")
                        .append(argJoiner.toString())
                        .append(")");
            } else {
                stringBuilder.append("new ")
                        .append(method.getContainingClass().getQualifiedName())
                        .append("(")
                        .append(argJoiner.toString())
                        .append(")");
            }
            PsiElement body = null;
            if (method.isConstructor() || !method.getReturnType().equalsToText("void")) {
                PsiExpression expression = JavaPsiFacade.getElementFactory(project).createExpressionFromText(stringBuilder.toString(), null);
                PsiReturnStatement psiStatement = (PsiReturnStatement) JavaPsiFacade.getElementFactory(project).createStatementFromText("return a;", null);
                psiStatement.getReturnValue().replace(expression);
                body = psiStatement;
            } else {
                stringBuilder.append(";");
                body = JavaPsiFacade.getElementFactory(project).createStatementFromText(stringBuilder.toString(), null);
            }
            body = JavaCodeStyleManager.getInstance(project).shortenClassReferences(body);
            newMethod.getBody().add(body);
            implClz.add(newMethod);
        }

        implClz.getImplementsList().add(JavaPsiFacade.getElementFactory(project).createReferenceElementByType(superClz));

        //先查找一下，看看类在不在，在的话只是添加方法
        PsiClass file = JavaPsiFacade.getInstance(project).findClass(packageName + ".impl." + implName, GlobalSearchScope.allScope(project));
        if (file == null) {
            file = JavaDirectoryService.getInstance().createInterface(ClassNameUtils.createDirectory(dir, "api", "impl"), implName);
        }
        file.replace(implClz);
    }
}
