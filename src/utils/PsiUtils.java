package utils;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Query;
import deps.DepsAnalysisHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PsiUtils {

    private static PsiModifierList list = null;
    private static PsiModifierList implList = null;

    public static PsiModifierList getModifierForInterfaceMethod(PsiMethod method, boolean isInterface) {
        if (isInterface && list != null) {
            return list;
        }
        if (!isInterface && implList != null) {
            return implList;
        }
        PsiModifierList modifierList = (PsiModifierList) method.getModifierList().copy();
        for (String tag : PsiModifier.MODIFIERS) {
            modifierList.setModifierProperty(tag, false);
        }
        modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
        if (isInterface) {
            modifierList.setModifierProperty(PsiModifier.ABSTRACT, true);
        }
        if (isInterface) {
            list = modifierList;
            return list;
        } else {
            implList = modifierList;
            return implList;
        }
    }

    /**
     * 查找到所有非本源文件所引用的地方
     * @param method 需要查找的方法
     * @param forReplace
     * @return 返回符合条件的应用
     */
    public static Collection<PsiReference> findUsage(PsiMethod method, boolean forReplace) {
        Query<PsiReference> ref = MethodReferencesSearch.search(method);
        ref = ref.allowParallelProcessing();
        Collection<PsiReference> referenceCollection = ref.findAll();
        List<PsiReference> references = new ArrayList<>();
        for (PsiReference reference : referenceCollection) {
            PsiElement element = reference.getElement();
            if (DepsAnalysisHolder.getInstance().isDeps(PsiUtil.getTopLevelClass(method), PsiUtil.getTopLevelClass(element))) {
                references.add(reference);
            }
        }
        //看看是不是在自己类之外的，如果
        return references;
    }

    public static Collection<PsiReference> findClassUsage(PsiClass clz) {
        Query<PsiReference> ref = ReferencesSearch.search(clz);
        ref = ref.allowParallelProcessing();
        Collection<PsiReference> referenceCollection = ref.findAll();
        List<PsiReference> references = new ArrayList<>();
        for (PsiReference reference : referenceCollection) {
            PsiElement element = reference.getElement();
            if (!DepsAnalysisHolder.getInstance().isDeps(clz, PsiUtil.getTopLevelClass(element))) {
                continue;
            }
            references.add(reference);
        }
        return references;
    }


    public static boolean isGetManagerExp(PsiElement element) {
        PsiElement parents = element.getParent();
        if (parents != null) {
            parents = parents.getParent();
        } else {
            return false;
        }
        if (parents == null) {
            return false;
        }
        if (parents instanceof PsiMethodCallExpression) {
            return RuntimeServiceReplace.GET_MANAGER_METHOD.equals(((PsiMethodCallExpression) parents).getMethodExpression().getReferenceName());
        }
        return false;
    }

    public static Collection<PsiReference> findManagerIdUsage(PsiField field) {
        Query<PsiReference> ref = ReferencesSearch.search(field);
        ref = ref.allowParallelProcessing();
        Collection<PsiReference> referenceCollection = ref.findAll();

        List<PsiReference> references = new ArrayList<>();
        for (PsiReference reference : referenceCollection) {
            PsiElement element = reference.getElement();
            if (!isGetManagerExp(element)) {
                continue;
            }
            references.add(reference);
        }
        return references;
    }
}
