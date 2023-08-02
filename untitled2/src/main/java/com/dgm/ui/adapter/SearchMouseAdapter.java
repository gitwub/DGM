package com.dgm.ui.adapter;

import com.dgm.ui.BookNode;
import com.intellij.ui.CheckedTreeNode;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/6/26 22:58
 * @description
 */
public class SearchMouseAdapter extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent event) {
        super.mouseClicked(event);
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
            }
        }
    }
}
