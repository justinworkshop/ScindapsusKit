package processor.staticmethod;

import apiOperator.ApiGen;
import apiOperator.ApiImplGen;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import utils.ClassNameUtils;
import utils.PsiUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StaticMethodApiGen {


    public static List<PsiMethod> getExtractMethodList(PsiClass psiClass) {
        PsiMethod[] constructors = psiClass.getConstructors();
        PsiMethod[] methods = psiClass.getMethods();

        //需要抽离的方法以及对应的引用
        List<PsiMethod> methodNeedExtra = new ArrayList<>();

        //寻找需要抽离的方法以及引用
        for (PsiMethod method : methods) {
            PsiModifierList modifierList = method.getModifierList();
            boolean isStaticAndPub = modifierList.hasModifierProperty(PsiModifier.STATIC) &&
                    modifierList.hasModifierProperty(PsiModifier.PUBLIC);
            if (isStaticAndPub) {
                //查找有没有外部引用
                Collection<PsiReference> refs = PsiUtils.findUsage(method, false);
                if (refs.size() > 0) {
                    //需要抽离的方法
                    methodNeedExtra.add(method);
                }
            }
        }

        for (PsiMethod constructor : constructors) {
            PsiModifierList modifierList = constructor.getModifierList();
            if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
                Collection<PsiReference> refs = PsiUtils.findUsage(constructor, false);
                if (refs.size() > 0) {
                    methodNeedExtra.add(constructor);
                }
            }
        }

        return methodNeedExtra;
    }

    public static void beginExtracted(PsiClass psiClass, List<PsiMethod> methodNeedExtra) {
        //开始抽离出一个接口类
        WriteCommandAction.runWriteCommandAction(psiClass.getProject(), new Runnable() {
            @Override
            public void run() {
                PsiClass interfaceClass = ApiGen.genInterface(psiClass.getProject(), methodNeedExtra, psiClass.getContainingFile().getParent(),
                        ((PsiJavaFile) psiClass.getContainingFile()).getPackageName(), psiClass.getName(), ClassNameUtils.getQRouteApi(psiClass.getProject()), true);

                PsiClassType type = PsiType.getTypeByName(((PsiJavaFile) psiClass.getContainingFile()).getPackageName() + ".api." + interfaceClass.getName(),
                        psiClass.getProject(), GlobalSearchScope.allScope(psiClass.getProject()));

                ApiImplGen.genStaticInterfaceImpl(psiClass.getProject(), methodNeedExtra, psiClass.getContainingFile().getParent(),
                        ((PsiJavaFile) psiClass.getContainingFile()).getPackageName() + ".api", psiClass.getName(), type);
            }
        });
    }


}
