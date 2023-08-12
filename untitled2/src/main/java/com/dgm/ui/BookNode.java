package com.dgm.ui;

import com.dgm.ApplicationContext;
import com.dgm.DGMToolWindow;
import com.dgm.db.TabMapper;
import com.dgm.ui.renderer.CheckboxTreeCellRenderer;
import com.dgm.DGMConstant;
import com.dgm.db.po.Node;
import com.dgm.ui.util.ColorsUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.searcheverywhere.ClassSearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.FileSearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor;
import com.intellij.ide.util.scopeChooser.ScopeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diff.impl.util.GutterActionRenderer;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.impl.PsiElementBase;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.ui.IconManager;
import com.intellij.util.ui.ThreeStateCheckBox;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.impl.XDebuggerManagerImpl;
import com.intellij.xdebugger.impl.XDebuggerUtilImpl;
import com.intellij.xdebugger.ui.DebuggerColors;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.tree.TreePath;


/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/27 23:00
 * @description
 */
public class BookNode extends MyTreeNode implements com.intellij.openapi.editor.event.DocumentListener, Disposable {
    private static Map<String, VirtualFile> alreadyChoose = new ConcurrentHashMap<>();
    private final String tableName;
    private boolean searchable = false;
    public Icon icon = AllIcons.Nodes.NotFavoriteOnHover;

    private AtomicReference<VirtualFile> virtualFile = new AtomicReference<>();
    private volatile int lineNumber;
    private RangeHighlighterEx rangeHighlighterEx;

    public BookNode(Project app, String tableName, Node node) {
        this(app, node, tableName,false);
    }

    public BookNode(Project app, Node node, String tableName, boolean searchable) {
        super(node);
        this.app = app;
        this.searchable = searchable;
        this.tableName = tableName;
        String path = node.getFilePath();
        setEnabled(path.endsWith("java") || path.endsWith("class"));
        this.isChecked = node.isChecked();
        lineNumber = node.getLineNumber();
        String iconPath = node.getIconPath();
        if(iconPath != null) {
            icon = IconManager.getInstance().getIcon(iconPath,   AllIcons.class);
            if (icon == null) {
                icon = IconManager.getInstance().loadRasterizedIcon(iconPath, AllIcons.class.getClassLoader(), 0, 0);
            }
        }
        VirtualFileManager instance = VirtualFileManager.getInstance();
        String protocol = node.getProtocol();
        VirtualFileSystem fileSystem = instance.getFileSystem(protocol);
        if (DGMConstant.SYSTEM_FILE.equals(protocol)) {
            if(node.getLocation() == DGMConstant.NODE_LOCATION_INNER) {//项目下文件
                virtualFile.set(fileSystem.findFileByPath(app.getBasePath() + path));
                addDebugAndChangeListener();
            } else {//项目外的文件
                virtualFile.set(fileSystem.findFileByPath(path));
                addDebugAndChangeListener();
            }
            addEditorState();
        } else if (DGMConstant.JAR.equals(protocol) || DGMConstant.JRT.equals(protocol)) {
            JButton button = new JButton();
            AtomicReference<List> reference = new AtomicReference();
            button.addActionListener(actionEvent -> {
                List<FoundItemDescriptor<Object>> list = reference.get();
                if (list != null) {
                    if (list.size() == 0) {
                        System.out.println("BookNode.BookNode:"+node.getFilePath()+"::"+node.getNodeName()+list.size());
                        ApplicationContext.notifyError(node.getJarName() + "不存在,无法添加断点");
                    } else if (list.size() == 1) {
                        ApplicationContext.ui(() -> {
                            if(list.get(0).getItem() instanceof  PsiFileImpl) {
                                virtualFile.set(((PsiFileImpl) list.get(0).getItem()).getVirtualFile());
                                addDebugAndChangeListener();
                                addEditorState();
                            } else if(list.get(0).getItem() instanceof PsiElementBase){
                                virtualFile.set(((PsiElementBase) list.get(0).getItem()).getContainingFile().getVirtualFile());
                                addDebugAndChangeListener();
                                addEditorState();
                            }
                        });
                    } else {
                        if(alreadyChoose.get(path) != null){
                            ApplicationContext.ui(() -> {
                                virtualFile.set(alreadyChoose.get(path));
                                addDebugAndChangeListener();
                                addEditorState();
                            });
                        } else {
                            Consumer<VirtualFile> runnable = e->{
                                alreadyChoose.put(path, e);
                                addDebugAndChangeListener();
                                addEditorState();
                            };
                            ApplicationContext.duplication(app, path, alreadyChoose, list, node, runnable);
                        }
                    }
                }
            });


            ArrayList list = new ArrayList();
            if(path.endsWith("java")) {
                globalSearchFile(path, button, reference, list);
            }
            if(path.endsWith("class")) {
                globalSearchClass(path, button, reference, list);
            }

        } else {
            //TODO 未知文件系统类型
        }
    }

