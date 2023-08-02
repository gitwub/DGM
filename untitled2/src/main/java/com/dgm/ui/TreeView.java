package com.dgm.ui;

import com.dgm.ApplicationContext;
import com.dgm.ui.util.BreakNode;
import com.dgm.ui.renderer.CheckboxTreeCellRenderer;
import com.dgm.DGMConstant;
import com.dgm.db.po.Node;
import com.dgm.db.NodeMapper;
import com.dgm.ui.adapter.MyMouseAdapter;
import com.dgm.ui.adapter.SearchMouseAdapter;
import com.dgm.ui.renderer.SearchTreeCellRenderer;
import com.intellij.find.SearchTextArea;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.favoritesTreeView.FavoritesManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.EmptyAction;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.components.BorderLayoutPanel;

import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/26 23:54
 * @description
 */
public class TreeView extends BorderLayoutPanel {

    public static Key<TreeView> key = new Key(TreeView.class.getName());

    private StringBuffer filterKey = new StringBuffer();

    private static Logger log = Logger.getLogger(TreeView.class.getName());
    private SearchTextArea comp;
    private ApplicationContext app;
    private String name;
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode(DGMConstant.ROOT);
    private DefaultTreeModel treemodel = new DefaultTreeModel(root);
    private Tree tree = new Tree(treemodel);
    private CheckboxTreeCellRenderer renderer = new CheckboxTreeCellRenderer();
    public AtomicReference<TreePath> treePathAtomicReference = new AtomicReference<>();

    private NodeMapper nodeMapper;
    private JScrollPane scroll = new JBScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    private Tree searchTree = new Tree(new DefaultTreeModel(new DefaultMutableTreeNode()));
    private AnAction newFolder = ActionManager.getInstance().getAction("new_folder");
    private AnAction bookmarkUp = ActionManager.getInstance().getAction("bookmark_up");
    private AnAction bookmarkDown = ActionManager.getInstance().getAction("bookmark_down");
    private AnAction bookmarkDel = ActionManager.getInstance().getAction("bookmark_delete");
    private AnAction editAction = ActionManager.getInstance().getAction("bookmark_rename");

    public TreeView(ApplicationContext app, String name) {
        this.app= app;
        this.name = name;
        tree.setRootVisible(false);
        tree.addMouseListener(new MyMouseAdapter(app, this, renderer));
        nodeMapper = new NodeMapper(app, root, name);
        nodeMapper.buildTrees();
        tree.setCellRenderer(renderer);
        searchTree.setRootVisible(false);
        searchTree.addMouseListener(new SearchMouseAdapter());
        searchTree.setCellRenderer(new SearchTreeCellRenderer(true));

        createSearchHeader();
        createTreeCenter();

        refreshData();
        nodeMapper.getExpandTreePaths().stream().forEach(e->tree.expandPath(e));
    }


    public void scrollPathToVisible(MyTreeNode myTreeNode){
        tree.scrollPathToVisible(new TreePath(myTreeNode.getPath()));
    }

    public Set<DefaultMutableTreeNode> expandedList = new HashSet<>();

    public void refreshData() {
        refreshExpand();
    }

    public void treeDataUp() {
        DefaultMutableTreeNode[] selectedNodes = tree.getSelectedNodes(DefaultMutableTreeNode.class, null);
        MyTreeNode selectedNode = (MyTreeNode)selectedNodes[0];
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
        int index = parent.getIndex(selectedNode);
        parent.remove(index);
        parent.insert(selectedNode,index - 1);
        LogUtils.nodeUp(app,"node name %s", selectedNode.node().getNodeName());
        refresh(tree, treemodel, root);
        tree.setSelectionPath(new TreePath(selectedNode.getPath()));
    }

    public void treeDataDown() {
        DefaultMutableTreeNode[] selectedNodes = tree.getSelectedNodes(DefaultMutableTreeNode.class, null);
        MyTreeNode selectedNode = (MyTreeNode)selectedNodes[0];
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
        int index = parent.getIndex(selectedNode);
        parent.remove(index);
        parent.insert(selectedNode,index + 1);
        LogUtils.nodeDown(app,"node name %s", selectedNode.node().getNodeName());
        refresh(tree, treemodel, root);
        tree.setSelectionPath(new TreePath(selectedNode.getPath()));
    }

    private void createTreeCenter() {
        scroll.setViewportView(tree);
        addToCenter(scroll);
    }

