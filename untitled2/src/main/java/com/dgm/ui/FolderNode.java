package com.dgm.ui;

import com.dgm.ApplicationContext;
import com.dgm.db.po.Node;
import com.intellij.icons.AllIcons;

import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/27 23:00
 * @description
 */
public class FolderNode extends MyTreeNode {
    public Icon icon = AllIcons.Nodes.Folder;
    public FolderNode(ApplicationContext app, Node node) {
        super(node);
        setAllowsChildren(true);
        this.app = app;
    }

    @Override
    public Icon icon() {
        return icon;
    }

    @Override
    public String nodeDesc(){
        return node.getNodeName();
    }

    @Override
    public void addEditorState() {
        addEditorChidren(children());
    }

    private void addEditorChidren(Enumeration<TreeNode> children) {
        while (children.hasMoreElements()) {
            TreeNode treeNode = children.nextElement();
            if(treeNode instanceof BookNode) {
                ((BookNode) treeNode).addEditorState();
            } else {
                addEditorChidren(((DefaultMutableTreeNode) treeNode).children());
            }
        }
    }
}