    private void globalSearchClass(String path, JButton button, AtomicReference<List> reference, ArrayList list) {
        DataContext dataContext = DataManager.getInstance().getDataContext(app.getUserData(DGMToolWindow.windowComponent));
        ClassSearchEverywhereContributor classSearchEverywhereContributor = new ClassSearchEverywhereContributor(new AnActionEvent(null, dataContext, "", new Presentation(), ActionManager.getInstance(), 0));
        classSearchEverywhereContributor.setScope(new ScopeDescriptor(new ProjectAndLibrariesScope(app)));
        BackgroundableProcessIndicator classIndicator = new BackgroundableProcessIndicator(new Task.Backgroundable(app, path) {
            @Override
            public void run(ProgressIndicator progressIndicator) {
                progressIndicator.cancel();
            }
        });
        ApplicationContext.work(()->{
            try {
                List<Object> items = (List<Object>) classSearchEverywhereContributor.searchWeightedElements(path.substring(path.indexOf("/")+1).replace("/", ".").replace(".class", "").replace(".java", ""), classIndicator, 10).getItems();
                Set<String> paths = new HashSet<>();
                list.addAll(items.stream()
                        .filter(e->paths.add(((FoundItemDescriptor<PsiElementBase>)(e)).getItem().getContainingFile().getVirtualFile().getPath()))
                        .filter(e->((FoundItemDescriptor<PsiElementBase>)(e)).getItem().getContainingFile().getVirtualFile().getPath().endsWith(path.replace(".java", ".class")))
                        .collect(Collectors.toList()));
                if (list.size() == 0) {
                    list.addAll(
                            classSearchEverywhereContributor.searchWeightedElements(path.substring(path.indexOf("/")+1).replace("/", ".").replace(".class", "").replace(".java", ""), classIndicator, 10).getItems().stream()
                            .filter(e->paths.add(((FoundItemDescriptor<PsiElementBase>)(e)).getItem().getContainingFile().getVirtualFile().getPath()))
                            .filter(e->((FoundItemDescriptor<PsiElementBase>)(e)).getItem().getContainingFile().getVirtualFile().getPath().endsWith(path.replace(".java", ".class")))
                            .collect(Collectors.toList()));
                }
            } catch (Throwable exception) {
                exception.printStackTrace();
            } finally {
                classIndicator.processFinish();
                reference.set(list);
                button.doClick();
            }
        });
    }

