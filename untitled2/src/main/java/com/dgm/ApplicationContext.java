package com.dgm;

import com.dgm.db.po.Node;
import com.dgm.ui.LogUtils;
import com.dgm.ui.util.BreakNode;
import com.dgm.ui.TreeView;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor;
import com.intellij.ide.file.BatchFileChangeListener;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileTypes.FileTypeEvent;
import com.intellij.openapi.fileTypes.FileTypeListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;
import com.intellij.openapi.project.impl.ProjectManagerImpl;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.BranchChangeListener;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileManagerListener;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.pom.Navigatable;
import com.intellij.psi.impl.PsiElementBase;
import com.intellij.psi.impl.include.FileIncludeIndex;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.Alarm;
import com.intellij.util.indexing.FileBasedIndexInfrastructureExtension;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/28 0:35
 * @description
 */
public class ApplicationContext implements Disposable{
  private static Logger logger = Logger.getLogger(ApplicationContext.class.getSimpleName());
  private Project project;
  private ToolWindow toolWindow;
  public Alarm rebuildListAlarm;
  public Alarm swingAlarm;

  public ApplicationContext(Project project, ToolWindow toolWindow) {
    this.project = project;
    this.toolWindow = toolWindow;
    rebuildListAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, ()->{});
    swingAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, () -> {});
  }

  public Project getProject() {
    return project;
  }

  public ApplicationContext setProject(Project project) {
    this.project = project;
    return this;
  }

  public ToolWindow getToolWindow() {
    return toolWindow;
  }

  public ApplicationContext setToolWindow(ToolWindow toolWindow) {
    this.toolWindow = toolWindow;
    return this;
  }

  public void addVirtualFileManagerListener(){
    VirtualFileManager.getInstance().addVirtualFileManagerListener(new VirtualFileManagerListener() {

    });

    project.getMessageBus().connect(this).subscribe(BatchFileChangeListener.TOPIC, new BatchFileChangeListener() {
      @Override
      public void batchChangeStarted(Project project, String activityName) {
        System.out.println("activityName = " + activityName);
      }

      @Override
      public void batchChangeCompleted(Project project) {
        System.out.println("activityName = ");
      }
    });
    project.getMessageBus().connect(this).subscribe(VirtualFileManager.VFS_CHANGES,new BulkFileListener(){
      @Override
      public void after(List<? extends VFileEvent> events) {
        //当前监听类修改xml会报错  弃用
        System.out.println("events = " + events);
//        ((VFilePropertyChangeEvent) events.get(0)).getOldPath();
      }
    });


    project.getMessageBus().connect(this).subscribe(VirtualFilePointerListener.TOPIC, new VirtualFilePointerListener() {
      @Override
      public void validityChanged(VirtualFilePointer[] pointers) {
        System.out.println("BookmarkToolWindow.validityChanged:"+pointers.length);
      }
    });

    project.getMessageBus().connect(this).subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, new BranchChangeListener() {
      @Override
      public void branchWillChange(String s) {

      }

      @Override
      public void branchHasChanged(String s) {

      }
    });
  }

  public void addBreakpointListener(){
    project.getMessageBus().connect(this).subscribe(XBreakpointListener.TOPIC, new XBreakpointListener<XBreakpoint<XBreakpointProperties>>() {
      @Override
      public void breakpointAdded(XBreakpoint<XBreakpointProperties> xBreakpoint) {
        if(xBreakpoint instanceof XLineBreakpointImpl){
          VirtualFile file = ((XLineBreakpointImpl) xBreakpoint).getFile();
          String relativePath = null;
          if(DGMConstant.SYSTEM_FILE.equals(file.getFileSystem().getProtocol())) {
            if(file.getPath().startsWith(project.getBasePath())) {//项目下文件
              relativePath = fileRelative(file);
            } else {//项目外的文件
              relativePath = file.getPath();
            }
          } else if (DGMConstant.JAR.equals(file.getFileSystem().getProtocol())|| DGMConstant.JRT.equals(file.getFileSystem().getProtocol())) {
            relativePath = jarRelative(file);
          }
          if(relativePath != null){
            (((XLineBreakpointImpl) xBreakpoint).getProject()).getUserData(BreakNode.KEY).checked(relativePath+":"+((XLineBreakpointImpl<?>) xBreakpoint).getLine(),true);
          }
          if (getToolWindow().getContentManager().getSelectedContent() != null) {
            ((TreeView) getToolWindow().getContentManager().getSelectedContent().getComponent()).refreshData();
          }
        }
      }

      @Override
      public void breakpointRemoved(XBreakpoint breakpoint) {
        if(breakpoint instanceof XLineBreakpointImpl){
          VirtualFile file = ((XLineBreakpointImpl) breakpoint).getFile();
          String relativePath = null;
          if(DGMConstant.SYSTEM_FILE.equals(file.getFileSystem().getProtocol())) {
            if(file.getPath().startsWith(project.getBasePath())) {//项目下文件
              relativePath = fileRelative(file);
            } else {//项目外的文件
              relativePath = file.getPath();
            }
          } else if (DGMConstant.JAR.equals(file.getFileSystem().getProtocol()) || DGMConstant.JRT.equals(file.getFileSystem().getProtocol())) {
            relativePath = jarRelative(file);
          }
          (((XLineBreakpointImpl<?>) breakpoint).getProject()).getUserData(BreakNode.KEY).checked(relativePath + ":"+ ((XLineBreakpointImpl<?>) breakpoint).getLine(),false);
          if (getToolWindow().getContentManager().getSelectedContent() != null) {
            ((TreeView) getToolWindow().getContentManager().getSelectedContent().getComponent()).refreshData();
          }
        }
      }
    });
  }

  public void addActionListener(){
    project.getMessageBus().connect(this).subscribe(AnActionListener.TOPIC, new AnActionListener(){
      @Override
      public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
//        FileEditor data = event.getData(PlatformDataKeys.FILE_EDITOR);
//        if (data == null) {
//          return;
//        }
//        TreeView.nodes.forEach((k, v) -> {
//          if (data.getFile().getPath().endsWith(k.split(":")[0])) {
//            v.stream().forEach(e->e.updateLine());
//          }
//        });
      }
    });
  }

  public void addFileEditorManagerListener(){

    ProjectManagerImpl.getInstance().addProjectManagerListener(project, new ProjectManagerListener() {
      @Override
      public void projectClosingBeforeSave(Project project) {

        ContentManager contentManagerIfCreated = getToolWindow().getContentManagerIfCreated();
        if(contentManagerIfCreated != null) {
          Content[] contents = contentManagerIfCreated.getContents();
          if(contents != null) {

            Arrays.stream(contents).forEach(e->{
              LogUtils.close(project.getName(), ((TreeView) e.getComponent()).getTreeViewName());
              ((TreeView) e.getComponent()).saving();
            });
          }
        }
      }
    });
    project.getMessageBus().connect(this).subscribe(ProjectLifecycleListener.TOPIC, new ProjectLifecycleListener() {
      @Override
      public void projectComponentsInitialized(@NotNull Project project) {

      }
    });

    List<FileBasedIndexInfrastructureExtension> extensionList = FileBasedIndexInfrastructureExtension.EP_NAME.getExtensionList();

    project.getMessageBus().connect(this);
    project.getMessageBus().connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {

      @Override
      public void fileOpenedSync(FileEditorManager source, VirtualFile file, Pair<FileEditor[], FileEditorProvider[]> editors) {
        System.out.println("BookmarkToolWindow.fileOpenedSync:"+file.getPath());
      }
      @Override
      public void selectionChanged(FileEditorManagerEvent event) {
//        TreeView.nodes.forEach((k, v) -> {
//          if (event.getOldFile() != null && event.getOldFile().getPath().endsWith(k.split(":")[0])) {
//            v.stream().forEach(e->e.updateLine());
//          }
//        });
      }
    });
  }

  public void work(Runnable runnable){
    rebuildListAlarm.addRequest(runnable,0);
  }

  @Override
  public void dispose() {
    rebuildListAlarm.cancelAllRequests();
    swingAlarm.cancelAllRequests();
  }

  /**
   * 相对路径
   * @param temp
   * @return 相对路径
   */
  public String fileRelative(VirtualFile temp ){
    return temp.getPath().substring(getProject().getBasePath().length());
  }
  /**
   * 相对路径
   * @param temp
   * @return 相对路径
   */
  public String jarRelative(VirtualFile temp ){
    String substring = temp.getPath().substring(VfsUtilCore.getRootFile(temp).getPath().length());
    return StringUtil.trimLeading(substring, '/');
  }

  public void navigate(VirtualFile virtualFile, int line) {
    swingAlarm.addRequest(() -> {
      XSourcePositionImpl xSourcePosition = XSourcePositionImpl.create(virtualFile, line);
      Navigatable navigatable = xSourcePosition.createNavigatable(getProject());
      navigatable.navigate(navigatable.canNavigate());
    }, 200);
  }

  public void notifyError(String msg) {
    swingAlarm.addRequest(() -> {
      Notification notification = new Notification("group", AllIcons.General.Error, NotificationType.ERROR);
      notification.setContent(msg);
      Notifications.Bus.notify(notification);
    }, 100);
  }

  public void rememberSelect(VirtualFile virtualFile, int line, Consumer<VirtualFile> runnable) {
    swingAlarm.addRequest(() -> {
      Notification notification = new Notification("group", AllIcons.General.Warning, NotificationType.ERROR);
      notification.setContent("文件:" + virtualFile.getPath());
      notification.setImportant(true);
      notification.addAction(new AnAction("跳转") {
        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
          navigate(virtualFile, line);
        }
      });
      notification.addAction(new AnAction("跳转并记住我的选择") {
        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
          navigate(virtualFile, line);
          runnable.accept(virtualFile);
        }
      });
      Notifications.Bus.notify(notification);
      notification.hideBalloon();
    }, 0);
  }

  public void duplication(String path, Map<String, VirtualFile> alreadyChoose, List<FoundItemDescriptor<Object>> list, Node node, Consumer<VirtualFile> consumer) {

    swingAlarm.addRequest(() -> {
      Notification notification = new Notification("com.dgm.group", AllIcons.General.Information, NotificationType.WARNING);
      if (path.endsWith(".class")) {
        notification.setContent("相同文件过多,请到event log中查看\n友情提示:多个相同class文件会导致omitted for duplicate编译错误");
      } else {
        notification.setContent("相同文件过多,请到event log中查看");
      }
      notification.setImportant(true);
      Notifications.Bus.notify(notification);
      if(alreadyChoose.get(path) != null) {
        notification.hideBalloon();
      }

      for (FoundItemDescriptor<Object> item : list) {
        if(item.getItem() instanceof PsiFileImpl) {
          rememberSelect(((PsiFileImpl) item.getItem()).getVirtualFile(), node.getLineNumber(), consumer);
        } else if(item.getItem() instanceof PsiElementBase){
          rememberSelect(((PsiElementBase) item.getItem()).getContainingFile().getVirtualFile(), node.getLineNumber(), consumer);
        }
      }
    }, 0);
  }


  /**
   * ui 线程
   * @param runnable
   */
  public void ui(Runnable runnable){
    swingAlarm.addRequest(runnable, 100);
  }
}
