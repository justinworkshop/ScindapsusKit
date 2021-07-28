package deps;

import com.intellij.openapi.project.Project;
import swing.inputset.InputFileSet;

public class DepsAnalysisFactory {


    public static DepsAnalysis createDepsAnalysis(String opt, Project project, String[] item) {
        switch (opt) {
            case InputFileSet
                    .ACTION_CHANGE_TO_FILE:
                FileDepsAnalysis fileDepsAnalysis = new FileDepsAnalysis(project);
                fileDepsAnalysis.addToFileSet(item);
                return fileDepsAnalysis;
            case InputFileSet.ACTION_CHANGE_TO_MODULE:
                ModuleDepsAnalysis moduleDepsAnalysis = new ModuleDepsAnalysis(project);
                moduleDepsAnalysis.addItemToList(item);
                return moduleDepsAnalysis;
            case InputFileSet.ACTION_CHANGE_TO_PACKAGE:
                PackageDepsAnalysis packageDepsAnalysis = new PackageDepsAnalysis(project);
                packageDepsAnalysis.addToPackage(item);
                return packageDepsAnalysis;
            default:
                return null;
        }
    }

}