    private void createSearchHeader() {
        JBTextArea textArea = new JBTextArea("");
        comp = new SearchTextArea(textArea, true);
        Document document = textArea.getDocument();
        document.addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filter(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        comp.setMultilineEnabled(false);

        EmptyAction.wrap(bookmarkDel).registerCustomShortcutSet(bookmarkDel.getShortcutSet(), this);
        EmptyAction.wrap(bookmarkUp).registerCustomShortcutSet(bookmarkUp.getShortcutSet(),this);
        EmptyAction.wrap(bookmarkDown).registerCustomShortcutSet(bookmarkDown.getShortcutSet(),this);
        EmptyAction.wrap(newFolder).registerCustomShortcutSet(CustomShortcutSet.fromString("LEFT"),this);
        EmptyAction.wrap(editAction).registerCustomShortcutSet(editAction.getShortcutSet(), this);
        List<Component> components = comp.setExtraActions(newFolder, bookmarkUp, bookmarkDown);
        for (Component button : components) {
            button.setFocusable(false);//取消焦点
        }
        addToTop(comp);
    }

    public void bookmarkDelete(@NotNull AnActionEvent anActionEvent) {
        MyTreeNode[] selectedNodes = tree.getSelectedNodes(MyTreeNode.class, null);
        for (MyTreeNode selectedNode : selectedNodes) {
            if(selectedNode instanceof BookNode) {
                deleteFolderChildren(selectedNode);
                LogUtils.delNode(app,"node name %s", selectedNode.node().getNodeName());
            } else if(selectedNode instanceof FolderNode) {
                int i = 0;
                if(selectedNode.getChildCount() != 0) {
                    i = Messages.showYesNoDialog("文件夹[" + selectedNode.node().getNodeName() + "]内的书签全部删除", "删除文件夹", AllIcons.General.Remove);
                }
                if (i == 0){
                    deleteFolderChildren(selectedNode);
                }
            }
        }
        treePathAtomicReference.set(null);
        refreshExpand();
    }

    public void renameBookmark(){
        String desc = "";
        if (name != null && name.length() != 0) {
            desc = tree.getSelectedNodes(MyTreeNode.class,null)[0].nodeDesc();
        }
        String name = Messages.showInputDialog(app.getProject(), "重命名节点","重命名节点",  AllIcons.General.Add, desc, new InputValidator() {
            @Override
            public boolean checkInput(@NlsSafe String inputString) {
                return inputString != null && inputString.trim().length() > 0;
            }

            @Override
            public boolean canClose(@NlsSafe String inputString) {
                inputString = inputString.trim();
                final FavoritesManager favoritesManager = FavoritesManager.getInstance(app.getProject());
                if (favoritesManager.getAvailableFavoritesListNames().contains(inputString)) {
                    Messages.showErrorDialog(app.getProject(), IdeBundle.message("error.favorites.list.already.exists", new Object[]{inputString.trim()}), IdeBundle.message("title.unable.to.add.favorites.list", new Object[0]));
                    return false;
                } else {
                    return inputString.length() > 0;
                }
            }
        });
        MyTreeNode selectedNode = tree.getSelectedNodes(MyTreeNode.class, null)[0];
        log.info("节点：【"+selectedNode.node().getNodeName() + "】修改为：【" +name+"】");
        if (name != null && name.length() != 0 && !selectedNode.node().getNodeName().equals(name)) {
            selectedNode.node().setNodeName(name);
            selectedNode.setUserObject(name);
            treePathAtomicReference.set(new TreePath(selectedNode.getPath()));
            LogUtils.renameNode(app,"node name %s", selectedNode.node().getNodeName());
            refreshData();
        }
    }
    private void filter(DocumentEvent e) {
        try {
            filterKey.delete(0, filterKey.length());
            filterKey.append(e.getDocument().getText(0, e.getDocument().getLength()));
            if(filterKey.length() == 0) {
                scroll.setViewportView(tree);
                newFolder.getTemplatePresentation().setEnabled(true);
            } else {
                newFolder.getTemplatePresentation().setEnabled(false);
                searcher();
                if (scroll.getViewport().getView() != searchTree) {
                    scroll.setViewportView(searchTree);
                }
                ((DefaultTreeModel) searchTree.getModel()).reload();
            }
            comp.updateExtraActions();
        } catch (BadLocationException badLocationException) {
            badLocationException.printStackTrace();
        }
    }

    private void searcher() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        ((DefaultMutableTreeNode) searchTree.getModel().getRoot()).removeAllChildren();
        search(root.children());
    }

    private void search(Enumeration<TreeNode> children) {
        while (children.hasMoreElements()) {
            MyTreeNode treeNode = (MyTreeNode) children.nextElement();
            if (treeNode instanceof FolderNode) {
                search(treeNode.children());
            } else {
                if (treeNode.getUserObject().toString().contains(filterKey.toString())) {
                    ((DefaultMutableTreeNode) searchTree.getModel().getRoot()).add(new BookNode(app, treeNode.node(), name, true));
                }
            }
        }
    }

