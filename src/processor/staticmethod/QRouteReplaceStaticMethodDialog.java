package processor.staticmethod;

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
import utils.ReplaceStaticMethodUtil;
import view.dialog.CustomRefactorDialog;
import view.dialog.ReplaceDialog;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * QRoute规则替换，用于将Util类的静态方法替换为QRoute的引用方式
 * 原始：TimeUtil.getCurrentTime();
 * 替换：QRoute.api(ITimeUtil.class).getCurrentTime();
 */
public class QRouteReplaceStaticMethodDialog extends CustomRefactorDialog {
    public QRouteReplaceStaticMethodDialog(@Nullable Project project, PsiClass clz) {
        super(project, clz, collectMembers(clz), null);
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
    protected void doAction() {
        // 获取目标包名 com.example.firsttest
        String packageName = getTargetPackageName();
        // 获取class文件 TimeUtil
        String className = getExtractedSuperName();
        // 获取选中方法
        Collection<MemberInfo> selectedMemberInfos = getSelectedMemberInfos();

        // 根据selectedMemberInfos获得List<PsiMethod>
        List<PsiMethod> methodList = getSelectedPsiMethods(selectedMemberInfos);
        // 找到TimeUtil.class
        String qualifiedClassName = packageName + "." + className;
        PsiClass newClass = JavaPsiFacade.getInstance(myProject).findClass(qualifiedClassName, GlobalSearchScope.allScope(myProject));

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ProgressManager.getInstance().run(new Task.Backgroundable(myProject, "查找替换", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        // 插件需要这样切到事件分发线程
                        ApplicationManager.getApplication().invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                QRouteReplaceStaticMethodDialog.this.closeOKAction();
                            }
                        });
                        try {
                            ReplaceStaticMethodUtil.replaceStaticMethod(newClass, methodList);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                });
            }
        };

        // 弹出选择区域的对话框
        ReplaceDialog dialog = new ReplaceDialog(myProject);
        dialog.setAfterOkAction(runnable);
        dialog.show();
    }

    @NotNull
    private List<PsiMethod> getSelectedPsiMethods(Collection<MemberInfo> extractMember) {
        List<PsiMethod> methodList = new ArrayList<>();
        for (MemberInfo memberInfo : extractMember) {
            if (memberInfo.getMember() instanceof PsiMethod) {
                methodList.add((PsiMethod) memberInfo.getMember());
            }
        }
        return methodList;
    }
}
