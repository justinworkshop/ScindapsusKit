package deps;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import java.util.HashSet;

public class ModuleDepsAnalysis implements DepsAnalysis{

    private HashSet<Module> depsSetModule = new HashSet<>();

    private Project project;

    public ModuleDepsAnalysis(Project project) {
        this.project = project;
    }

    public void addItemToList(String[] items) {
        if (items == null) {
            return;
        }
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (String moduleName : items) {
            for (Module module : modules) {
                if (module.getName().contains(moduleName)) {
                    depsSetModule.add(module);
                    break;
                }
            }
        }
    }

    @Override
    public boolean curClzInDepsSet(PsiClass psiClass) {
        Module module = ModuleUtil.findModuleForPsiElement(psiClass);
        return depsSetModule.contains(module);
    }


}
