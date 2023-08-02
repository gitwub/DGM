package com.dgm.db;

import com.dgm.ApplicationContext;
import com.dgm.ui.LogUtils;
import com.dgm.ui.TreeView;
import com.dgm.db.po.Node;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.util.Key;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerListener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dgm.DGMToolWindow.keyLis;

public class TabMapper extends DBUtils{
    public static Key<TabMapper> key = new Key(TabMapper.class.getName());
    private Map<String, TreeView> tabs = new HashMap<>();
    public TabMapper(ApplicationContext app) {
        super(app);
        try {
            init();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            Notification notification = new Notification("group", AllIcons.General.Error, NotificationType.ERROR);
            notification.setContent("db:" + getTableName() +"初始化失败！！");
            Notifications.Bus.notify(notification);
        }
    }

    public void add(String name) {
        int dgmCount = countByTable("dgm");
        try {
            connect.getConnection().createStatement().execute("update dgm set active = '0'");
            connect.getConnection().createStatement().execute("insert INTO dgm (name, active, sort_index) VALUES ('" + name + "', 1, " + dgmCount + ");");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        refresh();
    }

    public void refresh() {
        List<Node> nodes = new ArrayList<>();
        try {
            ResultSet resultSet = connect.getConnection().createStatement().executeQuery("select * from dgm order by sort_index;");
            while (resultSet.next()) {
                Node node = new Node();
                node.setNodeName(resultSet.getString("name"));
                node.setChecked(resultSet.getBoolean("active"));
                nodes.add(node);
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        ContentManagerListener managerListener = app.getProject().getUserData(keyLis);
        app.getToolWindow().getContentManager().removeContentManagerListener(managerListener);
        app.getToolWindow().getContentManager().removeAllContents(false);
        app.getProject().putUserData(TreeView.key, null);
        for (int i = 0; i < nodes.size(); i++) {
            ContentFactory instance = ContentFactory.SERVICE.getInstance();
            String name = nodes.get(i).getNodeName();
            if(!tabs.containsKey(name)) {
                TreeView treeView = new TreeView(app, name);
                tabs.put(name, treeView);
            }
            TreeView jComponent = tabs.get(name);
            Content content = instance.createContent(jComponent, name, true);

            app.getToolWindow().getContentManager().addContent(content);
            if (nodes.get(i).isChecked()) {
                app.getToolWindow().getContentManager().setSelectedContent(content);
                app.getToolWindow().getContentManager().requestFocus(content, true);
                app.getProject().putUserData(TreeView.key, jComponent);
            }
        }
        app.getToolWindow().getContentManager().addContentManagerListener(managerListener);
    }

    public void up() {
        if(canUp()) {
            try {
                connect.getConnection().createStatement().execute("update dgm set sort_index = sort_index + 1 where sort_index = (select sort_index - 1 from dgm where active = '1');");
                connect.getConnection().createStatement().execute("update dgm set sort_index = sort_index - 1 where active = '1';");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            refresh();
        }
    }

    public void down() {
        if(canDown()) {
            try {
                connect.getConnection().createStatement().execute("update dgm set sort_index = sort_index - 1 where sort_index = (select sort_index + 1 from dgm where active = '1');");
                connect.getConnection().createStatement().execute("update dgm set sort_index = sort_index + 1 where active = '1';");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            refresh();
        }
    }

    private boolean canUp() {
        boolean res = false;
        Statement statement = null;
        try {
            statement = connect.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select name from dgm where active = '1' and sort_index ='0';");
            if (!resultSet.next()) {
                res = true;
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
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

    private boolean canDown() {
        boolean res = false;
        Statement statement = null;
        try {
            statement = connect.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select name from dgm where active = '1' and sort_index = (select max(sort_index) from dgm);");
            if (!resultSet.next()) {
                res = true;
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
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

    public void delete() {
        try {
            connect.getConnection().createStatement().executeUpdate("drop table " + TABLE_NAME_PRE + "_" + currentActiveName() + ";");
            connect.getConnection().createStatement().executeUpdate("update dgm set sort_index = sort_index - 1 where sort_index > (select sort_index from dgm where active = '1');");
            connect.getConnection().createStatement().executeUpdate("delete from dgm where active = '1';");
            connect.getConnection().createStatement().executeUpdate("update dgm set active = '1' where sort_index = '0';");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        TreeView component = (TreeView) app.getToolWindow().getContentManager().getSelectedContent().getComponent();
        if(component != null) {
            component.delete();
            tabs.remove(component.getTreeViewName());
            LogUtils.delTab(app,"tab name %s", component.getTreeViewName());
        }
        refresh();
    }

    public boolean contains(String inputString){
        boolean res = false;
        Statement statement = null;
        try {
            statement = connect.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select * from dgm where name = '"  + inputString + "'");
            if (resultSet.next()) {
                res = true;
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
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


    public void active(int index) {
        try {
            connect.getConnection().createStatement().executeUpdate("update dgm set active = '0' where active = '1';");
            connect.getConnection().createStatement().executeUpdate("update dgm set active = '1' where sort_index = '" + index + "';");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public String getTableName() {
        return TABLE_NAME_PRE;
    }

    @Override
    public void insertNode(Node node) throws SQLException {

    }

    public void activeName(String treeViewName) {
        try {
            connect.getConnection().createStatement().executeUpdate("update dgm set active = '0';");
            connect.getConnection().createStatement().executeUpdate("update dgm set active = '1' where name = '" + treeViewName + "';");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        refresh();
    }

    public String currentActiveName() {
        String name = "";
        Statement statement = null;
        try {
            statement = connect.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("select name from dgm where active = '1';");
            if (resultSet.next()) {
                name = resultSet.getString("name");
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return name;
    }
}
