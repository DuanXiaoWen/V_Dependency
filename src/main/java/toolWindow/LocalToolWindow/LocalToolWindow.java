package toolWindow.LocalToolWindow;

import com.intellij.ide.util.EditorHelper;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import service.GraphDataMaker;
import toolWindow.LocalToolWindow.entity.Edge;
import toolWindow.LocalToolWindow.entity.Node;
import util.PsiUtils;
import util.SQLiteUtils;
import util.VDProperties;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class LocalToolWindow {
    private JPanel content;
    private JComboBox<String> moduleScopeComboBox;
    private JButton RUNButton;
    private JPanel canvasPanel;
    private PsiElement focusedElement;

    private JButton viewSourceCodeButton;
    private JTextField searchTextField;
    private  Canvas canvas= new Canvas(this);
    public LocalToolWindow(ToolWindow toolWindow, Project project){
        initModuleComboBox(project);
        viewSourceCodeButton.addActionListener(e-> viewSourceCodeHandler());
        searchTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {
                canvas.repaint();
            }
        });
        MouseEventHandler mouseEventHandler = new MouseEventHandler(canvas);

        canvas.addMouseListener(mouseEventHandler);
        canvas.addMouseMotionListener(mouseEventHandler);
        canvas.addMouseWheelListener(mouseEventHandler);

        this.canvas.setVisible(false);
        this.canvasPanel.add(this.canvas);


        RUNButton.addActionListener(e->{
            focusedElement=null;
            run(project);
        });

    }

    private void setupUiBeforeRun() {

        Arrays.asList(
                viewSourceCodeButton,
                searchTextField
        ).forEach(e->
                ((JComponent)e).setEnabled(false)
        );
        canvas.setVisible(false);
    }
    private void setupUiAfterRun() {

        canvas.setVisible(true);
        this.canvasPanel.updateUI();


        enableFocusedMethodButtons();
        Arrays.asList(
                searchTextField
        ).forEach(e->
                ((JComponent)e).setEnabled(true)
        );

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
                setupUiBeforeRun();
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "VDpendency") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        ApplicationManager.getApplication().runReadAction(
                                () -> {
                                    GraphDataMaker graphDataMaker=new GraphDataMaker();
                                    try {
                                        String sourceDB=PropertiesComponent.getInstance(project).getValue(VDProperties.SelectedModule.toString())+
                                                PropertiesComponent.getInstance(project).getValue(VDProperties.SourceDatabaseName.toString());
                                        String resultDB=PropertiesComponent.getInstance(project).getValue(VDProperties.SelectedModule.toString())+
                                                PropertiesComponent.getInstance(project).getValue(VDProperties.ResultDatabaseName.toString());

                                        graphDataMaker.run(sourceDB,resultDB);

                                        List<Edge> edgeList=graphDataMaker.queryEdges(resultDB);
                                        System.out.println(edgeList.size());
                                        Graph graph=PsiUtils.buildGraph(project,edgeList);
                                        canvas.reset(graph);
                                        setupUiAfterRun();

                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                }
                        );
                    }
                });
            }

        }



    }

    public LocalToolWindow toggleFocusedMethod(PsiElement psiElement){
        if (Objects.equals(psiElement,focusedElement)) {
            // clicked on a selected node
            this.focusedElement=null;
        } else {
            // clicked on an un-selected node
            this.focusedElement=psiElement;
        }
        return this;
    }

    private void viewSourceCodeHandler() {

        EditorHelper.openInEditor(this.focusedElement);
    }
    private void enableFocusedMethodButtons() {
        this.viewSourceCodeButton.setEnabled(Objects.equals(focusedElement,null));

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

                System.out.println("Close Connection!");
                return true;
            }

        } catch (Exception e) {
            System.out.println(PropertiesComponent.getInstance(project).getValue(VDProperties.SelectedModule.toString()));
            e.printStackTrace();
        }
        return false;
    }

    public LocalToolWindow clearFocusedMethods(){
        focusedElement=null;
        enableFocusedMethodButtons();
        return this;
    }
    public boolean isQueried(String text){
        String searchQuery = this.searchTextField.getText().toLowerCase();
        return !searchQuery.isEmpty() && text.toLowerCase().contains(searchQuery);
    }
    public boolean isFocusedElement(PsiElement psiElement){
        return Objects.equals(psiElement, focusedElement);
    }
    public Dimension getCanvasSize(){
        return this.canvasPanel.getSize();
    }

    public JComponent getContent(){
        return this.content;
    }
}
