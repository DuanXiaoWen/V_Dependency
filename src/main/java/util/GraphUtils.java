package util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class GraphUtils {


    public static Set<PsiJavaFile> getSourceCodeFiles(Project project, Set<VirtualFile> sourceCodeRoots) {

        Set<PsiJavaFile> virtualFiles=new HashSet<>();

        sourceCodeRoots.forEach(virtualFile -> VfsUtilCore.iterateChildrenRecursively(virtualFile, null, fileOrDir -> {
            if (fileOrDir.isValid() && !fileOrDir.isDirectory() && fileOrDir.getExtension().equals("java")) {

                PsiJavaFile psiJavaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(fileOrDir);
                if (null != psiJavaFile) {
                    virtualFiles.add(psiJavaFile);
                }
            }
            return true;
        }));
        return virtualFiles;
    }



    public static Set<PsiMethod> getMethodsFromFiles(Set<PsiJavaFile> files) {

        return files.stream().flatMap(
                file -> Arrays.stream(file.getClasses())).flatMap(
                        psiClass ->
                                Arrays.stream(psiClass.getMethods())).collect(Collectors.toSet());
        }




}