    private void globalSearchFile(String path, JButton button, AtomicReference<List> reference, ArrayList list) {
        DataContext dataContext = DataManager.getInstance().getDataContext(app.getUserData(DGMToolWindow.windowComponent));
        FileSearchEverywhereContributor fileSearchEverywhereContributor = new FileSearchEverywhereContributor(new AnActionEvent(null, dataContext, "", new Presentation(), ActionManager.getInstance(), 0));
        fileSearchEverywhereContributor.setScope(new ScopeDescriptor(new ProjectAndLibrariesScope(app)));
        BackgroundableProcessIndicator fileIndicator = new BackgroundableProcessIndicator(new Task.Backgroundable(app, path){
            @Override
            public void run(ProgressIndicator progressIndicator) {
                progressIndicator.start();
            }
        });

        ApplicationContext.work(()->{

            BackgroundableProcessIndicator fileIndicator2 = null;
            try {
                List<Object> items = (List<Object>) fileSearchEverywhereContributor.searchWeightedElements(path, fileIndicator, 10).getItems();
                Set<String> paths = new HashSet<>();
                list.addAll(items.stream()
                        .filter(e->paths.add(((FoundItemDescriptor<PsiFileImpl>) e).getItem().getVirtualFile().getPath()))
                        .filter(e->((FoundItemDescriptor<PsiFileImpl>) e).getItem().getVirtualFile().getPath().endsWith(path))
                        .collect(Collectors.toList()));

                if(list.size() == 0) {
                    fileIndicator2 = new BackgroundableProcessIndicator(new Task.Backgroundable(app, path){
                        @Override
                        public void run(ProgressIndicator progressIndicator) {
                            progressIndicator.start();
                        }
                    });
                    list.addAll(fileSearchEverywhereContributor.searchWeightedElements(path.substring(path.indexOf("/")+1), fileIndicator2, 10).getItems().stream()
                            .filter(e->paths.add(((FoundItemDescriptor<PsiFileImpl>) e).getItem().getVirtualFile().getPath()))
                            .filter(e->((FoundItemDescriptor<PsiFileImpl>) e).getItem().getVirtualFile().getPath().endsWith(path))
                            .collect(Collectors.toList()));
                }
            } catch (Throwable exception) {
                exception.printStackTrace();
            } finally {
                fileIndicator.processFinish();
                if (fileIndicator2 != null) {
                    fileIndicator2.processFinish();
                }

                reference.set(list);
                button.doClick();
            }
        });
    }

    private void addDebugAndChangeListener() {
        if(searchable || virtualFile.get() == null) {
            return;
        }

        if(isChecked) {
            XDebuggerUtilImpl.getInstance().toggleLineBreakpoint(app, virtualFile.get(), lineNumber, true);
        } else {
            XBreakpoint<?>[] allBreakpoints = XDebuggerManagerImpl.getInstance(app).getBreakpointManager().getAllBreakpoints();
            for (int i = 0; i < allBreakpoints.length; i++) {
                XSourcePosition sourcePosition = allBreakpoints[i].getSourcePosition();
                if (sourcePosition != null && sourcePosition.getFile().getPath().equals(virtualFile.get().getPath()) && sourcePosition.getLine() == lineNumber) {
                    XDebuggerUtilImpl.getInstance().toggleLineBreakpoint(app, virtualFile.get(), lineNumber, true);
                    node.setState(isChecked ? DGMConstant.NODE_EXPANDED : DGMConstant.NODE_COLLAPSED);
                    this.isChecked = true;
                    node.setChecked(true);
                    if (app.getUserData(TreeView.key) != null) {
                        app.getUserData(TreeView.key).getTree().requestFocus();
                        FileEditorManager.getInstance(app).openFile(virtualFile.get(),true);
                    }
                }
            }
        }

        FileDocumentManager.getInstance().getDocument(virtualFile.get()).addDocumentListener(this, this);

    }

    public void removeEditorState() {
        MarkupModelEx markupModelEx = (MarkupModelEx) DocumentMarkupModel.forDocument(FileDocumentManager.getInstance().getDocument(virtualFile.get()), app, true);
        if(rangeHighlighterEx != null) {
            markupModelEx.removeHighlighter(rangeHighlighterEx);
        }
        rangeHighlighterEx = null;
    }

