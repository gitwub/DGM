package com.dgm.ui;

import com.intellij.ide.dnd.TransferableList;
import com.intellij.ide.dnd.aware.DnDAwareTree;

import java.awt.Point;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/8/2 21:35
 * @description
 */
public class MyDnDAwareTree extends DnDAwareTree {
    public MyDnDAwareTree(TreeModel treemodel, TreeView treeView) {
        super(treemodel);
        setTransferHandler(new TransferHandler() {
            private TreePath[] treePaths = null;
            @Override
            protected Transferable createTransferable(JComponent component) {
                if (component instanceof JTree) {
                    JTree tree = (JTree)component;
                    TreePath[] selection = tree.getSelectionPaths();
                    treePaths = selection;
                    if (selection != null && selection.length == 1) {
                        return new TransferableList<>(selection) {
                            @Override
                            protected String toString(TreePath path) {
                                return String.valueOf(path.getLastPathComponent());
                            }
                        };
                    }
                }
                return null;
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                MyDnDAwareTree tree = (MyDnDAwareTree) support.getComponent();
                Point location = support.getDropLocation().getDropPoint().getLocation();
                TreePath closestPathForLocation = tree.getClosestPathForLocation(location.x, location.y);
                return closestPathForLocation.getLastPathComponent() != treePaths[0].getLastPathComponent();
            }

            @Override
            public boolean importData(TransferSupport support) {
                MyTreeNode lastPathComponent = (MyTreeNode) treePaths[0].getLastPathComponent();
                MyDnDAwareTree tree = (MyDnDAwareTree) support.getComponent();
                Point location = support.getDropLocation().getDropPoint().getLocation();
                TreePath closestPathForLocation = tree.getClosestPathForLocation(location.x, location.y);
                MyTreeNode folderNode = (MyTreeNode) closestPathForLocation.getLastPathComponent();
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) folderNode.getParent();
                int index = parent.getIndex(folderNode);
                if(folderNode instanceof FolderNode) {
                    DefaultMutableTreeNode parent1 = (DefaultMutableTreeNode) lastPathComponent.getParent();
                    parent1.remove(lastPathComponent);
                    ((DefaultMutableTreeNode) closestPathForLocation.getLastPathComponent()).add(lastPathComponent);
                } else {
                    DefaultMutableTreeNode parent1 = (DefaultMutableTreeNode) lastPathComponent.getParent();
                    parent1.remove(lastPathComponent);
                    parent.insert(lastPathComponent,index);
                }
                treeView.refresh();
                return true;
            }
        });
    }
}
