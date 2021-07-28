package view.ui;

import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.ui.MemberSelectionPanel;
import com.intellij.refactoring.ui.MemberSelectionTable;
import com.intellij.refactoring.util.classMembers.MemberInfo;

import java.util.List;

public class StaticMemberSelectionPanel extends MemberSelectionPanel {

    List<PsiMethod> outDeps;

    /**
     * @param title                if title contains 'm' - it would look and feel as mnemonic
     * @param memberInfo
     * @param abstractColumnHeader
     */
    public StaticMemberSelectionPanel(String title, List<MemberInfo> memberInfo, String abstractColumnHeader, List<PsiMethod> outDeps) {
        super(title, memberInfo, abstractColumnHeader);
        this.outDeps = outDeps;
    }

    @Override
    protected MemberSelectionTable createMemberSelectionTable(List<MemberInfo> memberInfo, String abstractColumnHeader) {
        return new StaticMemberSelectionTable(memberInfo, abstractColumnHeader, outDeps);
    }
}
