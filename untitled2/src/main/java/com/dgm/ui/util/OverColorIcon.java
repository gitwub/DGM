package com.dgm.ui.util;

import com.intellij.util.ui.ColorIcon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/6/25 1:08
 * @description
 */
public class OverColorIcon extends ColorIcon {

    public OverColorIcon(Color color) {
        super(16, color);
    }

    @Override
    public void paintIcon(Component component, Graphics g, int i, int j) {
        int iconWidth = this.getIconWidth();
        int iconHeight = this.getIconHeight();
        g.setColor(this.getIconColor());
        int width = 16;
        int height = 16;
        int x = i + (iconWidth - width) / 2;
        int y = j + (iconHeight - height) / 2;
        g.fillOval(x, y, width, height);
    }
}
