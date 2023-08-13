package com.dgm;

import com.dgm.db.TabMapper;
import com.dgm.ui.LogUtils;
import com.dgm.ui.TreeView;
import com.dgm.ui.util.BreakNode;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.impl.EditorComposite;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorWithProviderComposite;
import com.intellij.openapi.fileEditor.impl.PsiAwareFileEditorManagerImpl;
import com.intellij.openapi.fileTypes.FileTypeEvent;
import com.intellij.openapi.fileTypes.FileTypeListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.impl.ProjectExImpl;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowEP;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.impl.ToolWindowManagerImpl;
import com.intellij.ui.components.labels.BoldLabel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.indexing.diagnostic.ProjectIndexingHistory;
import com.intellij.util.indexing.diagnostic.ProjectIndexingHistoryListener;
import com.intellij.util.keyFMap.KeyFMap;
import com.intellij.util.ui.AnimatedIcon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
public class DGMToolWindow implements ToolWindowFactory, DumbAware, ProjectManagerListener, Runnable, ProjectIndexingHistoryListener {
  private Logger logger = Logger.getLogger(DGMToolWindow.class.getSimpleName());
  public static Key<JComponent> windowComponent = new Key(DGMToolWindow.class.getName());
  public static Key<ApplicationContext> key = new Key(DGMToolWindow.class.getName());
  public static Key<ContentManagerListener> keyLis = new Key(ContentManagerListener.class.getName());

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    loading(toolWindow);
  }

  private void loading(ToolWindow app) {
    Icon passive = AllIcons.Process.Big.Step_passive;
    Icon[] icons = {Step_1,Step_2,Step_3,Step_4,Step_5,Step_6,Step_7,Step_8};
    JPanel panel = new JPanel(new BorderLayout());
    AnimatedIcon animatedIcon = new AnimatedIcon("Casual", icons, passive, 600);
    animatedIcon.resume();
    panel.add(animatedIcon, BorderLayout.CENTER);

    ContentFactory instance = ContentFactory.SERVICE.getInstance();
    Content content = instance.createContent(panel, "loading", true);
    app.getContentManager().addContent(content);
  }


  private void addAction(Project app) {
    ArrayList<AnAction> objects = new ArrayList<>();
    objects.add(ActionManager.getInstance().getAction("new_tab"));
    objects.add(ActionManager.getInstance().getAction("tab_left"));
    objects.add(ActionManager.getInstance().getAction("tab_right"));
    objects.add(ActionManager.getInstance().getAction("tab_delete"));
    ToolWindowManagerImpl.getInstance(app).getToolWindow("DGM").setTitleActions(objects);
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


  @Override
  public void onFinishedIndexing(@NotNull ProjectIndexingHistory projectIndexingHistory) {
    if (projectIndexingHistory.getTimes().getIndexingReason().equals("On project open")) {
      ApplicationContext.ui(()-> openWindow(projectIndexingHistory.getProject()));
    }
  }


  private void openWindow(Project project) {
    project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
      @Override
      public void selectionChanged(@NotNull FileEditorManagerEvent event) {

      }
    });
    project.putUserData(windowComponent, Utils.getWindow(project).getComponent());

    project.putUserData(keyLis, new ContentManagerListener() {
      @Override
      public void selectionChanged(ContentManagerEvent event) {
        logger.info("" + event.getIndex());
        project.getUserData(TabMapper.key).active(event.getIndex());
        ContentManager contentManager = Utils.getWindow(project).getContentManager();
        if (contentManager.getSelectedContent() != null) {
          JComponent component = contentManager.getSelectedContent().getComponent();
          if (component != null) {
            project.putUserData(TreeView.key, (TreeView) component);
          }
        }
      }
    });
    Utils.getWindow(project).getContentManager().addContentManagerListener(project.getUserData(keyLis));


    project.putUserData(BreakNode.KEY, new BreakNode());

    ApplicationContext.addVirtualFileManagerListener(project);
    ApplicationContext.addBreakpointListener(project);
    ApplicationContext.addActionListener(project);
    ApplicationContext.addFileEditorManagerListener(project);
    addAction(project);

    TabMapper tabMapper = new TabMapper(project);
    project.putUserData(TabMapper.key, tabMapper);
    tabMapper.refresh();
  }
  @Override
  public void onStartedIndexing(@NotNull ProjectIndexingHistory projectIndexingHistory) {

  }
}
