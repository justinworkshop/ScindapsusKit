package deps;

import com.intellij.psi.PsiClass;

public interface DepsAnalysis {

    boolean curClzInDepsSet(PsiClass psiClass);

}
