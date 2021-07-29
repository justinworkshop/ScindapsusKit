package view;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import entity.Element;
import utils.Util;

import java.util.List;

public class ViewFieldMethodCreator extends WriteCommandAction.Simple {

    private FindViewByIdDialog mDialog;
    private Editor mEditor;
    private PsiFile mFile;
    private Project mProject;
    private PsiClass mClass;
    private List<Element> mElements;
    private PsiElementFactory mFactory;

    public ViewFieldMethodCreator(FindViewByIdDialog dialog, Editor editor, PsiFile psiFile, PsiClass psiClass, String command, List<Element> elements, String selectedText) {
        super(psiClass.getProject(), command);
        mDialog = dialog;
        mEditor = editor;
        mFile = psiFile;
        mProject = psiClass.getProject();
        mClass = psiClass;
        mElements = elements;
        // 获取Factory
        mFactory = JavaPsiFacade.getElementFactory(mProject);
    }

    /**
     * 单独用一个线程来生成代码
     *
     * @throws Throwable
     */
    @Override
    protected void run() throws Throwable {
        //生成属性
        generateFields(mClass, mFactory, mElements);
        //生成方法
        generateOnClickMethod(mElements);
        //重写XXXActivity.java文件
        //1.找到对应的项目
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        //优化文件
        styleManager.optimizeImports(mFile);
        styleManager.shortenClassReferences(mClass);
        //2.执行写入
        new ReformatCodeProcessor(mProject, mClass.getContainingFile(), null
                , false).runWithoutProgress();

        Util.showPopupBalloon(mEditor, "生成成功", 5);
    }

    /**
     * 创建字段的注入代码
     */
    private void generateFields(PsiClass psiClass, PsiElementFactory factory, List<Element> elementList) {
//        @BindView(R.id.tvText)
//        public TextView mTvText;
        for (Element element : elementList) {
            StringBuilder text = new StringBuilder();
            text.append("@BindView(" + element.getFullID() + ")\n");
            text.append("public ");
            text.append(element.getName() + " ");
            text.append(element.getFieldName() + ";");
            if (element.isCreateFiled()) {
                psiClass.add(factory.createFieldFromText(text.toString(), mClass));
            }
        }
    }


    /**
     * 创建监听事件方法
     */
    private void generateOnClickMethod(List<Element> elementList) {
        for (Element element : elementList) {
            if (element.isCreateClickMethod()) {
                //生成onClick()   btnClick()
                String methodName = getClickMethodName(element) + "Click";
                PsiMethod[] onClickMethod = mClass.findMethodsByName(methodName, true);
                boolean clickMethodExist = onClickMethod.length > 0;
                // 不存在相同的方法才去创建
                if (!clickMethodExist) {
                    createClickMethod(methodName, element);
                }
            }
        }
    }

    /**
     * 获取点击方法的名称   tv_text   tvText
     */
    public String getClickMethodName(Element element) {
        String[] names = element.getId().split("_");
        // aaBbCc
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            if (i == 0) {
                sb.append(names[i]);
            } else {
                sb.append(Util.firstToUpperCase(names[i]));
            }
        }
        return sb.toString();
    }


    private void createClickMethod(String methodName, Element element) {
//        @OnClick(R.id.tvText)
//        private void tvTextClick(TextView tvText) {
//        }
        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append("@OnClick(" + element.getFullID() + ")\n");
        methodBuilder.append("public void " + methodName + "(" + element.getName() + " "
                + getClickMethodName(element) + "){");
        methodBuilder.append("\n}");
        //创建onclick方法
        mClass.add(mFactory.createMethodFromText(methodBuilder.toString(), mClass));
    }
}
