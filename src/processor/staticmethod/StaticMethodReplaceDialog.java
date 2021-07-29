package processor.staticmethod;

import apiOperator.ApiReplaceOperator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.classMembers.MemberInfoBase;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.ReplaceHelper;
import view.dialog.MyRefactoringDialog;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 选择需要替换的方法，仅替换需要选择的方法，之后弹出一个窗口，用于排除或者选中需要替换的文件
 */
public class StaticMethodReplaceDialog extends MyRefactoringDialog {
    public StaticMethodReplaceDialog(@Nullable Project project, PsiClass clz, List<PsiMethod> hasOutDeps) {
        super(project, clz, collectMembers(clz), hasOutDeps);
    }

    private static List<MemberInfo> collectMembers(PsiClass clz) {
        return MemberInfo.extractClassMembers(clz, new MemberInfoBase.Filter<PsiMember>() {
            @Override
            public boolean includeMember(PsiMember member) {
                if (member instanceof PsiMethod) {
                    if (PsiUtil.isLanguageLevel9OrHigher(member)) {
                        return true;
                    }
                    return member.hasModifierProperty(PsiModifier.PUBLIC)
                            && (PsiUtil.isLanguageLevel8OrHigher(member) || member.hasModifierProperty(PsiModifier.STATIC));
                }
                return false;
            }
        }, true);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected JPanel createDestinationRootPanel() {
        return null;
    }

    @Override
    protected String getTopLabelText() {
        return "the class need to replace";
    }

    @Override
    protected String getTableTitle() {
        return "the method need to replace";
    }

    @Override
    protected void doAction() {
        //获取勾选的方法
        Collection<MemberInfo> extractMember = getSelectedMemberInfos();
        String packName = getTargetPackageName(); // com.example.firsttest
        String newClz = getExtractedSuperName(); // MainUtil

        //需要进行替换的方法列表
        //PsiMethod:getInfo
        //PsiMethod:sendMessage
        List<PsiMethod> methodList = new ArrayList<>();
        for (MemberInfo info : extractMember) {
            if (info.getMember() instanceof PsiMethod) {
                methodList.add((PsiMethod) info.getMember());
            }
        }
        // 找到MainUtil.class
        PsiClass newClass = JavaPsiFacade.getInstance(myProject).findClass(packName + "." + newClz, GlobalSearchScope.allScope(myProject));

        //弹出选择替换区域的弹窗
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ProgressManager.getInstance().run(new Task.Backgroundable(myProject, "查找替换", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        ApplicationManager.getApplication().invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                StaticMethodReplaceDialog.this.closeOKAction();
                            }
                        });
                        try {
                            ApiReplaceOperator.replaceStaticCall(mySourceClass, newClass, methodList);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                });
            }
        };
        ReplaceHelper.runWithReplaceDamianChoose(myProject, runnable);
    }
}
