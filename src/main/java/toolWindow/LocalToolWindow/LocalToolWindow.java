package toolWindow.LocalToolWindow;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import service.GraphDataMaker;
import util.SQLiteUtils;
import util.VDProperties;

import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LocalToolWindow {
    private JPanel content;
    private JComboBox<String> moduleScopeComboBox;
    private JButton RUNButton;
    private JPanel canvasPanel;
    private Set<PsiMethod> focusedMethods = new HashSet<>();
    public LocalToolWindow(ToolWindow toolWindow, Project project){
        initModuleComboBox(project);
        RUNButton.addActionListener(e->run(project));

    }


    private void initModuleComboBox(Project project){
        moduleScopeComboBox.setRenderer(new PromptComboBoxRenderer("Select Module"));

        this.moduleScopeComboBox.removeAllItems();
        Arrays.stream(ModuleManager.getInstance(project).getModules()).forEach(
                module -> moduleScopeComboBox.addItem(module.getName()));
        moduleScopeComboBox.setSelectedIndex(-1);
        moduleScopeComboBox.setEnabled(true);
    }

    private void run(Project project){
        if(moduleScopeComboBox.getSelectedItem()==null) {
            //todo:提醒用户选择Module

        }else {
            String DBsLocation= project.getBasePath()+
                    File.separator+
                    (String)moduleScopeComboBox.getSelectedItem();

            if(!checkSourceDatabaseExistence(project)){
                //todo:提醒用户检查Module下是否存在生成的TotalData.db

            }else {

                ProgressManager.getInstance().run(new Task.Backgroundable(project, "VDpendency") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        ApplicationManager.getApplication().invokeLater(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        GraphDataMaker gm=new GraphDataMaker();
                                        try {
                                            gm.run(PropertiesComponent.getInstance(project).getValue(VDProperties.SelectedModule.toString())+
                                                            PropertiesComponent.getInstance(project).getValue(VDProperties.SourceDatabaseName.toString()),
                                                    PropertiesComponent.getInstance(project).getValue(VDProperties.SelectedModule.toString())+
                                                            PropertiesComponent.getInstance(project).getValue(VDProperties.ResultDatabaseName.toString()));                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                        );
                    }
                });
            }

        }



    }

    private boolean checkSourceDatabaseExistence(Project project){
        String DBsLocation= project.getBasePath()+
                File.separator+
                (String)moduleScopeComboBox.getSelectedItem();
        Connection conn=null;


        try {
            conn=SQLiteUtils.connectDB(DBsLocation+
                    File.separator+
                    PropertiesComponent.getInstance(project).getValue(VDProperties.SourceDatabaseName.toString()));

            if(null!=conn){
                PropertiesComponent.getInstance(project).setValue(VDProperties.SelectedModule.toString(), DBsLocation+
                        File.separator);
                conn.close();

                System.out
                        .println("Close Connection!");
                return true;
            }

        } catch (Exception e) {
            System.out.println(PropertiesComponent.getInstance(project).getValue(VDProperties.SelectedModule.toString()));
            e.printStackTrace();
        }
        return false;
    }


    public boolean isFocusedMethod(PsiMethod method){
        return this.focusedMethods.contains(method);
    }

    public JComponent getContent(){
        return this.content;
    }
}
