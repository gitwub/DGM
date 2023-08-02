package com.dgm.leafaction;

import com.dgm.ApplicationContext;
import com.dgm.DGMToolWindow;
import com.dgm.ui.TreeView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;

import org.jetbrains.annotations.NotNull;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/25 22:17
 * @description
 */
public class BookmarkCollapseall extends MyAnAction implements DumbAware {


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        ApplicationContext app = anActionEvent.getProject().getUserData(DGMToolWindow.key);
        TreeView treeView = (TreeView) app.getToolWindow().getContentManager().getSelectedContent().getComponent();
    }

}