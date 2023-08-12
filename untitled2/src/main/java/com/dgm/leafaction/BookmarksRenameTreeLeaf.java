package com.dgm.leafaction;

import com.dgm.ApplicationContext;
import com.dgm.DGMToolWindow;
import com.dgm.Utils;
import com.dgm.ui.TreeView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.content.ContentManager;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

import javax.swing.JComponent;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/4/25 22:17
 * @description
 */
public class BookmarksRenameTreeLeaf extends MyAnAction implements DumbAware {
  private volatile boolean enable = true;
  private Logger log = Logger.getLogger(BookmarksRenameTreeLeaf.class.getSimpleName());

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    if (enable && Utils.getWindow(anActionEvent.getProject()) != null) {
      ContentManager contentManager = Utils.getWindow(anActionEvent.getProject()).getContentManager();
      if (contentManager.getSelectedContent() != null) {
        JComponent component = contentManager.getSelectedContent().getComponent();
        if (component != null) {
          ((TreeView) (component)).renameBookmark();
        }
      }
    }
  }

}
