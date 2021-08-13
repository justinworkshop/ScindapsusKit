package utils;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.ClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ReplaceStaticMethodUtil {

    private static final String API_CLASS = "com.tencent.mobileqq.qroute.QRoute";

    //生成一个QRoute.api(args);
    private static PsiCallExpression apiGetExpress(Project project, PsiClass interfaceClz) {
        StringBuilder stringBuilder = new StringBuilder(API_CLASS)
                .append(".api(")
                .append(interfaceClz.getQualifiedName())
                .append(".class)");
        PsiCallExpression expression = (PsiCallExpression) JavaPsiFacade.getElementFactory(project).createExpressionFromText(stringBuilder.toString(), null);
        expression = (PsiCallExpression) JavaCodeStyleManager.getInstance(project).shortenClassReferences(expression);
        return expression;
    }

    /**
     * 替换静态方法
     *
     * @param originPsiClass    : TimeUtil.class
     * @param needReplaceMethod : TimeUtil中要被替换的方法定义
     * @throws Throwable
     */
    public static void replaceStaticMethod(PsiClass originPsiClass, List<PsiMethod> needReplaceMethod) throws Throwable {
        if (needReplaceMethod == null) {
            return;
        }
        System.out.println("替换静态方法的运行线程: " + Thread.currentThread().getName());

        // 1.生成QRoute替换语句,PsiMethodCallExpression:com.tencent.mobileqq.qroute.QRoute.api(MainUtil.class)
        PsiCallExpression expression = apiGetExpress(originPsiClass.getProject(), originPsiClass);

        // 生成新接口中方法索引
        // getInfo()V
        // sendMessage(Ljava/lang/String;)Z
        // 2.生成所有要替换方法的签名，作为对比使用（有重载方法的存在，所以这里比对方法签名）
        HashSet<String> methodsSet = getNeedReplaceMethodSigSe(needReplaceMethod);

        //逐一方法替换 oldClass: PsiClass:MainUtil
        //PsiMethod:getInfo
        //PsiMethod:sendMessage
        PsiMethod[] methodNeedToReplace = originPsiClass.getMethods();

        for (PsiMethod method : methodNeedToReplace) {
            // 找到使用该方法的集合
            Collection<PsiReference> collection = ReadAction.compute(new ThrowableComputable<Collection<PsiReference>, Throwable>() {
                @Override
                public Collection<PsiReference> compute() throws Throwable {
                    String sig = method.getName() + ClassUtil.getAsmMethodSignature(method);
                    if (methodsSet.contains(sig)) {
                        return PsiUtils.findUsage(method, true);
                    } else {
                        return new ArrayList<>();
                    }
                }
            });

            // 这里是判断这个方法 有几处要替换
            innerReplace(originPsiClass.getProject(), collection, expression);
        }
    }

    private static HashSet<String> getNeedReplaceMethodSigSe(List<PsiMethod> needReplaceMethod) throws Throwable {
        HashSet<String> methodsSet = new HashSet<>();
        for (PsiMethod method : needReplaceMethod) {
            String compute = ReadAction.compute(new ThrowableComputable<String, Throwable>() {
                @Override
                public String compute() throws Throwable {
                    String methodSig = method.getName() + ClassUtil.getAsmMethodSignature(method);
                    System.out.println("待替换方法签名 methodSig: " + methodSig);
                    return methodSig;
                }
            });
            methodsSet.add(compute);
        }
        return methodsSet;
    }

    private static void innerReplace(Project project, Collection<PsiReference> collection, PsiCallExpression expression) {
        if (collection == null || collection.size() < 1) {
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, new Computable<Object>() {
            @Override
            public Object compute() {
                for (PsiReference reference : collection) {
                    PsiElement element = reference.getElement();
                    System.out.println("遍历有几处 replace ? >>> " + element.toString());

                    if (element instanceof PsiReferenceExpression) {
                        PsiExpression qualifierExpression = ((PsiReferenceExpression) element).getQualifierExpression();
                        System.out.println("qualifierExpression ? >>> " + qualifierExpression.toString());
                        System.out.println("expression ? >>> " + expression.toString());
                        qualifierExpression.replace(expression);
                    }
                }
                return null;
            }
        });
    }
}
