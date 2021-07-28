package utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;

public class ClassNameUtils {

    public static String QROUTE_FQN = "com.tencent.mobileqq.qroute.QRouteApi";

    public static String NEW_INSTANCE_NAME = "newInstance";

    public static String staticMethodInterfaceName(String simpleName) {
        return "I" + simpleName + "StaticMethodHelper";
    }

    public static String staticMethodImplName(String simpleName) {
        return simpleName + "StaticMethodHelperImpl";
    }


    public static PsiDirectory createDirectory(PsiDirectory parent, String... names)
            throws IncorrectOperationException {
        PsiDirectory result = null;

        for (String name : names) {
            result = null;
            for (PsiDirectory dir : parent.getSubdirectories()) {
                if (dir.getName().equalsIgnoreCase(name)) {
                    result = dir;
                    break;
                }
            }

            if (null == result) {
                result = parent.createSubdirectory(name);
            }
            parent = result;
        }


        return result;
    }

    private static PsiClassType qRouteClz = null;

    public static PsiClassType getQRouteApi(Project p) {
        if (qRouteClz == null) {
            qRouteClz = PsiType.getTypeByName(QROUTE_FQN, p, GlobalSearchScope.allScope(p));
        }
        return qRouteClz;
    }

    public static String getInterfaceName(String simpleName) {
        return "I" + simpleName;
    }

    public static String getImplName(String simpleName) {
        return simpleName + "Impl";
    }

    public static PsiClassType getTypeFromClass(Project p, PsiClass psiClass) {
        return null;
    }


}
