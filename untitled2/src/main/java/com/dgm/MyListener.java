package com.dgm;

import com.dgm.ui.util.BreakNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/8/16 21:32
 * @description
 * <applicationListeners>
 *        	  <listener class="com.plugin.MyBulkFileListener"
 *                     topic="com.intellij.openapi.vfs.newvfs.BulkFileListener">
 *      	</applicationListeners>
 */
public class MyListener implements com.intellij.openapi.vfs.newvfs.BulkFileListener{

    private final Project project;

    public MyListener(Project project) {
        this.project = project;
    }

    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        events.stream()
                .filter(e->e instanceof VFileDeleteEvent)
                .forEach(e->{
                    if (project.getUserData(BreakNode.KEY) != null) {
                        project.getUserData(BreakNode.KEY).deleteFile(((VFileDeleteEvent) e).getFile());
                    }
                });
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
//        events.stream()
//                .filter(e->e instanceof VFileCreateEvent)
//                .forEach(e->{
//                    if (project.getUserData(BreakNode.KEY) != null) {
//                        project.getUserData(BreakNode.KEY).newFile();
//                    }
//                });
    }
}
