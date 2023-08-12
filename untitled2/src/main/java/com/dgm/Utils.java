package com.dgm;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/8/10 2:47
 * @description
 */
public class Utils {

    public static ToolWindow getWindow(Project project) {
        return ToolWindowManagerEx.getInstance(project).getToolWindow(DGMConstant.WINDOW_NAME);
    }
}
