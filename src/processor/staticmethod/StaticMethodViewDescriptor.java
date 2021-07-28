package processor.staticmethod;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.usageView.UsageViewDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StaticMethodViewDescriptor implements UsageViewDescriptor {

    private PsiMethod curMethod;

    public StaticMethodViewDescriptor(final PsiMethod method) {
        curMethod = method;
    }

    @Override
    public PsiElement [] getElements() {
        return new PsiElement[0];
    }

    @Override
    public String getProcessedElementsHeader() {
        return null;
    }

    @Override
    public @NotNull String getCodeReferencesText(int usagesCount, int filesCount) {
        return null;
    }

    @Override
    public @Nullable String getCommentReferencesText(int i, int i1) {
        return null;
    }
}
