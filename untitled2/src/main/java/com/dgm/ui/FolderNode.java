package com.dgm.ui;

import com.dgm.ApplicationContext;
import com.dgm.DGMConstant;
import com.dgm.db.po.Node;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;

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
    public FolderNode(Project app, Node node) {
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
    public void unlock() {
        node.setLocked(null);
        unlockNode(this);
    }

    @Override
    public void lock() {
        node.setLocked(DGMConstant.LOCKED_);
        lockNode(this);
    }


    public void lockNode(MyTreeNode selectedNode) {
        if(selectedNode instanceof FolderNode) {
            int childCount = selectedNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                MyTreeNode treeNode = (MyTreeNode) selectedNode.getChildAt(childCount - (i+1));
                lockNode(treeNode);
            }
        } else {
            BookNode bn = (BookNode) selectedNode;
            bn.lock();
        }
    }

    public void unlockNode(MyTreeNode selectedNode) {
        if(selectedNode instanceof FolderNode) {
            int childCount = selectedNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                MyTreeNode treeNode = (MyTreeNode) selectedNode.getChildAt(childCount - (i+1));
                unlockNode(treeNode);
            }
        } else {
            BookNode bn = (BookNode) selectedNode;
            bn.unlock();
        }
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

    @Override
    public void bind(String branchName) {
        node.setLocked(branchName);
        bindNode(this, branchName);
    }



    public void bindNode(MyTreeNode selectedNode, String branchName) {
        if(selectedNode instanceof FolderNode) {
            int childCount = selectedNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                MyTreeNode treeNode = (MyTreeNode) selectedNode.getChildAt(childCount - (i+1));
                bindNode(treeNode, branchName);
            }
        } else {
            BookNode bn = (BookNode) selectedNode;
            bn.bind(branchName);
        }
    }


    @Override
    public void unbind() {
        node.setLocked(null);
        unbindNode(this);
    }

    public void unbindNode(MyTreeNode selectedNode) {
        if(selectedNode instanceof FolderNode) {
            int childCount = selectedNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                MyTreeNode treeNode = (MyTreeNode) selectedNode.getChildAt(childCount - (i+1));
                unbindNode(treeNode);
            }
        } else {
            BookNode bn = (BookNode) selectedNode;
            bn.unbind();
        }
    }


    @Override
    public void autoLockOrBind() {
        autoLockOrBind(this);
    }


    public void autoLockOrBind(MyTreeNode selectedNode) {
        if(selectedNode instanceof FolderNode) {
            int childCount = selectedNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                MyTreeNode treeNode = (MyTreeNode) selectedNode.getChildAt(childCount - (i+1));
                autoLockOrBind(treeNode);
            }
        } else {
            BookNode bn = (BookNode) selectedNode;
            bn.autoLockOrBind();
        }
    }
    @Override
    public void autoUnlockOrUnbind(String branchName) {
        if (branchName.equals(node.getLocked())) {
            node.setLocked(null);
        }
        autoUnlockOrUnbindNode(this, branchName);
    }

    public void autoUnlockOrUnbindNode(MyTreeNode selectedNode,String branchName) {
        if(selectedNode instanceof FolderNode) {
            int childCount = selectedNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                MyTreeNode treeNode = (MyTreeNode) selectedNode.getChildAt(childCount - (i+1));
                autoUnlockOrUnbindNode(treeNode, branchName);
            }
        } else {
            BookNode bn = (BookNode) selectedNode;
            bn.autoUnlockOrUnbind(branchName);
        }
    }

    @Override
    public void bindIfNull(String branchName) {
        if (node.getLocked() == null) {
            node.setLocked(branchName);
        }
        bindIfNullNode(this, branchName);
    }

    public void bindIfNullNode(MyTreeNode selectedNode,String branchName) {
        if(selectedNode instanceof FolderNode) {
            int childCount = selectedNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                MyTreeNode treeNode = (MyTreeNode) selectedNode.getChildAt(childCount - (i+1));
                bindIfNullNode(treeNode, branchName);
            }
        } else {
            BookNode bn = (BookNode) selectedNode;
            bn.bindIfNull(branchName);
        }
    }
}
