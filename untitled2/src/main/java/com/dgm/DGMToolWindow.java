package com.dgm;

import com.dgm.db.TabMapper;
import com.dgm.ui.LogUtils;
import com.dgm.ui.TreeView;
import com.dgm.ui.util.BreakNode;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.ui.AnimatedIcon;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import static com.intellij.icons.AllIcons.Process.Big.Step_1;
import static com.intellij.icons.AllIcons.Process.Big.Step_2;
import static com.intellij.icons.AllIcons.Process.Big.Step_3;
import static com.intellij.icons.AllIcons.Process.Big.Step_4;
import static com.intellij.icons.AllIcons.Process.Big.Step_5;
import static com.intellij.icons.AllIcons.Process.Big.Step_6;
import static com.intellij.icons.AllIcons.Process.Big.Step_7;
import static com.intellij.icons.AllIcons.Process.Big.Step_8;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/18 0:34
 * @description
 */
public class DGMToolWindow implements ToolWindowFactory, DumbAware, ProjectManagerListener, Runnable {
  private Logger logger = Logger.getLogger(DGMToolWindow.class.getSimpleName());
  public static Key<JComponent> windowComponent = new Key(DGMToolWindow.class.getName());
  public static Key<ApplicationContext> key = new Key(DGMToolWindow.class.getName());
  public static Key<ContentManagerListener> keyLis = new Key(ContentManagerListener.class.getName());
  /**
   * app 对象每词
   */
  private AtomicReference<ApplicationContext> reference = new AtomicReference<>();
  private Set<ApplicationContext> apps = new HashSet<>();
  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    ApplicationContext app = new ApplicationContext(project, toolWindow);
    LogUtils.app(app);
    Disposer.register(toolWindow.getDisposable(), app);

    loading(app);
    ApplicationManager.getApplication().runWriteAction(()->{
      app.work(()->{
        try {
          while (DumbServiceImpl.getInstance(project).isDumb() && FileBasedIndexImpl.getInstance().getCurrentDumbModeAccessType() == null) {
            Thread.sleep(100);
          }
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
      DumbServiceImpl.getInstance(project).smartInvokeLater(launch(app));
    });
  }

  private void loading(ApplicationContext app) {
    Icon passive = AllIcons.Process.Big.Step_passive;
    Icon[] icons = {Step_1,Step_2,Step_3,Step_4,Step_5,Step_6,Step_7,Step_8};
    JPanel panel = new JPanel(new BorderLayout());
    AnimatedIcon animatedIcon = new AnimatedIcon("Casual", icons, passive, 600);
    animatedIcon.resume();
    panel.add(animatedIcon, BorderLayout.CENTER);

    ContentFactory instance = ContentFactory.SERVICE.getInstance();
    Content content = instance.createContent(panel, "loading", true);
    app.getToolWindow().getContentManager().addContent(content);
  }


  private void addAction(ApplicationContext app) {
    ArrayList<AnAction> objects = new ArrayList<>();
    objects.add(ActionManager.getInstance().getAction("new_tab"));
    objects.add(ActionManager.getInstance().getAction("tab_left"));
    objects.add(ActionManager.getInstance().getAction("tab_right"));
    objects.add(ActionManager.getInstance().getAction("tab_delete"));
    app.getToolWindow().setTitleActions(objects);
  }


  @Override
  public boolean shouldBeAvailable(Project project) {
    ContentFactory instance = ContentFactory.SERVICE.getInstance();
//    Content[] selectedContents = toolWindow.getContentManager().getSelectedContents();
//    for (int i = 0; i < selectedContents.length; i++) {
//      ((TreeView)(selectedContents[i])).newFolder.getTemplatePresentation().setEnabled(true);
//    }
    return true;
  }

  @Override
  public void run() {
  }

  public Runnable launch(ApplicationContext app){
    return ()->{

      app.getProject().putUserData(windowComponent, app.getToolWindow().getComponent());

      app.getProject().putUserData(keyLis, new ContentManagerListener() {
        @Override
        public void selectionChanged(ContentManagerEvent event) {
          logger.info(""+event.getIndex());
          app.getProject().getUserData(TabMapper.key).active(event.getIndex());
          ContentManager contentManager = app.getToolWindow().getContentManager();
          if(contentManager.getSelectedContent() != null){
            JComponent component = contentManager.getSelectedContent().getComponent();
            if (component != null) {
              app.getProject().putUserData(TreeView.key, (TreeView) component);
            }
          }
        }
      });
      app.getToolWindow().getContentManager().addContentManagerListener(app.getProject().getUserData(keyLis));

//      PersistingUtil persistingUtil = new PersistingUtil(app);
//      Disposer.register(app, persistingUtil);
//      persistingUtil.createRootBookmark();
//      app.getProject().putUserData(PersistingUtil.KEY, persistingUtil);

      app.getProject().putUserData(DGMToolWindow.key, app);
      app.getProject().putUserData(BreakNode.KEY, new BreakNode());

      app.addVirtualFileManagerListener();
      app.addBreakpointListener();
      app.addActionListener();
      app.addFileEditorManagerListener();
      addAction(app);

      TabMapper tabMapper = new TabMapper(app);
      Disposer.register(app, tabMapper);
      app.getProject().putUserData(TabMapper.key, tabMapper);
      tabMapper.refresh();
    };
  }
}
