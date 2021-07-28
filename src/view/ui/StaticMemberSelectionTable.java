package view.ui;

import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.ClassUtil;
import com.intellij.refactoring.classMembers.MemberInfoModel;
import com.intellij.refactoring.ui.MemberSelectionTable;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import org.jetbrains.annotations.Nullable;
import view.dialog.MyRefactoringDialog;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class StaticMemberSelectionTable extends MemberSelectionTable {

    HashSet<String> sigSet = new HashSet<>();
    AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    public StaticMemberSelectionTable(List<MemberInfo> memberInfos, String abstractColumnHeader, List<PsiMethod> outDeps) {
        super(memberInfos, abstractColumnHeader);
    }

    public StaticMemberSelectionTable(List<MemberInfo> memberInfos, MemberInfoModel<PsiMember, MemberInfo> memberInfoModel, String abstractColumnHeader, List<PsiMethod> outDeps) {
        super(memberInfos, memberInfoModel, abstractColumnHeader);
    }

    @Override
    protected @Nullable Object getAbstractColumnValue(MemberInfo memberInfo) {
        if (!atomicBoolean.getAndSet(true)) {
            initSet();
        }
        if (!(memberInfo.getMember() instanceof PsiMethod)) {
            return null;
        }
        if (MyRefactoringDialog.outDeps == null) {
            return null;
        }
        PsiMethod method = (PsiMethod) memberInfo.getMember();
        return sigSet.contains(method.getName() + ClassUtil.getAsmMethodSignature(method));
    }

    private void initSet() {
        List<PsiMethod> outDeps = MyRefactoringDialog.outDeps;
        if (outDeps != null) {
            for (PsiMethod method : outDeps) {
                sigSet.add(method.getName() + ClassUtil.getAsmMethodSignature(method));
            }
        }
    }


}
