package toolWindow.LocalToolWindow;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import service.GraphDataMaker;
import toolWindow.LocalToolWindow.entity.Edge;
import toolWindow.LocalToolWindow.entity.Node;
import util.GraphUtils;
import util.SQLiteUtils;
import util.VDProperties;

import java.util.List;

import javax.swing.*;
import java.awt.*;
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
        String selectedMoudleName=(String)moduleScopeComboBox.getSelectedItem();
        if(null==selectedMoudleName) {
            //todo:提醒用户选择Module

        }else {
            String DBsLocation= project.getBasePath()+
                    File.separator+
                    selectedMoudleName;

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
                                        GraphDataMaker graphDataMaker=new GraphDataMaker();


                                        try {
                                            String sourceDB=PropertiesComponent.getInstance(project).getValue(VDProperties.SelectedModule.toString())+
                                                    PropertiesComponent.getInstance(project).getValue(VDProperties.SourceDatabaseName.toString());
                                            String resultDB=PropertiesComponent.getInstance(project).getValue(VDProperties.SelectedModule.toString())+
                                                    PropertiesComponent.getInstance(project).getValue(VDProperties.ResultDatabaseName.toString());


                                            graphDataMaker.run(sourceDB,resultDB);
                                            List<Node> nodeList=graphDataMaker.queryNodes(resultDB);
                                            List<Edge> edgeList=graphDataMaker.queryEdges(resultDB);
                                            System.out.println(nodeList.size());
                                            System.out.println(edgeList.size());


                                            Set<VirtualFile> sourceRoots=new HashSet<>(List.of(ModuleRootManager.getInstance(ModuleManager.getInstance(project).findModuleByName(selectedMoudleName)).getSourceRoots()));

                                            Set<PsiJavaFile> files= GraphUtils.getSourceCodeFiles(project,sourceRoots);
                                            Set<PsiMethod> methods=GraphUtils.getMethodsFromFiles(files);








                                        } catch (SQLException e) {
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
//
//    private Graph buildGraph(List<Node> nodeList,List<Edge> edgeList,Set<PsiMethod> psiMethods) {
//
//        val graph = Graph()
//        methods.forEach { graph.addNode(it) }
//        dependencyView.forEach {
//            graph.addNode(it.caller)
//            graph.addNode(it.callee)
//            graph.addEdge(it.caller, it.callee)
//        }
//        Utils.layout(graph)
//        return graph
//    }

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
    public Dimension getCanvasSize(){
        return this.canvasPanel.getSize();
    }

    public JComponent getContent(){
        return this.content;
    }
}