    @Override
    public void addEditorState() {
        if (this.searchable || virtualFile.get() == null) {
            return;
        }
        Color textColor = ColorsUtil.getColor(this,Node::getEditorTextColor, CheckboxTreeCellRenderer.flow);
        Color bgColor = ColorsUtil.getColor(this,Node::getEditorBgColor, CheckboxTreeCellRenderer.flow);
        Color styleColor = ColorsUtil.getColor(this,Node::getEditorStyleColor, CheckboxTreeCellRenderer.flow);
        String editorStyle = ColorsUtil.getStyle(this,Node::getEditorStyle, CheckboxTreeCellRenderer.flow);
        EffectType effectType = null;
        if (!CheckboxTreeCellRenderer.flow.equals(editorStyle)) {
            effectType = EffectType.valueOf(editorStyle);
        }

        TextAttributes attributes = new TextAttributes(textColor,bgColor,styleColor, effectType, 0);
        MarkupModelEx markupModelEx = (MarkupModelEx) DocumentMarkupModel.forDocument(FileDocumentManager.getInstance().getDocument(virtualFile.get()), app, true);
        if(rangeHighlighterEx != null) {
            markupModelEx.removeHighlighter(rangeHighlighterEx);
        }
        rangeHighlighterEx = markupModelEx.addPersistentLineHighlighter(lineNumber, 10000, attributes);
        if (rangeHighlighterEx != null) {
            rangeHighlighterEx.putUserData(DebuggerColors.BREAKPOINT_HIGHLIGHTER_KEY, Boolean.TRUE);
            rangeHighlighterEx.setEditorFilter(MarkupEditorFilterFactory.createIsNotDiffFilter());
            rangeHighlighterEx.setErrorStripeMarkColor(bgColor);
            rangeHighlighterEx.setGutterIconRenderer(new GutterActionRenderer(new AnAction(AllIcons.Diff.Arrow) {
                @Override
                public void actionPerformed(AnActionEvent anActionEvent) {
                    TabMapper tabMapper = app.getUserData(TabMapper.key);
                    TreeView treeView = app.getUserData(TreeView.key);
                    if (treeView != null) {
                        if(tabMapper != null && !treeView.getTreeViewName().equals(tableName)) {
                            tabMapper.activeName(tableName);
                        }

                        treeView.getTree().expandPath(new TreePath(BookNode.this.getPath()));
                        treeView.getTree().scrollPathToVisible(new TreePath(BookNode.this.getPath()));
                        treeView.getTree().clearSelection();
                        treeView.getTree().addSelectionPath(new TreePath(BookNode.this.getPath()));
                    }
                }
            }));
            rangeHighlighterEx.setErrorStripeTooltip(node.getNodeName());
            rangeHighlighterEx.setTextAttributes(attributes);
        }

    }

    @Override
    public Icon icon() {
        return icon;
    }

    public ThreeStateCheckBox.State getState() {
        if (!isEnabled()) {
            return ThreeStateCheckBox.State.DONT_CARE;
        } else {
            if(isChecked()){
                return ThreeStateCheckBox.State.SELECTED;
            } else {
                return ThreeStateCheckBox.State.NOT_SELECTED;
            }
        }
    }

    public void navigate() {
        if (node != null) {
            String path = node.getFilePath();
            VirtualFileSystem file = VirtualFileManager.getInstance().getFileSystem(node.getProtocol());

            if ((DGMConstant.JAR.equals(node.getProtocol()) || DGMConstant.JRT.equals(node.getProtocol())) && (path.endsWith("java") || path.endsWith("class"))) {
                JButton button = new JButton();
                AtomicReference<List> reference = new AtomicReference();
                button.addActionListener(actionEvent -> {
                    List<FoundItemDescriptor<Object>> list = reference.get();
                    if (list != null) {
                        if (list.size() == 0) {
                            ApplicationContext.notifyError("包不存在,无法跳转到:" + path);
                        } else if (list.size() == 1) {
                            if(list.get(0).getItem() instanceof  PsiFileImpl) {
                                ApplicationContext.navigate(app, ((PsiFileImpl) list.get(0).getItem()).getVirtualFile(), node.getLineNumber());
                            } else if(list.get(0).getItem() instanceof PsiElementBase){
                                ApplicationContext.navigate(app, ((PsiElementBase) list.get(0).getItem()).getContainingFile().getVirtualFile(), node.getLineNumber());
                            } else {
                                ApplicationContext.notifyError("未知问题请联系作者!!!");
                            }
                        } else {
                            if(alreadyChoose.get(path) != null){
                                ApplicationContext.navigate(app, alreadyChoose.get(path), node.getLineNumber());
                            } else {
                                ApplicationContext.duplication(app, path, alreadyChoose, list, node, e->alreadyChoose.put(path, e));
                            }
                        }
                    }
                });

                ArrayList list = new ArrayList();
                if(path.endsWith("java")) {
                    globalSearchFile(path, button,reference,list);
                }
                if(path.endsWith("class")) {
                    globalSearchClass(path, button,reference,list);
                }
            } else if(DGMConstant.SYSTEM_FILE.equals(node.getProtocol())){
                VirtualFile virtualFile = null;
                if(DGMConstant.LOCATION_INNER == node.getLocation()) {
                    virtualFile = file.findFileByPath(app.getBasePath() + path);
                } else {
                    virtualFile = file.findFileByPath(path);
                }

                if (virtualFile != null) {
                    ApplicationContext.navigate(app, virtualFile, node.getLineNumber());
                } else {
                    ApplicationContext.notifyError("文件不存在:" + path);
                }
            }
        }
    }
    @Override
    public String nodeDesc(){
        return node.getNodeName();
    }

