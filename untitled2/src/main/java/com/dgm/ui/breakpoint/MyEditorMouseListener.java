package com.dgm.ui.breakpoint;

import com.dgm.DGMToolWindow;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/8/15 20:15
 * @description
 */
public class MyEditorMouseListener implements EditorMouseListener {

    private Project project;

    public MyEditorMouseListener(Project project) {
        this.project = project;
    }

    @Override
    public void mousePressed(@NotNull EditorMouseEvent event) {
        project.putUserData(DGMToolWindow.branch,false);
    }

    @Override
    public void mouseClicked(@NotNull EditorMouseEvent event) {
        project.putUserData(DGMToolWindow.branch,true);

    }

    @Override
    public void mouseReleased(@NotNull EditorMouseEvent event) {
    }

    @Override
    public void mouseEntered(@NotNull EditorMouseEvent event) {

    }

    @Override
    public void mouseExited(@NotNull EditorMouseEvent event) {
    }
}
