package processor.methodextract;


import com.intellij.psi.*;
import utils.PsiUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MethodApiGetter {

    public static List<PsiMethod> genClassInterface(PsiClass psiClass) {

        PsiMethod[] methods = psiClass.getMethods();

        //需要抽离的方法以及对应的引用
        List<PsiMethod> methodNeedExtra = new ArrayList<>();

        //寻找需要抽离的方法以及引用
        for (PsiMethod method : methods) {
            PsiModifierList modifierList = method.getModifierList();
            boolean isNotStaticAndPub = !modifierList.hasModifierProperty(PsiModifier.STATIC) &&
                    modifierList.hasModifierProperty(PsiModifier.PUBLIC) && !method.isConstructor();
            if (isNotStaticAndPub) {
                //查找有没有外部引用
                Collection<PsiReference> refs = PsiUtils.findUsage(method, false);
                if (refs.size() > 0) {
                    //需要抽离的方法
                    methodNeedExtra.add(method);
                }
            }
        }

        if (methodNeedExtra.size() <= 0) {
            return null;
        }

        return methodNeedExtra;

    }

}
