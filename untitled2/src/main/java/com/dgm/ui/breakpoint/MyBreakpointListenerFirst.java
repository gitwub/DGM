package com.dgm.ui.breakpoint;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.tasks.context.WorkingContextProvider;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointManagerImpl;

import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/8/15 19:19
 * @description
 */
public class MyBreakpointListenerFirst extends WorkingContextProvider {


    @Override
    public @NotNull String getId() {
        return "xDebugger";
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getDescription() {
        return "MyBreakpointListenerFirst";
    }

    @Override
    public void saveContext(@NotNull Project project, @NotNull Element toElement) {
        XBreakpointManagerImpl breakpointManager = ((XBreakpointManagerImpl) XDebuggerManager.getInstance(project).getBreakpointManager());
        XBreakpointBase<?,?,?>[] breakpoints = breakpointManager.getAllBreakpoints();
        for (final XBreakpointBase<?, ?, ?> breakpoint : breakpoints) {
            ApplicationManager.getApplication().runWriteAction(() -> breakpointManager.removeBreakpoint(breakpoint));
        }
    }

    @Override
    public void loadContext(@NotNull Project project, @NotNull Element fromElement) {
        XBreakpointManagerImpl breakpointManager = ((XBreakpointManagerImpl)XDebuggerManager.getInstance(project).getBreakpointManager());
        XBreakpointBase<?,?,?>[] breakpoints = breakpointManager.getAllBreakpoints();
        for (final XBreakpointBase<?, ?, ?> breakpoint : breakpoints) {
            ApplicationManager.getApplication().runWriteAction(() -> breakpointManager.removeBreakpoint(breakpoint));
        }
        fromElement.getContent().clear();
    }
}
