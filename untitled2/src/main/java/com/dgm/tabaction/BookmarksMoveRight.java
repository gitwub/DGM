package com.dgm.tabaction;

import com.dgm.ApplicationContext;
import com.dgm.DGMToolWindow;
import com.dgm.db.TabMapper;
import com.dgm.ui.LogUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/25 22:17
 * @description
 */
public class BookmarksMoveRight extends AnAction implements DumbAware {
    private Logger log = Logger.getLogger(BookmarksMoveRight.class.getSimpleName());
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        anActionEvent.getProject().getUserData(TabMapper.key).down();
        ApplicationContext app = anActionEvent.getProject().getUserData(DGMToolWindow.key);
        LogUtils.tabRight(app,"node name %s", anActionEvent.getProject().getUserData(TabMapper.key).currentActiveName());
    }

}