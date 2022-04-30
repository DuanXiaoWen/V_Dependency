package toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;
import toolWindow.LocalToolWindow.LocalToolWindow;
import toolWindow.WebToolWindow.WebToolWindow;

import java.lang.reflect.Method;
import java.util.Objects;


public class MyToolWindowFactory implements ToolWindowFactory {




    // evaluated only once when open a new project , decide to show or hide  a tool window
    @Override
    public boolean isApplicable(@NotNull Project project) {
        return ToolWindowFactory.super.isApplicable(project);
    }

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        ToolWindowFactory.super.init(toolWindow);
//        System.out.println("init!");

    }

    private boolean isSupportedJCEF() {
        try {
            Method method = ReflectionUtil.getDeclaredMethod(Class.forName("com.intellij.ui.jcef.JBCefApp"),
                    "isSupported");
            return Objects.nonNull(method) && (boolean) method.invoke(null);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
//        WebToolWindow myToolWindow=new WebToolWindow(toolWindow);
        LocalToolWindow myToolWindow=new LocalToolWindow(toolWindow,project);
        ContentFactory contentFactory=ContentFactory.SERVICE.getInstance();
        Content content= contentFactory.createContent(myToolWindow.getContent(),"",false);
        toolWindow.getContentManager().addContent(content);
//        System.out.println("createContent!");
    }


    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return ToolWindowFactory.super.shouldBeAvailable(project);
    }


}
