package deps;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;

import java.util.Arrays;
import java.util.HashSet;

public class PackageDepsAnalysis implements DepsAnalysis{


    HashSet<String> packagesSet = new HashSet<>();
    private Project project;

    public PackageDepsAnalysis(Project project) {
        this.project = project;
    }

    public void addToPackage(String[] items) {
        if (items == null) {
            return;
        }
        packagesSet.addAll(Arrays.asList(items));
    }

    @Override
    public boolean curClzInDepsSet(PsiClass psiClass) {
        String packName = ((PsiJavaFile)psiClass.getContainingFile()).getPackageName();
        return packagesSet.contains(packName);
    }
}
