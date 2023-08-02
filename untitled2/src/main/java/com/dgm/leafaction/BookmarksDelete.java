package com.dgm.leafaction;

import com.dgm.ui.TreeView;
import com.intellij.openapi.actionSystem.AnActionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/25 22:17
 * @description
 */
public class BookmarksDelete extends MyAnAction {

    private Logger log = Logger.getLogger(BookmarksDelete.class.getSimpleName());

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TreeView userData = anActionEvent.getProject().getUserData(TreeView.key);
        if (userData != null) {
            userData.bookmarkDelete(anActionEvent);
        }
    }

}