    public void deleteFolderChildren(MyTreeNode selectedNode) {
        if(selectedNode instanceof FolderNode) {
            int childCount = selectedNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                MyTreeNode treeNode = (MyTreeNode) selectedNode.getChildAt(childCount - (i+1));
                deleteFolderChildren(treeNode);
            }
            selectedNode.removeFromParent();
        } else {
            BookNode bn = (BookNode) selectedNode;
            app.getProject().getUserData(BreakNode.KEY).remove(bn, (Supplier<Object>) () -> {
                bn.removeDebugColor();
                return null;
            });
            bn.removeListener();
            selectedNode.removeFromParent();
        }
    }

    public void refreshExpand() {
        refresh(tree, treemodel, root);
        tree.clearSelection();
        if(root.getChildCount() == 0){
            treePathAtomicReference.set(null);
        }
        if (treePathAtomicReference.get() != null) {
            tree.addSelectionPath(treePathAtomicReference.get());
            tree.scrollPathToVisible(new TreePath(treePathAtomicReference.get().getPath()));
        } else {
            tree.clearSelection();
        }
    }

    @NotNull
    public Tree getTree() {
        return tree;
    }

    public DefaultMutableTreeNode getRootNode() {
        return root;
    }



    public void newFolder() {
        String name = Messages.showInputDialog(app.getProject(), DGMConstant.NEW_FOLDER, DGMConstant.NEW_FOLDER,  AllIcons.Actions.NewFolder, DGMConstant.NEW_FOLDER, new InputValidator() {
            @Override
            public boolean checkInput(@NlsSafe String inputString) {
                return inputString != null && inputString.trim().length() > 0;
            }
            @Override
            public boolean canClose(@NlsSafe String inputString) {
                return inputString.trim().length() > 0;
            }
        });
        if (name != null && name.length() != 0) {
            log.info(name);
            Node n = new Node();
            n.setId(nodeMapper.getNodeId());
            n.setNodeName(name);
            n.setDgmType(DGMConstant.NODE_GROUP);
            n.setState(DGMConstant.NODE_EXPANDED);
            MyTreeNode node = new FolderNode(app, n);
            if (tree.getSelectedNodes(FolderNode.class,null).length != 0) {
                MyTreeNode myTreeNode = tree.getSelectedNodes(FolderNode.class, null)[0];
                myTreeNode.add(node);
                n.setParentId(myTreeNode.node().getId());
                tree.expandPath(new TreePath(myTreeNode.getPath()));
                treePathAtomicReference.set(new TreePath(tree.getSelectedNodes(FolderNode.class, null)[0].getPath()));
            } else if (tree.getSelectedNodes(BookNode.class,null).length != 0 && tree.getSelectedNodes(BookNode.class,null)[0] instanceof MyTreeNode) {
                MyTreeNode myTreeNode = tree.getSelectedNodes(BookNode.class, null)[0];
                if (myTreeNode.getParent() instanceof FolderNode) {
                    n.setParentId(((FolderNode) myTreeNode.getParent()).node().getId());
                } else {
                    n.setParentId(DGMConstant.ROOT);
                }
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) myTreeNode.getParent();
                int index = parent.getIndex(myTreeNode) + 1;
                parent.insert(node,index);
                treePathAtomicReference.set(new TreePath(myTreeNode.getPath()));
            } else {
                root.add(node);
                n.setParentId(DGMConstant.ROOT);
                treePathAtomicReference.set(new TreePath(node.getPath()));
            }

            try {
                nodeMapper.insertNode(n);
                LogUtils.newFolder(app,"node name %s", n.getNodeName());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                throw new RuntimeException("db 操作失败");
            }
            refreshExpand();
        }
    }


    public ApplicationContext getApp() {
        return app;
    }

    public NodeMapper getNodeMapper() {
        return nodeMapper;
    }


    public void saving() {
        nodeMapper.saveToDB(tree);
    }

    private void refresh(JTree jTree, DefaultTreeModel treeModel, DefaultMutableTreeNode root) {
        List<TreePath> treePaths = new ArrayList<>();
        recordExpandPath(treePaths, root.children());
        treeModel.reload();
        treePaths.forEach(jTree::expandPath);
    }


    private void recordExpandPath(List<TreePath> treePaths, Enumeration<TreeNode> children) {
        while (children.hasMoreElements()) {
            MyTreeNode treeNode = (MyTreeNode) children.nextElement();
            if(treeNode.node().getDgmType() == DGMConstant.NODE_GROUP) {
                TreePath path = new TreePath(treeNode.getPath());
                if(tree.isExpanded(path)) {
                    treePaths.add(path);
                }

                recordExpandPath(treePaths, treeNode.children());
            }
        }
    }

    public String getTreeViewName() {
        return name;
    }

    public void delete() {
        int i = Messages.showYesNoDialog("删除tab后不可恢复", "删除tab", AllIcons.General.Remove);
        if (i == 0){
            for (int childCount = root.getChildCount() - 1; childCount >= 0; childCount--) {
                deleteFolderChildren((MyTreeNode) root.getChildAt(childCount));
            }
        }
    }
}
