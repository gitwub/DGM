package com.dgm.ui;

import com.dgm.ApplicationContext;
import com.dgm.db.po.Node;
import com.intellij.openapi.project.Project;
import com.intellij.ui.CheckedTreeNode;

import javax.swing.Icon;


/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/29 23:08
 * @description
 */
public abstract class MyTreeNode extends CheckedTreeNode {
//    protected TreeView treeView;
    protected Project app;
    protected Node node;

    public MyTreeNode(Node node) {
        super(node.getNodeName());
        this.node = node;
    }

    public Icon icon() {
        return null;
    }


    public String nodeDesc() {
        return node.getNodeName();
    }

    public Node node() {
        return node;
    }


    public String getJavaBreakKey() {
        return node.getFilePath() + ":" + node.getLineNumber();
    }

    public String getId() {
        return node.getId();
    }


    public void addEditorState() {

    }

    public abstract void lock();

    public abstract void unlock();

    public abstract void bind(String branchName);

    public abstract void unbind();

    public abstract void autoLockOrBind();

    public abstract void autoUnlockOrUnbind(String branchName);

    public abstract void bindIfNull(String branchName);

    public abstract void debug();

}