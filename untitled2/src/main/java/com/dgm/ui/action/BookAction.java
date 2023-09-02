package com.dgm.ui.action;

import com.dgm.DGMConstant;
import com.dgm.db.po.Node;
import com.dgm.ui.BookNode;
import com.dgm.ui.MyTreeNode;
import com.dgm.ui.TreeView;
import com.dgm.ui.renderer.CheckboxTreeCellRenderer;
import com.dgm.ui.util.ColorsUtil;
import com.dgm.ui.util.OverColorIcon;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsProjectSetProcessor;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.ui.awt.RelativePoint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.tree.TreePath;

import git4idea.GitReference;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryImpl;
import git4idea.repo.GitRepositoryInitializer;
import git4idea.repo.GitRepositoryManager;

import static com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.MNEMONICS;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/6/15 21:13
 * @description
 */
public class BookAction {
    private ActionGroup textColorGroup = new ActionGroup("text color",true) {
        @Override
        public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
            return ColorsUtil.colorArr.stream().map(e-> createColorAction(ColorsUtil.colors, e, Node::setColor)).collect(Collectors.toList()).toArray(new AnAction[]{});
        }
    };

    private ActionGroup styleColorGroup = new ActionGroup("style color",true) {
        @Override
        public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
            return ColorsUtil.colorArr.stream().map(e-> createColorAction(ColorsUtil.colors, e, Node::setWaveColor)).collect(Collectors.toList()).toArray(new AnAction[]{});
        }
    };

    private ActionGroup styleGroup = new ActionGroup("text style",true) {
        @Override
        public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
            return ColorsUtil.nodeStyleArr.stream().map(e -> createColorAction(ColorsUtil.nodeStyle, e, Node::setTextStyle)).collect(Collectors.toList()).toArray(new AnAction[]{});
        }
    };


    private ActionGroup editorTextColor = new ActionGroup("editor text color",true) {
        @Override
        public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
            return ColorsUtil.colorArr.stream().map(e-> createEditorColorAction(ColorsUtil.colors, e, Node::setEditorTextColor)).collect(Collectors.toList()).toArray(new AnAction[]{});
        }
    };
    private ActionGroup editorBgColor = new ActionGroup("editor bg color",true) {
        @Override
        public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
            return ColorsUtil.colorArr.stream().map(e-> createEditorColorAction(ColorsUtil.colors, e, Node::setEditorBgColor)).collect(Collectors.toList()).toArray(new AnAction[]{});
        }
    };
    private ActionGroup editorSytleColor = new ActionGroup("editor sytle color",true) {
        @Override
        public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
            return ColorsUtil.colorArr.stream().map(e-> createEditorColorAction(ColorsUtil.colors, e, Node::setEditorStyleColor)).collect(Collectors.toList()).toArray(new AnAction[]{});
        }
    };

    private ActionGroup editorStyle = new ActionGroup("editor style",true) {
        @Override
        public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
            return ColorsUtil.editStyleArr.stream().map(e-> createEditorColorAction(ColorsUtil.editStyle, e, Node::setEditorStyle)).collect(Collectors.toList()).toArray(new AnAction[]{});
        }
    };

    private TreeView treeView;
    private Component component;

    public BookAction(TreeView treeView, Component node) {
        this.treeView = treeView;
        this.component = node;
    }


    public static BookAction listAction(TreeView treeView, Component component) {
        return new BookAction(treeView, component);
    }



    public void show(MouseEvent event) {
        DataContext dataContext = DataManager.getInstance().getDataContext(component);


        ActionGroup actionGroup2 = new ActionGroup() {
            @Override
            public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                TreePath selectionPath = treeView.getTree().getSelectionPath();
                MyTreeNode lastPathComponent = (MyTreeNode) selectionPath.getLastPathComponent();

                if (ProjectLevelVcsManagerImpl.getInstanceEx(anActionEvent.getProject()).checkVcsIsActive("Git")) {
                    if(lastPathComponent instanceof BookNode) {
                        ArrayList<AnAction> actions = new ArrayList<>();
                        if(DGMConstant.LOCKED_.equals(lastPathComponent.node().getLocked())) { //已加锁
                            actions.add(new AnAction(()-> DGMConstant.UNLOCKED) {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e) {
                                    lastPathComponent.unlock();
                                }
                            });
                        } else if (lastPathComponent.node().getLocked() != null) {//已绑定
                            actions.add(new AnAction(()-> DGMConstant.UNBIND) {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e) {
                                    lastPathComponent.unbind();
                                }
                            });
                        } else {//无状态
                            actions.add(new ActionGroup(DGMConstant.BIND,true) {
                                @Override
                                public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                                    GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(anActionEvent.getProject());
                                    GitRepository instance = repositoryManager.getRepositoryForFile(anActionEvent.getProject().getProjectFile());
                                    return instance.getBranches().getLocalBranches()
                                            .stream()
                                            .map(GitReference::getName)
                                            .filter(e -> !e.equals(instance.getCurrentBranch().getName()))
                                            .map(e -> new AnAction(e) {
                                                @Override
                                                public void actionPerformed(@NotNull AnActionEvent e) {
                                                    lastPathComponent.bind(e.getPresentation().getText());
                                                }
                                            }).collect(Collectors.toList()).toArray(new AnAction[]{});
                                }
                            });
                            actions.add(new AnAction(()->DGMConstant.LOCKED) {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e) {
                                    lastPathComponent.lock();
                                }
                            });
                        }
                        actions.add(textColorGroup);
                        actions.add(styleColorGroup);
                        actions.add(styleGroup);
                        actions.add(editorTextColor);
                        actions.add(editorBgColor);
                        actions.add(editorStyle);
                        actions.add(editorSytleColor);
                        return actions.toArray(new AnAction[]{});
                    } else {
                        return new AnAction[] {
                                new ActionGroup(DGMConstant.BIND,true) {
                                    @Override
                                    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                                        GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(anActionEvent.getProject());
                                        GitRepository instance = repositoryManager.getRepositoryForFile(anActionEvent.getProject().getProjectFile());
                                        return instance.getBranches().getLocalBranches()
                                                .stream()
                                                .map(GitReference::getName)
                                                .filter(e -> !e.equals(instance.getCurrentBranch().getName()))
                                                .map(e -> new AnAction(e) {
                                                    @Override
                                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                                        lastPathComponent.bind(e.getPresentation().getText());
                                                    }
                                                }).collect(Collectors.toList()).toArray(new AnAction[]{});
                                    }
                                },
                                new AnAction(()->DGMConstant.UNBIND) {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                        lastPathComponent.unbind();
                                    }
                                },
                                new AnAction(()->DGMConstant.LOCKED) {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                        lastPathComponent.lock();
                                    }
                                },
                                new AnAction(()->DGMConstant.UNLOCKED) {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                        lastPathComponent.unlock();
                                    }
                                },
                                textColorGroup, styleColorGroup, styleGroup,
                                editorTextColor,editorBgColor, editorStyle,editorSytleColor
                        };
                    }

                } else {
                    return new AnAction[] {
                            textColorGroup, styleColorGroup, styleGroup,
                            editorTextColor,editorBgColor, editorStyle,editorSytleColor
                    };
                }
            }
        };

        ListPopup changeColor = JBPopupFactory.getInstance().createActionGroupPopup(null, actionGroup2, dataContext, MNEMONICS, false, new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 100);
        changeColor.setHandleAutoSelectionBeforeShow(true);
        changeColor.setShowSubmenuOnHover(true);
        changeColor.show(new RelativePoint(event));
    }


    public AnAction createColorAction(Map<String, Object> colors,String color, BiFunction<Node, String, Node>  consumer) {

        OverColorIcon colorIcon = null;
        if(colors.get(color) != null) {
            if(colors.get(color) != null && colors.get(color) instanceof Color) {
                colorIcon = new OverColorIcon((Color) colors.get(color));
            }
        }
        return new AnAction(color, color, colorIcon) {

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                TreePath selectionPath = treeView.getTree().getSelectionPath();
                MyTreeNode lastPathComponent = (MyTreeNode) selectionPath.getLastPathComponent();
                if(CheckboxTreeCellRenderer.flow.equals(color)) {
                    consumer.apply(lastPathComponent.node(), CheckboxTreeCellRenderer.flow);
                } else {
                    if(colors.get(color) instanceof Color) {
                        Color c = (Color) colors.get(color);
                        consumer.apply(lastPathComponent.node(), c.getRed()+"," + c.getGreen() + "," + c.getBlue());
                    } else {
                        consumer.apply(lastPathComponent.node(), colors.get(color).toString());
                    }
                }
            }
        };
    }

    public AnAction createEditorColorAction(Map<String, Object> colors,String color, BiFunction<Node, String, Node>  consumer) {

        OverColorIcon colorIcon = null;
        if(colors.get(color) instanceof Color) {
            colorIcon = new OverColorIcon((Color) colors.get(color));
        }
        return new AnAction(color, color, colorIcon) {

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                TreePath selectionPath = treeView.getTree().getSelectionPath();
                MyTreeNode lastPathComponent = (MyTreeNode) selectionPath.getLastPathComponent();
                if(CheckboxTreeCellRenderer.flow.equals(color)) {
                    consumer.apply(lastPathComponent.node(), CheckboxTreeCellRenderer.flow);
                } else {
                    if(colors.get(color) instanceof Color) {
                        Color c = (Color) colors.get(color);
                        consumer.apply(lastPathComponent.node(), c.getRed()+"," + c.getGreen() + "," + c.getBlue());
                    } else {
                        consumer.apply(lastPathComponent.node(), colors.get(color).toString());
                    }
                }
                lastPathComponent.addEditorState();
            }
        };
    }

}
