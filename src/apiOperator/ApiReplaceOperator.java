package apiOperator;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.ClassUtil;
import utils.ClassNameUtils;
import utils.PsiUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ApiReplaceOperator {

    public static final String API_CLASS = "com.tencent.mobileqq.qroute.QRoute";

    public static PsiCallExpression apiGetExpress(Project project, PsiClass interfaceClz) {
        //生成一个QRoute.api(args);
        StringBuilder stringBuilder = new StringBuilder(API_CLASS)
                .append(".api(")
                .append(interfaceClz.getQualifiedName())
                .append(".class)");
        PsiCallExpression expression = (PsiCallExpression) JavaPsiFacade.getElementFactory(project).createExpressionFromText(stringBuilder.toString(), null);
        expression = (PsiCallExpression) JavaCodeStyleManager.getInstance(project).shortenClassReferences(expression);
        return expression;
    }

    // 点击确定指定的方法： 勾选的方法

    /**
     * @param oldClass
     * @param newInterface
     * @param needReplaceMethod
     * @throws Throwable
     * 1.生成待替换的表达式
     * 2.
     */
    public static void replaceStaticMethod(PsiClass oldClass, PsiClass newInterface, List<PsiMethod> needReplaceMethod) throws Throwable {
        if (needReplaceMethod == null) {
            return;
        }
        System.out.println("整个大方法的线程 replaceStaticCall: " + Thread.currentThread().getName());

        //生成QRoute语句作为target PsiMethodCallExpression:com.tencent.mobileqq.qroute.QRoute.api(MainUtil.class)
        PsiCallExpression expression = ReadAction.compute(new ThrowableComputable<PsiCallExpression, Throwable>() {
            @Override
            public PsiCallExpression compute() throws Throwable {
                PsiCallExpression psiCallExpression = apiGetExpress(oldClass.getProject(), newInterface);
                System.out.println("生成target替换语句 Compute inThread: " + Thread.currentThread().getName() + ", psiCallExpression: " + psiCallExpression);
                return psiCallExpression;
            }
        });

        // 生成新接口中方法索引
        // getInfo()V
        // sendMessage(Ljava/lang/String;)Z
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

        //逐一方法替换 oldClass: PsiClass:MainUtil
        //PsiMethod:getInfo
        //PsiMethod:sendMessage
        PsiMethod[] methodNeedToReplace = oldClass.getMethods();
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
            if (collection.size() > 0) {
                WriteCommandAction.runWriteCommandAction(oldClass.getProject(), new Computable<Object>() {
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
    }


    public static void replaceNewCall(PsiClass oldClass, PsiClass newInterface) {
        //生成QRoute语句作为target
        PsiCallExpression expression = apiGetExpress(oldClass.getProject(), newInterface);
        PsiMethodCallExpression call = (PsiMethodCallExpression) JavaPsiFacade.getElementFactory(oldClass.getProject()).createExpressionFromText("a.newInstance(b)", null);
        call.getMethodExpression().getQualifierExpression().replace(expression);
        //生成新接口中方法索引
        HashSet<String> methodsSet = new HashSet<>();
        PsiMethod[] methods = newInterface.getMethods();
        for (PsiMethod method : methods) {
            if (ClassNameUtils.NEW_INSTANCE_NAME.equals(method.getName())) {
                String asmSig = ClassUtil.getAsmMethodSignature(method);
                methodsSet.add(getArgSig(asmSig));
            }
        }

        PsiMethod[] methodNeedToReplace = oldClass.getMethods();
        for (PsiMethod method : methodNeedToReplace) {
            String sig = getArgSig(ClassUtil.getAsmMethodSignature(method));
            //如果需要替换该方法
            if (methodsSet.contains(sig)) {
                Collection<PsiReference> allRef = PsiUtils.findUsage(method, true);

                WriteCommandAction.runWriteCommandAction(oldClass.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        for (PsiReference reference : allRef) {
                            PsiNewExpression exp = ((PsiNewExpression)reference.getElement().getParent());
                            PsiMethodCallExpression cur = (PsiMethodCallExpression) call.copy();
                            cur.getArgumentList().replace(exp.getArgumentList());
                            exp.replace(cur);
                        }
                    }
                });
            }
        }

    }


    /**
     * 获取ASM签名中参数部分
     * @return 返回参数部分
     */
    private static String getArgSig(String asmSig) {
        int index = asmSig.indexOf(")");
        return asmSig.substring(0, index);
    }

}
