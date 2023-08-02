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
public class BookmarksDownTreeLeaf extends MyAnAction implements DumbAware {
    private Logger log = Logger.getLogger(BookmarksUpTreeLeaf.class.getSimpleName());

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        log.info(anActionEvent+"");
        ApplicationContext app = anActionEvent.getProject().getUserData(DGMToolWindow.key);
        if (app.getToolWindow() != null) {
            ContentManager contentManager = app.getToolWindow().getContentManager();
            if( contentManager.getSelectedContent() != null){
                JComponent component = contentManager.getSelectedContent().getComponent();
                if (component != null) {
                    TreeView treeView = (TreeView) (component);
                    MyTreeNode[] selectedNodes = treeView.getTree().getSelectedNodes(MyTreeNode.class, null);
                    if(selectedNodes.length == 1 && selectedNodes[0].getParent().getIndex(selectedNodes[0]) != selectedNodes[0].getParent().getChildCount() - 1) {
//                        Node node = selectedNodes[0].node;
//                        Node parentNode = node.getParentNode();
//                        List<Node> childrenNodes = parentNode.getChildrenNodes();
//                        int i = childrenNodes.indexOf(node);
//                        if(i == childrenNodes.size() - 1) {
//                            if (parentNode.getParentNode() != null) {
//                                childrenNodes.remove(childrenNodes.size() - 1);
//                            }
//                        } else {
//                            Node afterNode = childrenNodes.get(i + 1);
//                            afterNode.setSortIndex(afterNode.getSortIndex() - 1);
//
//                            node.setSortIndex(node.getSortIndex() + 1);
//
//                            Collections.swap(childrenNodes, i, i + 1);
                            treeView.treeDataDown();
//                        }
                    }
                }
            }

        }
    }

}