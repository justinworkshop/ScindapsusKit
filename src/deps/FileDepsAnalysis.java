package deps;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.HashSet;

public class FileDepsAnalysis implements DepsAnalysis{

    private HashSet<PsiFile> fileSet = new HashSet<>();
    private Project project;

    public FileDepsAnalysis(Project project) {
        this.project = project;
    }


    public void addToFileSet(String[] items) {
        if (items == null) {
            return;
        }
        for (String clzName : items) {
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(clzName, GlobalSearchScope.allScope(project));
            if (psiClass != null) {
                fileSet.add(psiClass.getContainingFile());
            }
        }
    }


    @Override
    public boolean curClzInDepsSet(PsiClass psiClass) {
        PsiFile curFile = psiClass.getContainingFile();
        return fileSet.contains(curFile);
    }


}
