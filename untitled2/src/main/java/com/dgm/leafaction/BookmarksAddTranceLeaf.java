package com.dgm.leafaction;

import com.intellij.debugger.NoDataException;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.JavaExecutionStack;
import com.intellij.debugger.engine.JavaStackFrame;
import com.intellij.debugger.ui.impl.watch.MethodsTracker;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.impl.XDebugSessionImpl;
import com.jetbrains.jdi.ConcreteMethodImpl;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/25 22:17
 * @description
 */
public class BookmarksAddTranceLeaf extends MyAnAction {
    private Logger log = Logger.getLogger(BookmarksAddTranceLeaf.class.getSimpleName());

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project app = anActionEvent.getProject();
        XDebugSession currentSession = XDebuggerManager.getInstance(anActionEvent.getProject()).getCurrentSession();
        if (app != null && currentSession != null) {
            JavaExecutionStack javaExecutionStack = (JavaExecutionStack) ((XDebugSessionImpl) currentSession).getCurrentExecutionStack();
            try {
                Field myTracker = javaExecutionStack.getClass().getDeclaredField("myTracker");
                myTracker.setAccessible(true);
                MethodsTracker  methodsTracker = (MethodsTracker) myTracker.get(javaExecutionStack);

                Field myCacheField = methodsTracker.getClass().getDeclaredField("myCache");
                myCacheField.setAccessible(true);
                Int2ObjectMap<MethodsTracker.MethodOccurrence> o = (Int2ObjectMap) myCacheField.get(methodsTracker);
                for (int i = 0; i < o.size(); i++) {
                    MethodsTracker.MethodOccurrence apply = o.apply(i);
                    SourcePosition sourcePosition = ((JavaStackFrame) currentSession.getCurrentStackFrame()).getDescriptor().getDebugProcess().getPositionManager().getSourcePosition(apply.getMethod().location());
                    VirtualFile virtualFile = sourcePosition.getFile().getVirtualFile();
                    System.out.println("virtualFile = " + virtualFile);

                }

                Field myMethodCounterFiled = methodsTracker.getClass().getDeclaredField("myMethodCounter");
                myMethodCounterFiled.setAccessible(true);
                Object2IntOpenHashMap<ConcreteMethodImpl> myMethodCounter = (Object2IntOpenHashMap) myMethodCounterFiled.get(methodsTracker);

                myMethodCounter.keySet().stream().forEach(e->{
//                            System.out.println("methodsTracker = " + e.declaringType().toString()+""+e.name());
                });
            } catch (NoSuchFieldException | IllegalAccessException | NoDataException e) {
                e.printStackTrace();
            }
        }
    }

}