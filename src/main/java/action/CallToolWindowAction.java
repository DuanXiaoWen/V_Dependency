// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package action;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.pom.Navigatable;
import icons.V_DependencyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toolWindow.MyToolWindowFactory;
import util.VDProperties;

import javax.swing.*;
import java.util.function.Supplier;


public class CallToolWindowAction extends AnAction {


  private MyToolWindowFactory myToolWindowFactory;


  public CallToolWindowAction() {
    super();
  }

  //dynamically add menu action
  public CallToolWindowAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
    super(text, description, icon);
  }



  private void registerProperties(Project project){
    PropertiesComponent.getInstance(project).setValue(VDProperties.SourceDatabaseName.toString(),"TotalData.db");
    PropertiesComponent.getInstance(project).setValue(VDProperties.ResultDatabaseName.toString(), "callGraphResult.db");
    PropertiesComponent.getInstance(project).setValue(VDProperties.NodeDatabaseName.toString(), "node.db");
    PropertiesComponent.getInstance(project).setValue(VDProperties.EdgeDatabaseName.toString(), "edge.db");



  }
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    final Project project = CommonDataKeys.PROJECT.getData(event.getDataContext());
    if(project==null||ToolWindowManager.getInstance(project).getToolWindow("V-Dependency")!=null) return;
    registerProperties(project);
    myToolWindowFactory=new MyToolWindowFactory();
    ToolWindowManager.getInstance(project).registerToolWindow(
            new RegisterToolWindowTask(
                    "V-Dependency",
                    ToolWindowAnchor.RIGHT,
                    null,
                    false,
                    false,
                    false,
                    true,
                    myToolWindowFactory,
                    V_DependencyIcons.App_icon_16,
                    null ));
  }

  /**
   * Determines whether this menu item is available for the current context.
   * Requires a project to be open.
   * @param e Event received when the associated group-id menu is chosen.
   */
  @Override
  public void update(AnActionEvent e) {
    // Set the availability based on whether a project is open
    Project project = e.getProject();
    e.getPresentation().setEnabledAndVisible(project != null);
  }

}
