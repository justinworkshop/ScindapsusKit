package view.dialog;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public class ReplaceDialog extends DepsConfigDialog {

    public ReplaceDialog(@Nullable Project project) {
        super(project);
        setTitle("选择不需要替换的范围");
    }
}
