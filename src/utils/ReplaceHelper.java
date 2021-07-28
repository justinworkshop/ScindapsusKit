package utils;

import com.intellij.openapi.project.Project;
import view.dialog.ReplaceDialog;

/**
 * 替换方法的统一使用入口
 */
public class ReplaceHelper {


    public static void runWithReplaceDamianChoose(Project project, Runnable replaceRunnable) {
        //弹出选择区域的对话框
        ReplaceDialog dialog = new ReplaceDialog(project);
        dialog.setAfterOkAction(replaceRunnable);
        dialog.show();
    }

}
