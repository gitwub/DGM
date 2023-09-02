package com.dgm.db;

import com.dgm.ApplicationContext;
import com.dgm.DGMConstant;
import com.dgm.ui.FolderNode;
import com.dgm.ui.LogUtils;
import com.dgm.ui.MyTreeNode;
import com.dgm.db.po.Node;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import java.awt.print.Book;
import java.io.File;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public abstract class DBUtils implements Disposable {
    public SQLiteConnectionPoolDataSource connect = null;
    protected static final String TABLE_NAME_PRE = "dgm";
    public final Project app;
    private static boolean showDBPath = false;
    protected DefaultMutableTreeNode root;

    private AtomicInteger id = new AtomicInteger();

    public DBUtils(Project app) {
        this.app = app;
    }

    public void init() throws SQLException {
        PropertiesComponent instance = PropertiesComponent.getInstance(app);
        String value = instance.getValue("com.dmg.root.path");
        File file = null;
        if (value == null || "".equals(value)) {
            file = new File(app.getBasePath() + "/" + DGMConstant.DMG_ROOT_PATH);
        } else {
            file = new File(DGMConstant.DMG_ROOT_PATH);
        }
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        connect = new SQLiteConnectionPoolDataSource();
        connect.setUrl("jdbc:sqlite:" + file.getPath());
        connect.setEncoding("utf-8");
        connect.setLogWriter(new PrintWriter(System.out));


//        LogUtils.initDB(app, "connect : %s", connect.hashCode()+"");
        if(!tableExists()) {
            if (TABLE_NAME_PRE.equals(getTableName())) {
                connect.getConnection().createStatement().execute("create table dgm ( id integer primary key autoincrement, name varchar(10) not null unique, active integer default 0, sort_index integer not null);");
            } else {
                connect.getConnection().createStatement().execute
                        (
                        "create table " + getTableName() + "(\n" +
                                "    `id` integer primary key,\n" +//主键
                                "    `sort_index` integer not null,\n" +//排序从零开始连续且不重复
                                "    `parent_id` integer not null,\n" +//父节点
                                "    `node_name` text null,\n" +//显示名字
                                "    `protocol` text,\n" +//协议 file jar
                                "    `color` text not null default 'black',\n" +//颜色 文本或者rgb逗号分割
                                "    `wave_color` text not null default 'black',\n" +//风格颜色
                                "    `text_style` integer not null default '0',\n" +//文本颜色
                                "    `level` integer not null default 1,\n" +//树级别 从一开始连续的
                                "    `dgm_type` integer not null,\n" +// 0.叶子节点 1.组节点
                                "    `checked` integer default 0,\n" +// 1选中 0未选中
                                "    `editor_text_color` text default null,\n" +// 编辑区文本颜色
                                "    `editor_bg_color` text default null,\n" +//编辑区背景颜色
                                "    `editor_style_color` text default null,\n" +//编辑区风格颜色
                                "    `editor_style` text default null,\n" +//编辑区风格
                                "    `lock_at` text default null,\n" +// 1选中 0未选中
                                "    `file_path` text,\n" +//文件路径
                                "    `jar_name` text,\n" +//jar名字
                                "    `origin_text` text not null,\n" +//原生文本
                                "    `line_number` integer,\n" +//行
                                "    `column` integer,\n" +//列
                                "    `line_end_offset` integer,\n" +//光标节点偏移
                                "    `location` integer,\n" +//0 out绝对路径 1 inner项目相对路径
                                "    `state` integer default 1\n" +//0.收缩 1.展开`
                                ");"
                );
            }
        }

        if (!showDBPath) {
            Notification notification = new Notification("debug db path", AllIcons.General.Warning, NotificationType.INFORMATION);
            notification.setTitle("debug group manage DB path");
            notification.setContent(file.getAbsolutePath());
            notification.setImportant(false);
            Notifications.Bus.notify(notification);
            notification.hideBalloon();
            showDBPath = true;
        }
        id.set(maxId());
    }

    public boolean tableExists(){
        boolean res = false;
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = connect.getConnection().createStatement();
            resultSet = statement.executeQuery("select * from sqlite_master where type = 'table' and name = '" + getTableName() + "';");
            if (resultSet.next()) {
                res = true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return res;
    }

    public int countByTable(String tableName) {
        int count = 0;
        ResultSet resultSet = null;
        try {
            resultSet = connect.getConnection().createStatement().executeQuery("select count(1) as count from " + tableName);
            if (resultSet.next()) {
                count = resultSet.getInt("count");
                resultSet.close();
            } else {
                return count;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return count;
    }

    public int maxId() {
        int count = 0;
        ResultSet resultSet = null;
        try {
            resultSet = connect.getConnection().createStatement().executeQuery("select max(id) as count from " + getTableName());
            if (resultSet.next()) {
                count = resultSet.getInt("count") + 1;
                resultSet.close();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return count;
    }

    @Override
    public void dispose() {
        connect = null;
    }

    public abstract String getTableName();

    public void saveToDB(JTree tree) {
        try {
            connect.getConnection().createStatement().execute("DELETE FROM " + getTableName() + ";");
            buildNode(tree, root.children());

//            LogUtils.saveDB(app, "connect : %s", connect.hashCode()+"");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void buildNode(JTree tree, Enumeration<TreeNode> children) throws SQLException {

        int index = 0;
        while (children.hasMoreElements()) {
            MyTreeNode treeNode = (MyTreeNode) children.nextElement();

            Node node = treeNode.node();
            node.setNodeName(treeNode.getUserObject().toString());
            node.setSortIndex(index++);

            if (treeNode.getParent() instanceof FolderNode) {
                node.setParentId(((FolderNode) treeNode.getParent()).node().getId());
            } else if (treeNode.getParent() instanceof DefaultMutableTreeNode) {
                node.setParentId(DGMConstant.ROOT);
            }
            if (node.getDgmType() == DGMConstant.NODE_GROUP) {
                node.setState(tree.isExpanded(new TreePath(treeNode.getPath())) ? DGMConstant.NODE_EXPANDED : DGMConstant.NODE_COLLAPSED);
                buildNode(tree, treeNode.children());
            }


            insertNode(node);
        }
    }

    public abstract void insertNode(Node node) throws SQLException;

    public String getNodeId() {
        return String.valueOf(id.incrementAndGet());
    };
}
