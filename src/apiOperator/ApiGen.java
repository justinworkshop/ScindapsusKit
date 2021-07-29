package apiOperator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.ClassUtil;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import org.jetbrains.annotations.NotNull;
import utils.ClassNameUtils;
import utils.PsiUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ApiGen {


    public static PsiClass genInterface(Project project, List<PsiMethod> methodList, PsiDirectory dir,
                                        String packageName, String oldClzName, PsiClassType superClz, boolean isStatic) {
        String interfaceName = ClassNameUtils.staticMethodInterfaceName(oldClzName);
        if (!isStatic) {
            interfaceName = ClassNameUtils.getInterfaceName(oldClzName);
        }
        PsiClass interfaceClz = JavaPsiFacade.getElementFactory(project).createInterface(interfaceName);
        for (PsiMethod method : methodList) {
            PsiMethod newMethod = getPsiMethod(project, packageName, oldClzName, method);
            interfaceClz.add(newMethod);
        }
        if (superClz != null) {
            interfaceClz.getExtendsList().add(JavaPsiFacade.getElementFactory(project).createReferenceElementByType(superClz));
        }

        //先查找一下，看看类在不在，在的话只是添加方法
        PsiClass file = JavaPsiFacade.getInstance(project).findClass(packageName + ".api." + interfaceName, GlobalSearchScope.allScope(project));
        if (file == null) {
            file = JavaDirectoryService.getInstance().createInterface(ClassNameUtils.createDirectory(dir, "api"), interfaceName);
        }
        file.replace(interfaceClz);
        return file;
    }

    // 创建newClz
    public static PsiClass getInterfaceWithMemberInfo(Project project, PsiClass curClz, Collection<MemberInfo> memberInfos,
                                                      PsiDirectory dir, String packageName, String clzName, PsiClassType superClz, boolean isStatic) {
        String fqName = packageName + "." + clzName;
        PsiClass newClz = JavaPsiFacade.getInstance(project).findClass(fqName, GlobalSearchScope.allScope(project));
        if (newClz == null) {
            //不存在之前的类，需要创建
            newClz = JavaDirectoryService.getInstance().createInterface(ClassNameUtils.createDirectory(dir, "api"), clzName);
            if (superClz != null) {
                newClz.getExtendsList().add(JavaPsiFacade.getElementFactory(project).createReferenceElementByType(superClz));
            }
            // 如何让clz直接继承QRouteApi ??
//            newClz.getExtendsList().add(JavaPsiFacade.getElementFactory(project).createReferenceElementByType())
        }
        HashSet<String> curMethodSet = new HashSet<>();
        for (PsiMethod method : newClz.getMethods()) {
            curMethodSet.add(ClassUtil.getAsmMethodSignature(method));
        }

        String oldClzName = curClz.getName();
        for (MemberInfo info : memberInfos) {
            if (!(info.getMember() instanceof PsiMethod)) {
                continue;
            }
            PsiMethod method = (PsiMethod) info.getMember();
            if (curMethodSet.contains(ClassUtil.getAsmMethodSignature(method))) {
                continue;
            }
            PsiMethod newMethod = getPsiMethod(project, packageName, oldClzName, method);
            newClz.add(newMethod);
        }
        return newClz;
    }

    @NotNull
    private static PsiMethod getPsiMethod(Project project, String packageName, String oldClzName, PsiMethod method) {
        String methodName = method.getName();
        PsiType returnType = method.getReturnType();
        if (method.isConstructor()) {
            methodName = ClassNameUtils.NEW_INSTANCE_NAME;
            returnType = PsiType.getTypeByName(packageName + "." + oldClzName, project, GlobalSearchScope.allScope(project));
        }
        PsiMethod newMethod = JavaPsiFacade.getElementFactory(project).createMethod(methodName, returnType, method.getContext());
        newMethod.getModifierList().replace(PsiUtils.getModifierForInterfaceMethod(newMethod, true));
        newMethod.getParameterList().replace(method.getParameterList());

        newMethod = (PsiMethod) JavaCodeStyleManager.getInstance(project).shortenClassReferences(newMethod);
        Objects.requireNonNull(newMethod.getBody()).delete();
        return newMethod;
    }


}
