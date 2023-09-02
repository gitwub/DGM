package com.dgm.leafaction;

import com.dgm.ui.renderer.CheckboxTreeCellRenderer;
import com.dgm.DGMConstant;
import com.dgm.db.po.Node;
import com.intellij.find.impl.FindResultImpl;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.impl.jar.JarFileSystemImpl;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.ui.CoreIconManager;
import com.intellij.util.ui.JBImageIcon;

import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/25 22:17
 * @description
 */
public abstract class MyAnAction extends AnAction {
    private Logger log = Logger.getLogger(MyAnAction.class.getSimpleName());

    public Node createNode(Editor editor) {
        int line = editor.getCaretModel().getPrimaryCaret().getEditor().getCaretModel().getLogicalPosition().line;
        int column = editor.getCaretModel().getPrimaryCaret().getEditor().getCaretModel().getLogicalPosition().column;
        int lineEndOffset = editor.getDocument().getLineEndOffset(line);
        Node node = new Node();
        node.setLineNumber(line);
        node.setColumn(column);
        node.setLineEndOffset(lineEndOffset);

        Caret caret = editor.getCaretModel().getPrimaryCaret();
        int visualLineStart = caret.getVisualLineStart();
        int visualLineEnd = caret.getVisualLineEnd();
        if(caret.getSelectedText() != null && !"".equals(caret.getSelectedText())) {
            node.setOriginText(caret.getSelectedText());
        } else {
            node.setOriginText(editor.getDocument().getText(new FindResultImpl(visualLineStart, visualLineEnd)).trim());
        }
        String trim = editor.getDocument().getText(new FindResultImpl(visualLineStart, visualLineEnd)).trim();
        node.setNodeName(trim);
        String protocol = (((EditorImpl) (editor))).getVirtualFile().getFileSystem().getProtocol();
        node.setProtocol(protocol);
        String path = ((EditorImpl) editor).getVirtualFile().getPath();
        if (DGMConstant.SYSTEM_FILE.equals(protocol)) {
            if(path.startsWith(editor.getProject().getBasePath())) {//项目下文件
                node.setFilePath(fileRelative(((EditorImpl) editor).getVirtualFile(), editor.getProject()));
                node.setLocation(DGMConstant.LOCATION_INNER);
            } else {//项目外的文件
                node.setFilePath(path);
                node.setLocation(DGMConstant.LOCATION_OUT);
            }
        } else if (DGMConstant.JAR.equals(protocol)) {
            JarFileSystemImpl fileSystem = (JarFileSystemImpl) (((EditorImpl) (editor))).getVirtualFile().getFileSystem();
            String jarName = fileSystem.getVirtualFileForJar(((EditorImpl) (editor)).getVirtualFile()).getName();
            node.setJarName(jarName);
            node.setFilePath(jarRelative(((EditorImpl) (editor)).getVirtualFile()));
        } else if (DGMConstant.JRT.equals(protocol)) {
            VirtualFileSystem fileSystem = (((EditorImpl) (editor))).getVirtualFile().getFileSystem();
            node.setFilePath(jarRelative(((EditorImpl) (editor)).getVirtualFile()));
        }
        node.setChecked(DGMConstant.NODE_CHECKED_NO);
        node.setColor(CheckboxTreeCellRenderer.flow);
        return node;
    }


    /**
     * 相对路径
     * @param temp
     * @return 相对路径
     */
    public static String jarRelative(VirtualFile temp ){
        String substring = temp.getPath().substring(VfsUtilCore.getRootFile(temp).getPath().length());
        return StringUtil.trimLeading(substring, '/');
    }

    /**
     * 相对路径
     * @param temp
     * @param project
     * @return 相对路径
     */
    public static String fileRelative(VirtualFile temp, @Nullable Project project){
        return temp.getPath().substring(project.getBasePath().length());
    }

}