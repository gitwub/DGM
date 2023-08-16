package com.dgm;

import com.dgm.db.TabMapper;
import com.dgm.db.po.Node;
import com.dgm.ui.LogUtils;
import com.dgm.ui.util.BreakNode;
import com.dgm.ui.TreeView;
import com.intellij.AppTopics;
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
import com.intellij.openapi.diff.impl.util.GutterActionRenderer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.FileEditorProvider;
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
import com.intellij.pom.Navigatable;
import com.intellij.psi.impl.PsiElementBase;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.Alarm;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.indexing.FileBasedIndexInfrastructureExtension;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.impl.XDebuggerManagerImpl;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointManagerImpl;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil;
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl;
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointManager;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
  public static Alarm rebuildListAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, ()->{});
  public static Alarm swingAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, () -> {});

  private static void removeAll(Project project){
    ((XDebuggerManagerImpl) XDebuggerManager.getInstance(project)).getState().getBreakpointManagerState().getBreakpoints().clear();
    XBreakpointUtil.breakpointTypes().remove(e->true);

    XBreakpointManagerImpl breakpointManager = (XBreakpointManagerImpl) XDebuggerManager.getInstance(project).getBreakpointManager();
    XLineBreakpointManager lineBreakpointManager = breakpointManager.getLineBreakpointManager();
    Field[] declaredFields = XDebuggerManager.getInstance(project).getBreakpointManager().getClass().getDeclaredFields();
    Field myAllBreakpoints = Arrays.stream(declaredFields).filter(e -> e.getName().equals("myAllBreakpoints")).findAny().get();
    try {
      myAllBreakpoints.setAccessible(true);
      LinkedHashSet linkedHashSet = (LinkedHashSet) myAllBreakpoints.get(breakpointManager);
      linkedHashSet.clear();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    Field[] declaredFields1 = lineBreakpointManager.getClass().getDeclaredFields();
    Field myBreakpoints = Arrays.stream(declaredFields1).filter(e -> e.getName().equals("myBreakpoints")).findAny().get();
    try {
      myBreakpoints.setAccessible(true);
      BidirectionalMap<?, ?> map = (BidirectionalMap<?, ?>) myBreakpoints.get(lineBreakpointManager);
      map.keySet().stream().filter(Objects::nonNull).filter(e->e instanceof XLineBreakpointImpl).forEach(e-> {
        if (((XLineBreakpointImpl) e).getHighlighter() != null) {
          ((XLineBreakpointImpl) e).getHighlighter().setGutterIconRenderer(null);
          ((XLineBreakpointImpl) e).getHighlighter().setGutterIconRenderer(new GutterActionRenderer(new AnAction(AllIcons.Diff.ArrowLeftDown) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {

            }
          }));
        }
        ((XLineBreakpointImpl) e).dispose();
      });
      map.clear();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public static void addVirtualFileManagerListener(Project project){
    VirtualFileManager.getInstance().addVirtualFileManagerListener(new VirtualFileManagerListener() {

    });

    project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
      @Override
      public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        BreakNode userData = project.getUserData(BreakNode.KEY);
        if (userData != null) {
          userData.newFile();
        }
      }
    });
    project.getMessageBus().connect().subscribe(AppTopics.FILE_DOCUMENT_SYNC, new FileDocumentManagerListener() {
      public void fileContentLoaded(@NotNull VirtualFile file, @NotNull Document document) {
        System.out.println("file.getPath() = " + file.getPath());
      }

      @Override
      public void beforeAllDocumentsSaving() {

      }

      @Override
      public void beforeDocumentSaving(@NotNull Document document) {

      }

      @Override
      public void beforeFileContentReload(@NotNull VirtualFile file, @NotNull Document document) {
        System.out.println("file.getPath() = " + file.getPath());
      }

      @Override
      public void fileWithNoDocumentChanged(@NotNull VirtualFile file) {
        System.out.println("file.getPath() = " + file.getPath());
      }

      @Override
      public void fileContentReloaded(@NotNull VirtualFile file, @NotNull Document document) {
        System.out.println("file.getPath() = " + file.getPath());
      }

      @Override
      public void unsavedDocumentDropped(@NotNull Document document) {

      }

      @Override
      public void unsavedDocumentsDropped() {

      }

      @Override
      public void afterDocumentUnbound(@NotNull VirtualFile file, @NotNull Document document) {

      }
    });
    project.getMessageBus().connect().subscribe(BatchFileChangeListener.TOPIC, new BatchFileChangeListener() {
      @Override
      public void batchChangeStarted(Project project, String activityName) {
        System.out.println("activityName = " + activityName);
      }

      @Override
      public void batchChangeCompleted(Project project) {
        System.out.println("activityName = ");
      }
    });
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES,new BulkFileListener(){
      @Override
      public void after(List<? extends VFileEvent> events) {
        //当前监听类修改xml会报错  弃用
        System.out.println("events = " + events);
      }
    });


    project.getMessageBus().connect().subscribe(VirtualFilePointerListener.TOPIC, new VirtualFilePointerListener() {
      @Override
      public void validityChanged(VirtualFilePointer[] pointers) {
        System.out.println("BookmarkToolWindow.validityChanged:"+pointers.length);
      }
    });

    project.getMessageBus().connect().subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, new BranchChangeListener() {
      @Override
      public void branchWillChange(String branchName) {
//        removeAll(project);
        TabMapper userData = project.getUserData(TabMapper.key);
        project.putUserData(DGMToolWindow.branch,true);
        if (userData != null) {
          userData.getTabs().forEach((k,v)->{
            v.bind(branchName);
          });
        }
      }

      @Override
      public void branchHasChanged(String branchName) {
//        removeAll(project);
        TabMapper userData = project.getUserData(TabMapper.key);
        if (userData != null) {
          userData.getTabs().forEach((k,v)->{
            v.unbind(branchName);
          });
        }
        project.putUserData(DGMToolWindow.branch,false);
      }
    });
  }

  public static void addBreakpointListener(Project project){
    project.getMessageBus().connect().subscribe(XBreakpointListener.TOPIC, new XBreakpointListener<XBreakpoint<XBreakpointProperties>>() {
      @Override
      public void breakpointAdded(XBreakpoint<XBreakpointProperties> xBreakpoint) {
        String onChosen = Arrays.stream(new RuntimeException().getStackTrace())
                .map(StackTraceElement::getMethodName)
                .filter(e -> e.equals("onChosen") || e.equals("mouseClicked"))
                .findAny()
                .orElse(null);
        if(onChosen == null || project.getUserData(DGMToolWindow.branch)) {
          return;
        }
        if(xBreakpoint instanceof XLineBreakpointImpl){
          VirtualFile file = ((XLineBreakpointImpl) xBreakpoint).getFile();
          String relativePath = null;
          if(DGMConstant.SYSTEM_FILE.equals(file.getFileSystem().getProtocol())) {
            if(file.getPath().startsWith(project.getBasePath())) {//项目下文件
              relativePath = fileRelative(project, file);
            } else {//项目外的文件
              relativePath = file.getPath();
            }
          } else if (DGMConstant.JAR.equals(file.getFileSystem().getProtocol())|| DGMConstant.JRT.equals(file.getFileSystem().getProtocol())) {
            relativePath = jarRelative(file);
          }
          if(relativePath != null){
            (((XLineBreakpointImpl) xBreakpoint).getProject()).getUserData(BreakNode.KEY).checked(relativePath+":"+((XLineBreakpointImpl<?>) xBreakpoint).getLine(),true);
          }
          if (Utils.getWindow(project).getContentManager().getSelectedContent() != null) {
            ((TreeView) Utils.getWindow(project).getContentManager().getSelectedContent().getComponent()).refreshData();
          }
        }
      }

      @Override
      public void breakpointRemoved(XBreakpoint breakpoint) {
        if(project.getUserData(DGMToolWindow.branch)) {
          return;
        }
        if(breakpoint instanceof XLineBreakpointImpl){
          VirtualFile file = ((XLineBreakpointImpl) breakpoint).getFile();
          String relativePath = null;
          if(DGMConstant.SYSTEM_FILE.equals(file.getFileSystem().getProtocol())) {
            if(file.getPath().startsWith(project.getBasePath())) {//项目下文件
              relativePath = fileRelative(project, file);
            } else {//项目外的文件
              relativePath = file.getPath();
            }
          } else if (DGMConstant.JAR.equals(file.getFileSystem().getProtocol()) || DGMConstant.JRT.equals(file.getFileSystem().getProtocol())) {
            relativePath = jarRelative(file);
          }
          (((XLineBreakpointImpl<?>) breakpoint).getProject()).getUserData(BreakNode.KEY).checked(relativePath + ":"+ ((XLineBreakpointImpl<?>) breakpoint).getLine(),false);
          if (Utils.getWindow(project).getContentManager().getSelectedContent() != null) {
            ((TreeView) Utils.getWindow(project).getContentManager().getSelectedContent().getComponent()).refreshData();
          }
        }
      }
    });
  }

  public static void addActionListener(Project project){
    project.getMessageBus().connect().subscribe(AnActionListener.TOPIC, new AnActionListener(){
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

  public static void addFileEditorManagerListener(Project project){

    ProjectManagerImpl.getInstance().addProjectManagerListener(project, new ProjectManagerListener() {
      @Override
      public void projectClosingBeforeSave(Project project) {

        ContentManager contentManagerIfCreated = Utils.getWindow(project).getContentManagerIfCreated();
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
    project.getMessageBus().connect().subscribe(ProjectLifecycleListener.TOPIC, new ProjectLifecycleListener() {
      @Override
      public void projectComponentsInitialized(@NotNull Project project) {

      }
    });

    List<FileBasedIndexInfrastructureExtension> extensionList = FileBasedIndexInfrastructureExtension.EP_NAME.getExtensionList();

    project.getMessageBus().connect();
    project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {

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

  public static void work(Runnable runnable){
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
  public static String fileRelative(Project project,VirtualFile temp ){
    return temp.getPath().substring(project.getBasePath().length());
  }
  /**
   * 相对路径
   * @param temp
   * @return 相对路径
   */
  public static String jarRelative(VirtualFile temp ){
    String substring = temp.getPath().substring(VfsUtilCore.getRootFile(temp).getPath().length());
    return StringUtil.trimLeading(substring, '/');
  }

  public static void navigate(Project app, VirtualFile virtualFile, int line) {
    swingAlarm.addRequest(() -> {
      XSourcePositionImpl xSourcePosition = XSourcePositionImpl.create(virtualFile, line);
      Navigatable navigatable = xSourcePosition.createNavigatable(app);
      navigatable.navigate(navigatable.canNavigate());
    }, 200);
  }

  public static void notifyError(String msg) {
    swingAlarm.addRequest(() -> {
      Notification notification = new Notification("group", AllIcons.General.Error, NotificationType.ERROR);
      notification.setContent(msg);
      Notifications.Bus.notify(notification);
    }, 100);
  }

  public static void rememberSelect(Project app, VirtualFile virtualFile, int line, Consumer<VirtualFile> runnable) {
    swingAlarm.addRequest(() -> {
      Notification notification = new Notification("group", AllIcons.General.Warning, NotificationType.ERROR);
      notification.setContent("文件:" + virtualFile.getPath());
      notification.setImportant(true);
      notification.addAction(new AnAction("跳转") {
        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
          navigate(app, virtualFile, line);
        }
      });
      notification.addAction(new AnAction("跳转并记住我的选择") {
        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
          navigate(app, virtualFile, line);
          runnable.accept(virtualFile);
        }
      });
      Notifications.Bus.notify(notification);
      notification.hideBalloon();
    }, 0);
  }

  public static void duplication(Project app, String path, Map<String, VirtualFile> alreadyChoose, List<FoundItemDescriptor<Object>> list, Node node, Consumer<VirtualFile> consumer) {

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
          rememberSelect(app, ((PsiFileImpl) item.getItem()).getVirtualFile(), node.getLineNumber(), consumer);
        } else if(item.getItem() instanceof PsiElementBase){
          rememberSelect(app, ((PsiElementBase) item.getItem()).getContainingFile().getVirtualFile(), node.getLineNumber(), consumer);
        }
      }
    }, 0);
  }


  /**
   * ui 线程
   * @param runnable
   */
  public static void ui(Runnable runnable){
    swingAlarm.addRequest(runnable, 100);
  }
}
