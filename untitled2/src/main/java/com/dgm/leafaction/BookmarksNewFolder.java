package com.dgm.leafaction;

import com.dgm.ApplicationContext;
import com.dgm.DGMToolWindow;
import com.dgm.Utils;
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
public class BookmarksNewFolder extends MyAnAction implements DumbAware {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TreeView treeView = (TreeView)  Utils.getWindow(anActionEvent.getProject()).getContentManager().getSelectedContent().getComponent();
        treeView.newFolder();
    }

}