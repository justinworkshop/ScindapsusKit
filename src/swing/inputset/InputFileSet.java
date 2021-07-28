package swing.inputset;

import com.intellij.ide.util.ClassFilter;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.ui.PackageNameReferenceEditorCombo;
import com.intellij.ui.ReferenceEditorComboWithBrowseButton;
import deps.DepsAnalysis;
import deps.DepsAnalysisFactory;
import deps.DepsAnalysisHolder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InputFileSet implements ActionListener{

    public static final String ACTION_CHANGE_TO_MODULE = "action_module";
    public static final String ACTION_CHANGE_TO_FILE = "action_file";
    public static final String ACTION_CHANGE_TO_PACKAGE = "action_package";

    private String curMode;


    private Project project;
    private static final String RECENTS_KEY = "MoveClassesOrPackagesDialog.RECENTS_KEY";

    private JPanel root;
    private JRadioButton moduleRadioButton;
    private JRadioButton packageRadioButton;
    private JRadioButton fileRadioButton;
    private ButtonGroup buttonGroup;

    private ReferenceEditorComboWithBrowseButton currentInputBox;
    private JList<String> depsSet;
    private final DefaultListModel<String> dlm;


    private ReferenceEditorComboWithBrowseButton packageChooser;
    private ReferenceEditorComboWithBrowseButton clzChooser;
    private ReferenceEditorComboWithBrowseButton moduleChooser;


    public InputFileSet(Project project) {
        this.project = project;
        initListener();
        dlm = new DefaultListModel<String>();
    }

    public void initListener() {
        moduleRadioButton.setActionCommand(ACTION_CHANGE_TO_MODULE);
        packageRadioButton.setActionCommand(ACTION_CHANGE_TO_PACKAGE);
        fileRadioButton.setActionCommand(ACTION_CHANGE_TO_FILE);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(packageRadioButton);
        buttonGroup.add(moduleRadioButton);
        buttonGroup.add(fileRadioButton);

        packageRadioButton.addActionListener(this);
        moduleRadioButton.addActionListener(this);
        fileRadioButton.addActionListener(this);

        moduleRadioButton.setSelected(true);


        moduleChooser.setVisible(true);
        clzChooser.setVisible(false);
        packageChooser.setVisible(false);
        currentInputBox = moduleChooser;
        curMode = ACTION_CHANGE_TO_MODULE;

        depsSet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = depsSet.locationToIndex(e.getPoint());
                    dlm.remove(index);
                    depsSet.setModel(dlm);
                }
            }
        });
    }

    public JPanel getRoot() {
        root.setSize(1000, 1000);
        return root;
    }

    private void createUIComponents() {
        packageChooser = createPackageChooser();
        clzChooser = createClassChooser();
        moduleChooser = createModuleChooser();
    }

    private ReferenceEditorComboWithBrowseButton createPackageChooser() {
        return new PackageNameReferenceEditorCombo("", project, RECENTS_KEY, RefactoringBundle.message("choose.destination.package"));
    }

    private ReferenceEditorComboWithBrowseButton createClassChooser() {
        return new ReferenceEditorComboWithBrowseButton(new ChooseClassAction(), "", project, true, JavaCodeFragment.VisibilityChecker.PROJECT_SCOPE_VISIBLE, RECENTS_KEY);
    }

    private ReferenceEditorComboWithBrowseButton createModuleChooser() {
        return new ReferenceEditorComboWithBrowseButton(null, "", project, false, RefactoringBundle.message("choose.destination.package"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        currentInputBox.setText("");
        dlm.clear();
        switch (e.getActionCommand()) {
            case ACTION_CHANGE_TO_FILE:
                clzChooser.setVisible(true);
                moduleChooser.setVisible(false);
                packageChooser.setVisible(false);
                currentInputBox = clzChooser;
                curMode = ACTION_CHANGE_TO_FILE;
                break;
            case ACTION_CHANGE_TO_PACKAGE:
                packageChooser.setVisible(true);
                clzChooser.setVisible(false);
                moduleChooser.setVisible(false);
                currentInputBox = packageChooser;
                curMode = ACTION_CHANGE_TO_PACKAGE;
                break;
            case ACTION_CHANGE_TO_MODULE:
                moduleChooser.setVisible(true);
                clzChooser.setVisible(false);
                packageChooser.setVisible(false);
                currentInputBox = moduleChooser;
                curMode = ACTION_CHANGE_TO_MODULE;
                break;
        }
    }


    private class ChooseClassAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project).createWithInnerClassesScopeChooser(
                    RefactoringBundle.message("choose.destination.class"), GlobalSearchScope.projectScope(project), new ClassFilter() {
                        @Override
                        public boolean isAccepted(PsiClass aClass) {
                            return aClass.getParent() instanceof PsiFile || aClass.hasModifierProperty(PsiModifier.STATIC);
                        }
                    }, null);
            final String targetClassName = getTargetClassName();
            if (targetClassName != null) {
                final PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(targetClassName, GlobalSearchScope.allScope(project));
                if (aClass != null) {
                    chooser.selectDirectory(aClass.getContainingFile().getContainingDirectory());
                }
            }

            chooser.showDialog();
            PsiClass aClass = chooser.getSelected();
            if (aClass != null) {
                clzChooser.setText(aClass.getQualifiedName());
            }
        }
    }

    public String getTargetClassName() {
        return clzChooser.getText();
    }

    /**
     * 上层的ok Event 传入UI，UI根据情况看看是否处理，处理了事件返回true，反之返回false
     * @return 是否已经处理了事件
     */
    public boolean onOkEvent() {
        String content = currentInputBox.getText();
        if (StringUtil.isEmpty(content)) {
            DepsAnalysisHolder.getInstance().setDepsSet(getDepsAnalysis());
            return false;
        } else {
            dlm.addElement(content);
            depsSet.setModel(dlm);
            currentInputBox.setText("");
            return true;
        }
    }


    public DepsAnalysis getDepsAnalysis() {
        String[] items = new String[dlm.getSize()];
        dlm.copyInto(items);
        return DepsAnalysisFactory.createDepsAnalysis(curMode, project, items);
    }
}
