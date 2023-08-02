-dontshrink
#-dontoptimize
#-dontobfuscate

#-microedition

-ignorewarnings
-injars build/libs/instrumented-untitled2-1.0-SNAPSHOT.jar
-outjars build/libs/out-untitled2-1.0-SNAPSHOT.jar
-keep public class com.dgm.DGMToolWindow implements ToolWindowFactory, DumbAware, ProjectManagerListener, Runnable{
    public void createToolWindowContent(com.intellij.openapi.project.Project, com.intellij.openapi.wm.ToolWindow);
}

-repackageclasses 'com.dgm'
-allowaccessmodification

-keep class com.dgm.leafaction.**
-keep class com.dgm.tabaction.**
-keep class com.dgm.DGMToolWindow