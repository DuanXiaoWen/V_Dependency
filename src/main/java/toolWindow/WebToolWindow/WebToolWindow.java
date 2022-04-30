package toolWindow.WebToolWindow;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;
import org.cef.CefApp;

import javax.swing.*;

import java.awt.*;

public class WebToolWindow {

    private JButton hideToolWindowButton;
    private JPanel myToolWindowContent;
    private JPanel myBrowserPanel;

    public WebToolWindow(ToolWindow toolWindow) {
        hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));

        if (!JBCefApp.isSupported()) {
            myToolWindowContent.add(new JLabel("当前环境不支持JCEF", SwingConstants.CENTER));
        }else {
            JBCefBrowser browser = new JBCefBrowser();
            myBrowserPanel.add(browser.getComponent(), BorderLayout.CENTER);
//browser.loadURL("file:///home/kyriepotreler/Projects/FrontendLearn/html_css_material-master/第三天/22-综合案例-新闻页面.html");
            browser.loadURL("https://en.wikipedia.org/wiki/Main_Page");
            browser.getComponent();
//            JBCefClient jbCefClient=browser.getJBCefClient();

        }
    }

    public JComponent getContent() {
        return myToolWindowContent;
    }
}