package com.dgm;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.VetoableProjectManagerListener;

import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/8/10 2:31
 * @description
 */
public class MyProjectManager implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull Project project) {

        System.out.println("project = " + project);
    }
}
