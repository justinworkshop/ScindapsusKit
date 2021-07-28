package deps;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiClass;
import utils.ClassNameUtils;

/**
 * 统一持有一个依赖分析，需不需要抽离，或者是不是依赖方法通过这个类进行判断获取
 */
public class DepsAnalysisHolder {

    public DepsAnalysis analysis;

    private DepsAnalysisHolder() {

    }

    private static class DepsAnalysisHolderInstanceHolder {
        private static DepsAnalysisHolder holder = new DepsAnalysisHolder();
    }

    public static DepsAnalysisHolder getInstance() {
        return DepsAnalysisHolderInstanceHolder.holder;
    }


    public void setDepsSet(DepsAnalysis depsSet) {
        analysis = depsSet;
    }

    public DepsAnalysis getDepsSet() {
        return analysis;
    }

    public boolean isDeps(PsiClass sourceClz, PsiClass targetClz) {
        //判断是否出现类为空
        if (sourceClz == null || targetClz == null) {
            return sourceClz != targetClz;
        }
        //判断是否是接口的实现类
        String interfaceName = sourceClz.getName();
        String oldName = targetClz.getName();
        if (interfaceName.equals(ClassNameUtils.staticMethodImplName(oldName))) {
            return false;
        }


        if (analysis != null) {
            return !analysis.curClzInDepsSet(targetClz);
        }

        Module module = ModuleUtil.findModuleForPsiElement(sourceClz);
        Module oldModule = ModuleUtil.findModuleForPsiElement(targetClz);

        if (module == null || oldModule == null) {
            return false;
        }
        return !module.equals(oldModule);
    }

}
