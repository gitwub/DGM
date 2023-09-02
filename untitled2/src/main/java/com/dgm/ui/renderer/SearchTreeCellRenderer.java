package com.dgm.ui.renderer;

import com.dgm.ui.BookNode;
import com.dgm.ui.FolderNode;
import com.dgm.ui.MyTreeNode;
import com.dgm.db.po.Node;
import com.dgm.ui.util.ColorsUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.accessibility.AccessibleContextDelegate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;

import javax.accessibility.AccessibleContext;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import static com.dgm.ui.renderer.CheckboxTreeCellRenderer.getIconByPath;


/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/5/7 18:18
 * @description
 */
public class SearchTreeCellRenderer extends JPanel implements TreeCellRenderer {
    private final ColoredTreeCellRenderer myTextRenderer;

    public SearchTreeCellRenderer(boolean opaque) {
        this(opaque, true);
    }

    public SearchTreeCellRenderer(boolean opaque, boolean usePartialStatusForParentNodes) {
        super(new BorderLayout());
        this.myTextRenderer = new NodeRenderer() {
            @Override
            public void customizeCellRenderer(JTree tree, @NlsSafe Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
                if(value instanceof BookNode || value instanceof FolderNode) {
                    clear();
                    if(value instanceof BookNode) {
                        setIcon(getIconByPath(((MyTreeNode)(value)).node().getFilePath()));
                    }

                    Color textColor = ColorsUtil.getColor( (DefaultMutableTreeNode)value, Node::getColor, ColorsUtil.convertColorRGB(ColorsUtil.BLACK));
                    Color styleColor = ColorsUtil.getColor( (DefaultMutableTreeNode)value, Node::getWaveColor, ColorsUtil.convertColorRGB(ColorsUtil.BLACK));
                    String style = ColorsUtil.getStyle( (DefaultMutableTreeNode)value, Node::getTextStyle, ColorsUtil.DEF_TYPE);

                    SimpleTextAttributes simpleTextAttributes = new SimpleTextAttributes(textColor, textColor, styleColor, Integer.valueOf(style));
                    append(value.toString(), simpleTextAttributes);
                }
            }
        };
        this.myTextRenderer.setOpaque(opaque);
        this.add(this.myTextRenderer, "Center");
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        this.myTextRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        revalidate();
        repaint();
        return this;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.red);
        super.paint(g);
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleContextDelegate(super.getAccessibleContext()) {
                @Override
                protected Container getDelegateParent() {
                    return SearchTreeCellRenderer.this.getParent();
                }

                @Override
                public String getAccessibleName() {
                    return "";
                }
            };
        }

        return this.accessibleContext;
    }

    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

    }



}
