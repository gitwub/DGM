package com.dgm.leafaction;

import com.dgm.DGMToolWindow;
import com.dgm.ApplicationContext;
import com.dgm.ui.MyTreeNode;
import com.dgm.ui.TreeView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.content.ContentManager;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

import javax.swing.JComponent;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/25 22:17
 * @description
 */
public class BookmarksUpTreeLeaf extends MyAnAction implements DumbAware {
    private volatile boolean enable = true;
    private Logger log = Logger.getLogger(BookmarksUpTreeLeaf.class.getSimpleName());

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        ApplicationContext app = anActionEvent.getProject().getUserData(DGMToolWindow.key);
        if (enable && app.getToolWindow() != null) {
            ContentManager contentManager = app.getToolWindow().getContentManager();
            if(contentManager.getSelectedContent() != null){
                JComponent component = contentManager.getSelectedContent().getComponent();
                if (component != null) {
                    TreeView treeView = (TreeView) (component);
                    MyTreeNode[] selectedNodes = treeView.getTree().getSelectedNodes(MyTreeNode.class, null);
                    if(selectedNodes.length == 1 && selectedNodes[0].getParent().getIndex(selectedNodes[0]) != 0) {
//                        Node node = selectedNodes[0].node;
//                        List<Node> childrenNodes = node.getParentNode().getChildrenNodes();
//                        int i = childrenNodes.indexOf(node);
//                        if(i == 0) {
//                            if (node.getParentNode().getParentNode() != null) {
//
//                            }
//                        } else {
//                            Node beforeNode = childrenNodes.get(i - 1);
//                            beforeNode.setSortIndex(beforeNode.getSortIndex() + 1);
//
//                            node.setSortIndex(node.getSortIndex() - 1);
//
//                            Collections.swap(childrenNodes, i, i - 1);
                            treeView.treeDataUp();
//                        }
                    }
                }
            }

        }
    }
}