    public void checked(boolean checked) {
        this.isChecked = checked;
//        node.setState(isChecked ? Constant.NODE_EXPANDED : Constant.NODE_COLLAPSED);
    }

    @Override
    public void setChecked(boolean checked) {
        this.isChecked = checked;
        if (virtualFile.get() != null) {
            node.setState(isChecked ? DGMConstant.NODE_EXPANDED : DGMConstant.NODE_COLLAPSED);
            XDebuggerUtilImpl.getInstance().toggleLineBreakpoint(app, virtualFile.get(), lineNumber, checked);
        }
    }


    @Override
    public void documentChanged(DocumentEvent event) {
        if (rangeHighlighterEx == null) {
            return;
        }

        if(node().getLocked() == null || DGMConstant.LOCKED_.equals(node().getLocked())) {
            this.rangeHighlighterEx.getStartOffset();
            this.rangeHighlighterEx.getEndOffset();
            int newLine = this.rangeHighlighterEx.getDocument().getLineNumber(this.rangeHighlighterEx.getStartOffset());

            if (lineNumber == newLine) {
                addEditorState();
            }
            if(lineNumber != newLine){
                lineNumber = newLine;
                node.setLineNumber(newLine);
            }
        }
    }

    @Override
    public void beforeDocumentChange(DocumentEvent event) {

    }

    @Override
    public void dispose() {
        FileDocumentManager.getInstance().getDocument(virtualFile.get()).removeDocumentListener(this);
    }

    public void removeDebugColor() {
        if (virtualFile != null && rangeHighlighterEx != null) {
            MarkupModelEx markupModelEx = (MarkupModelEx) DocumentMarkupModel.forDocument(FileDocumentManager.getInstance().getDocument(virtualFile.get()), app, true);
            markupModelEx.removeHighlighter(rangeHighlighterEx);
        }
    }

    public void removeListener() {
        if(virtualFile.get() != null) {
            FileDocumentManager.getInstance().getDocument(virtualFile.get()).removeDocumentListener(this);
        }
    }

    @Override
    public void lock() {
        if (virtualFile != null && !virtualFile.get().isWritable()) {
            return;
        }
        if (node.getLocked() == null) {
            node.setLocked(DGMConstant.LOCKED_);
        }
    }

    @Override
    public void unlock() {
        if (node.getLocked() != null) {
            node.setLocked(null);
        }
    }

    @Override
    public void bind(String branchName) {
        if (virtualFile != null && !virtualFile.get().isWritable()) {
            return;
        }
        if(node.getLocked() == null) {
            node.setLocked(branchName);
            removeEditorState();
        }
    }

    @Override
    public void unbind() {
        if(node.getLocked() != null && !DGMConstant.LOCKED_.equals(node.getLocked())) {
            node.setLocked(null);
            addEditorState();
        }
    }

    @Override
    public void autoLockOrBind() {
        if(node.getLocked() == null) {

        } else {
            if(DGMConstant.LOCKED_.equals(node.getLocked())) {
                addEditorState();
            } else {
                removeEditorState();
            }
        }
    }

    @Override
    public void autoUnlockOrUnbind(String branchName) {
        if (branchName.equals(node.getLocked())) {
            node.setLocked(null);
            addEditorState();
        }
    }

    @Override
    public void bindIfNull(String branchName) {
        if (node.getLocked() == null) {
            node.setLocked(branchName);
            removeEditorState();
        }
    }
}
