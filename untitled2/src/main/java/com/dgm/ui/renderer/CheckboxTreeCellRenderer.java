package com.dgm.ui.renderer;

import com.dgm.DGMConstant;
import com.dgm.db.po.Node;
import com.dgm.ui.BookNode;
import com.dgm.ui.FolderNode;
import com.dgm.ui.MyTreeNode;
import com.dgm.ui.util.ColorsUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.IconManager;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.ThreeStateCheckBox;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.accessibility.AccessibleContextDelegate;
import com.intellij.util.ui.accessibility.AccessibleContextUtil;

import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.ConcurrentHashMap;

import javax.accessibility.AccessibleContext;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.WEST;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/5/7 18:18
 * @description
 */
public class CheckboxTreeCellRenderer extends JPanel implements TreeCellRenderer {
    private final ColoredTreeCellRenderer myTextRenderer;
    public final ThreeStateCheckBox myCheckbox;
    public final JBLabel locked;
    public static final String flow = "跟随";
    private final boolean myUsePartialStatusForParentNodes;
    protected boolean myIgnoreInheritance;
    private static ConcurrentHashMap<String, Icon> icons = new ConcurrentHashMap<>();

    public CheckboxTreeCellRenderer(boolean opaque) {
        this(opaque, true);
    }

    public CheckboxTreeCellRenderer(boolean opaque, boolean usePartialStatusForParentNodes) {
        super(new BorderLayout());
        this.myUsePartialStatusForParentNodes = usePartialStatusForParentNodes;
        this.myCheckbox = new ThreeStateCheckBox();
        this.locked = new JBLabel();
        this.myCheckbox.setSelected(false);
        this.myCheckbox.setThirdStateEnabled(false);
        this.myTextRenderer = new NodeRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, @NlsSafe Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
                if(value instanceof BookNode || value instanceof FolderNode) {
                    clear();
                    if(value instanceof BookNode) {
                        setIcon(getIconByPath(((MyTreeNode)(value)).node().getFilePath()));
                    } else {
                        setIcon(AllIcons.Nodes.Folder);
                    }
//
                    Color textColor = ColorsUtil.getColor( (DefaultMutableTreeNode)value, Node::getColor, ColorsUtil.convertColorRGB(ColorsUtil.BLACK));
                    Color styleColor = ColorsUtil.getColor( (DefaultMutableTreeNode)value, Node::getWaveColor, ColorsUtil.convertColorRGB(ColorsUtil.BLACK));
                    String style = ColorsUtil.getStyle( (DefaultMutableTreeNode)value, Node::getTextStyle, ColorsUtil.DEF_TYPE);

                    SimpleTextAttributes simpleTextAttributes = new SimpleTextAttributes(textColor, textColor, styleColor, Integer.valueOf(style));
                    append(value.toString(), simpleTextAttributes);
                }
            }
        };
        this.myTextRenderer.setOpaque(opaque);
        this.add(this.myCheckbox, WEST);
        this.add(this.myTextRenderer, CENTER);
        this.add(this.locked, EAST);
    }

    public static Icon getIconByPath(String filePath) {
        String path = "";
        try {
            if (filePath.endsWith(".uml")) {
                path = "fileTypes/diagram.svg";
            } else if (filePath.endsWith(".class")) {
                path = "fileTypes/java.svg";
            } else if (filePath.endsWith(".java")) {
                path = "nodes/class.svg";
            } else if (filePath.endsWith(".patch")) {
                path = "vcs/patch_file.svg";
            } else if (filePath.endsWith(".sql")) {
                path = "icons/sql.svg";
            } else if (filePath.endsWith(".snippet")) {
                path = "fileTypes/java.svg";
            } else if (filePath.endsWith(".css")) {
                path = "fileTypes/css.svg";
            } else if (filePath.endsWith(".txt")) {
                path = "fileTypes/text.svg";
            } else if (filePath.endsWith(".json")) {
                path = "fileTypes/json.svg";
            } else if (filePath.endsWith(".xml")) {
                path = "fileTypes/xml.svg";
            } else if (filePath.endsWith(".html")) {
                path = "fileTypes/html.svg";
            } else if (filePath.endsWith(".dtd")) {
                path = "fileTypes/dtd.svg";
            } else if (filePath.endsWith(".mf")) {
                path = "fileTypes/manifest.svg";
            } else if (filePath.endsWith(".scratch")) {
                path = "fileTypes/text.svg";
            } else if (filePath.endsWith(".yaml")) {
                path = "fileTypes/yaml.svg";
            } else if (filePath.endsWith(".xhtml")) {
                path = "fileTypes/xhtml.svg";
            } else if (filePath.endsWith(".ignore")) {
                path = "vcs/ignore_file.svg";
            } else if (filePath.endsWith(".gitignore")) {
                path = "vcs/ignore_file.svg";
            }


            if ("".equals(path)) {
                path = "nodes/notFavoriteOnHover.svg";
            }
            if (icons.containsKey(path)) {
                return icons.get(path);
            } else {
                Icon icon = IconManager.getInstance().getIcon(path, AllIcons.class);
                if (icon == null) {
                    icon = AllIcons.Nodes.NotFavoriteOnHover;
                }
                icons.put(path, icon);
                return icons.get(path);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return AllIcons.Nodes.NotFavoriteOnHover;
    }

    public CheckboxTreeCellRenderer() {
        this(true);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof MyTreeNode) {
            MyTreeNode node = (MyTreeNode) value;
            if(((MyTreeNode) value).node().getLocked() == null || ((MyTreeNode) value).node().getLocked().equals(DGMConstant.LOCKED_)) {
                this.myCheckbox.setVisible(node.isEnabled());
                this.myCheckbox.setEnabled(node.isEnabled());
                if (value instanceof BookNode) {
                    this.myCheckbox.setSelected(node.isChecked());
                    this.myCheckbox.setState(((BookNode) value).getState());
                } else {
                    this.myCheckbox.setEnabled(true);
                    this.myCheckbox.setSelected(getNodeStatus(node) == ThreeStateCheckBox.State.SELECTED);
                }
                this.myCheckbox.setOpaque(false);
                this.myCheckbox.setBackground(null);
                this.myCheckbox.setForeground(Color.RED);
                if (UIUtil.isUnderWin10LookAndFeel()) {
                    Object hoverValue = this.getClientProperty("JCheckBox.rollOver.rectangle");
                    this.myCheckbox.getModel().setRollover(hoverValue == value);
                    Object pressedValue = this.getClientProperty("JCheckBox.pressed.rectangle");
                    this.myCheckbox.getModel().setPressed(pressedValue == value);
                }
            } else {
                this.myCheckbox.setVisible(false);
            }
//            append("12313123", ERROR_ATTRIBUTES);

            if(DGMConstant.LOCKED_.equals(((MyTreeNode) value).node().getLocked())) {
                this.locked.setIcon(AllIcons.Nodes.Padlock);
                this.locked.setText(null);
            } else {
                this.locked.setIcon(AllIcons.Vcs.Branch);
                this.locked.setText(((MyTreeNode) value).node().getLocked());
            }
            this.locked.setOpaque(false);
            this.locked.setVisible(((MyTreeNode) value).node().getLocked() != null);
        } else {
            this.myCheckbox.setVisible(false);
        }

        this.myTextRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        this.customizeRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
        repaint();
        return this;
    }

    private ThreeStateCheckBox.State getNodeStatus(CheckedTreeNode node) {
        if (this.myIgnoreInheritance) {
            return node.isChecked() ? ThreeStateCheckBox.State.SELECTED : ThreeStateCheckBox.State.NOT_SELECTED;
        } else {
            boolean checked = node.isChecked();
            if (node.getChildCount() != 0 && this.myUsePartialStatusForParentNodes) {
                ThreeStateCheckBox.State result = null;

                for (int i = 0; i < node.getChildCount(); ++i) {
                    TreeNode child = node.getChildAt(i);
                    ThreeStateCheckBox.State childStatus = child instanceof CheckedTreeNode ? this.getNodeStatus((CheckedTreeNode) child) : (checked ? ThreeStateCheckBox.State.SELECTED : ThreeStateCheckBox.State.NOT_SELECTED);
                    if (childStatus == ThreeStateCheckBox.State.DONT_CARE) {
                        continue;
                    }

                    if (result == null) {
                        result = childStatus;
                    } else if (result != childStatus) {
                        return ThreeStateCheckBox.State.NOT_SELECTED;
                    }
                }

                return result == null ? ThreeStateCheckBox.State.NOT_SELECTED : result;
            } else {
                return node.isEnabled() ? (node.isChecked() ? ThreeStateCheckBox.State.SELECTED : ThreeStateCheckBox.State.NOT_SELECTED) : ThreeStateCheckBox.State.DONT_CARE;
            }
        }
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.accessibleContext == null) {
            this.accessibleContext = new AccessibleContextDelegate(super.getAccessibleContext()) {
                @Override
                protected Container getDelegateParent() {
                    return CheckboxTreeCellRenderer.this.getParent();
                }

                @Override
                public String getAccessibleName() {
                    return AccessibleContextUtil.combineAccessibleStrings(CheckboxTreeCellRenderer.this.myTextRenderer.getAccessibleContext().getAccessibleName(), CheckboxTreeCellRenderer.this.myCheckbox.isSelected() ? "checked" : "not checked");
                }
            };
        }

        return this.accessibleContext;
    }

    public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof CheckedTreeNode) {
            this.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
        }

    }

    /**
     * @deprecated
     */
    @Deprecated
    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

    }

}
