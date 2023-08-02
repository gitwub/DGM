package com.dgm.tabaction;

import com.dgm.ApplicationContext;
import com.dgm.DGMToolWindow;
import com.dgm.db.TabMapper;
import com.dgm.ui.LogUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/25 22:17
 * @description
 */
public class BookmarksAddAction extends AnAction implements DumbAware {
    private Logger log = Logger.getLogger(BookmarksAddAction.class.getSimpleName());


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        ApplicationContext app = anActionEvent.getProject().getUserData(DGMToolWindow.key);
        Project project = anActionEvent.getProject();
        String name = Messages.showInputDialog(project, "新增一个书签列表","新增一个书签列表",  AllIcons.General.Add, "new tab", new InputValidator() {
            public boolean checkInput(String inputString) {
                return inputString != null && inputString.trim().length() > 0;
            }

            public boolean canClose(String inputString) {
                inputString = inputString.trim().replace(" ", "");
                if (anActionEvent.getProject().getUserData(TabMapper.key).contains(inputString)) {
                    Messages.showErrorDialog(project, inputString + "书签列表已存在", "提醒");
                    return false;
                } else {
                    return inputString.length() > 0;
                }
            }
        });
        name = name.trim().replace(" ", "");
        if (name != null && name.length() != 0) {
            anActionEvent.getProject().getUserData(TabMapper.key).add(name);

            LogUtils.newTab(app,"tab name %s", name);
        }
    }

}