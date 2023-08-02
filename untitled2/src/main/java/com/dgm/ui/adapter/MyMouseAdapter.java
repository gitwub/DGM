package com.dgm.ui.adapter;

import com.dgm.ApplicationContext;
import com.dgm.ui.BookNode;
import com.dgm.ui.util.BreakNode;
import com.dgm.ui.FolderNode;
import com.dgm.ui.MyTreeNode;
import com.dgm.ui.TreeView;
import com.dgm.ui.action.BookAction;
import com.dgm.ui.renderer.CheckboxTreeCellRenderer;
import com.intellij.ui.CheckedTreeNode;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/6/26 22:58
 * @description
 */
public class MyMouseAdapter extends MouseAdapter {
    private ApplicationContext app;
    private TreeView treeView;
    private CheckboxTreeCellRenderer renderer;

    public MyMouseAdapter(ApplicationContext app, TreeView treeView, CheckboxTreeCellRenderer renderer) {
        this.app = app;
        this.treeView = treeView;
        this.renderer = renderer;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        super.mouseClicked(event);
        if(event.getButton() == 3) {
            BookAction.listAction(treeView, event.getComponent()).show(event);
            return;
        }

        JTree tree = (JTree)event.getSource();
        int x = event.getX();
        int y = event.getY();
        int row = tree.getClosestRowForLocation(x, y);
        TreePath path = tree.getPathForRow(row);
        if(path != null) {
            CheckedTreeNode node = (CheckedTreeNode)path.getLastPathComponent();
            if(event.getClickCount() == 2){
                if (node instanceof BookNode) {
                    ((BookNode) (node)).navigate();
                }
            } else {
                Rectangle rowBounds = tree.getRowBounds(row);
                renderer.setBounds(rowBounds);
                Rectangle checkBounds = renderer.myCheckbox.getBounds();
                checkBounds.setLocation(rowBounds.getLocation());

                if (checkBounds.height == 0) {
                    checkBounds.height = checkBounds.width = rowBounds.height;
                }

                if (checkBounds.contains(event.getPoint())) {
                    if (node.isEnabled()) {

                        if(node instanceof BookNode) {
                            node.setChecked(!node.isChecked());
                            app.getProject().getUserData(BreakNode.KEY).checked(((BookNode)node).getJavaBreakKey(), node.isChecked());
                        } else if(node instanceof FolderNode){
                            Enumeration<TreeNode> children = node.children();
                            boolean checked = false;
                            if (node.getChildCount() == 0) {
                                checked = true;
                            } else {
                                ArrayList<Boolean> objects = new ArrayList<>();
                                while (children.hasMoreElements()) {
                                    MyTreeNode item = (MyTreeNode) (children.nextElement());
                                    objects.add(item.isChecked());
                                    if(!item.isChecked()) {
                                        break;
                                    }
                                }

                                if (objects.stream().allMatch(e->e.equals(true))) {
                                    checked = false;
                                } else {
                                    checked = true;
                                }
                            }
                            check(node, checked);
                        }
                        treeView.treePathAtomicReference.set(null);
                        treeView.getTree().clearSelection();
                    }
                }
            }

        }
    }


    private void check(CheckedTreeNode node, boolean checked){
        if (node.isEnabled()) {
            node.setChecked(checked);
        }
        Enumeration<TreeNode> children = node.children();

        if(node instanceof BookNode) {
            return;
        }
        while (children.hasMoreElements()) {
            MyTreeNode item = (MyTreeNode) (children.nextElement());
            if (node.isEnabled()) {
                item.setChecked(checked);
                if (item instanceof BookNode) {
                    app.getProject().getUserData(BreakNode.KEY).checked(item.getJavaBreakKey(), node.isChecked());
                } else {
                    check(item, checked);
                }
            }
        }
    }
}
