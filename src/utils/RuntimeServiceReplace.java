package utils;

import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.util.Collection;

public class RuntimeServiceReplace {
    public static final String ANNOTATION_NAME = "com.tencent.mobileqq.qroute.annotation.Service";
    public static final String PROCESS_CONSTANT = "mqq.app.api.ProcessConstant";
    public static final String PROCESS_NAME = "process";
    public static final String GET_MANAGER_METHOD = "getManager";

    public static String getAnnotationFromClass(PsiClass psiClass) {
        PsiAnnotation[] annotations = psiClass.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if (ANNOTATION_NAME.equals(annotation.getQualifiedName())) {
                for (JvmAnnotationAttribute annotationAttribute : annotation.getAttributes()) {
                    if (PROCESS_NAME.equals(annotationAttribute.getAttributeName())) {
                        return ((PsiReferenceExpression)((PsiNameValuePair) annotationAttribute).getValue()).getReferenceName();
                    }
                }
            }
        }
        return null;
    }


    public static String getProcessNameArgs(PsiClass psiClass) {
        String process = getAnnotationFromClass(psiClass);
        if (process == null) {
            return null;
        } else {
            return PROCESS_CONSTANT + "." + process;
        }
    }


    public static PsiMethodCallExpression getGetRuntimeServiceCall(PsiClass newInterfaceName) {
        return WriteCommandAction.runWriteCommandAction(newInterfaceName.getProject(), new Computable<PsiMethodCallExpression>() {
            @Override
            public PsiMethodCallExpression compute() {
                String firstArgs = newInterfaceName.getQualifiedName() + ".class";
                String processArgs = getProcessNameArgs(newInterfaceName);
                StringBuilder stringBuilder = new StringBuilder()
                        .append("a.getRuntimeService(")
                        .append(firstArgs)
                        .append(", ")
                        .append(processArgs)
                        .append(")");
                PsiMethodCallExpression expression = (PsiMethodCallExpression) JavaPsiFacade.getElementFactory(newInterfaceName.getProject())
                        .createExpressionFromText(stringBuilder.toString(), null);
                return (PsiMethodCallExpression) JavaCodeStyleManager.getInstance(newInterfaceName.getProject()).shortenClassReferences(expression);
            }
        });
    }


    public static void replaceMethod(Collection<PsiReference> referenceCollection, PsiClass newInterfaces) {

        PsiMethodCallExpression methodCallExpression = getGetRuntimeServiceCall(newInterfaces);

        WriteCommandAction.runWriteCommandAction(newInterfaces.getProject(), new Runnable() {
            @Override
            public void run() {

                for (PsiReference reference : referenceCollection) {
                    PsiElement element = reference.getElement();
                    //直接获取到调用的表达式，替换掉方法调用部分
                    PsiMethodCallExpression callExpression = (PsiMethodCallExpression) element.getParent().getParent();
                    PsiMethodCallExpression cur = (PsiMethodCallExpression) methodCallExpression.copy();
                    PsiElement callQualifier = callExpression.getMethodExpression().getQualifierExpression();
                    if (callQualifier == null) {
                        cur.getMethodExpression().getQualifierExpression().delete();
                    } else {
                        cur.getMethodExpression().getQualifierExpression().replace(callExpression.getMethodExpression().getQualifierExpression());
                    }
                    callExpression.replace(cur);
                }
            }
        });

    }
}
