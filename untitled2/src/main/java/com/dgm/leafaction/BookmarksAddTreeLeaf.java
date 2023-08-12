package com.dgm.leafaction;

import com.dgm.ApplicationContext;
import com.dgm.DGMToolWindow;
import com.dgm.DGMConstant;
import com.dgm.Utils;
import com.dgm.db.po.Node;
import com.dgm.ui.BookNode;
import com.dgm.ui.FolderNode;
import com.dgm.ui.LogUtils;
import com.dgm.ui.MyTreeNode;
import com.dgm.ui.TreeView;
import com.dgm.ui.util.BreakNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.EditorFactoryImpl;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.ContentManager;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/25 22:17
 * @description
 */
public class BookmarksAddTreeLeaf extends MyAnAction {
    private Logger log = Logger.getLogger(BookmarksAddTreeLeaf.class.getSimpleName());
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project app = anActionEvent.getProject();
        if (app != null) {
            ContentManager contentManager = Utils.getWindow(app).getContentManager();
            if( contentManager.getSelectedContent() != null){
                JComponent component = contentManager.getSelectedContent().getComponent();
                if (component != null) {
                    TreeView treeView = (TreeView) (component);
                    MyTreeNode[] selectedNodes = treeView.getTree().getSelectedNodes(MyTreeNode.class, null);

                    Node node = createNode(anActionEvent.getRequiredData(CommonDataKeys.EDITOR));
                    node.setId(treeView.getNodeMapper().getNodeId());
                    BookNode mNode = new BookNode(app, treeView.getTreeViewName(), node);
                    if(selectedNodes.length == 1 && selectedNodes[0] instanceof BookNode) {
                        ((DefaultMutableTreeNode) selectedNodes[0].getParent()).insert(mNode, selectedNodes[0].getParent().getIndex(selectedNodes[0]) + 1);
                        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNodes[0].getParent();
                        if(parent instanceof MyTreeNode) {
                            node.setParentId(((MyTreeNode) parent).node().getId());
                            node.setLocked(((MyTreeNode) parent).node().getLocked());
                        } else {
                            node.setParentId(DGMConstant.ROOT);
                        }
                    } else if(selectedNodes.length == 1 && selectedNodes[0] instanceof FolderNode) {
                        selectedNodes[0].add(mNode);
                        node.setParentId(selectedNodes[0].node().getId());
                        node.setLocked(selectedNodes[0].node().getLocked());
                        treeView.getTree().expandPath(new TreePath(((DefaultMutableTreeNode)mNode.getParent()).getPath()));
                    } else {
                        node.setParentId(DGMConstant.ROOT);
                        treeView.getRootNode().add(mNode);
                    }
                    if (mNode.node().getLocked() != null) {
                        mNode.autoLockOrBind();
                    }
                    treeView.treePathAtomicReference.set(new TreePath(mNode.getPath()));
//                    LogUtils.newNode(app,"node name %s", mNode.node().getNodeName());
                    app.getUserData(BreakNode.KEY).add(mNode);
                    treeView.refreshData();
                }
            }

        }
    }

}