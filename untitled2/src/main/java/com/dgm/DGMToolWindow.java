package com.dgm;

import com.dgm.db.TabMapper;
import com.dgm.ui.TreeView;
import com.dgm.ui.breakpoint.MyEditorMouseListener;
import com.dgm.ui.util.BreakNode;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.impl.ToolWindowManagerImpl;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.intellij.util.ui.AnimatedIcon;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointManagerImpl;

import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
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
  public static Key<Boolean> branch = new Key("branch");

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    project.putUserData(DGMToolWindow.branch, true);
    loading(toolWindow);
    DumbServiceImpl.getInstance(project).smartInvokeLater(()-> openWindow(project), ModalityState.stateForComponent(toolWindow.getComponent()));
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

  private void openWindow(Project project) {
    XBreakpointManagerImpl breakpointManager = ((XBreakpointManagerImpl) XDebuggerManager.getInstance(project).getBreakpointManager());
    XBreakpointBase<?,?,?>[] breakpoints = breakpointManager.getAllBreakpoints();
    for (final XBreakpointBase<?, ?, ?> breakpoint : breakpoints) {
      ApplicationManager.getApplication().runWriteAction(() -> breakpointManager.removeBreakpoint(breakpoint));
    }

    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {

      @Override
      public void before(@NotNull List<? extends VFileEvent> events) {
        events.stream()
                .filter(e->e instanceof VFileDeleteEvent)
                .forEach(e->{
                  if (project.getUserData(BreakNode.KEY) != null) {
                    project.getUserData(BreakNode.KEY).deleteFile(((VFileDeleteEvent) e).getFile());
                  }
                });
      }
    });
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
//    project.putUserData(DGMToolWindow.branch, false);
    EditorEventMulticaster editorEventMulticaster = EditorFactory.getInstance().getEventMulticaster();
    editorEventMulticaster.addEditorMouseListener(new MyEditorMouseListener(project), project);
  }

}
