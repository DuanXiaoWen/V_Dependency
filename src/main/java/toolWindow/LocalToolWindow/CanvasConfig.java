package toolWindow.LocalToolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;

import java.util.Set;

public class CanvasConfig {

    private Project project;
    private Canvas canvas;
    private String selectedModuleName;
    private  Set<PsiMethod> focusedMethods;
    private LocalToolWindow localToolWindow;
}
