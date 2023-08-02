package com.dgm.db;

import com.dgm.ApplicationContext;
import com.dgm.DGMConstant;
import com.dgm.ui.BookNode;
import com.dgm.ui.LogUtils;
import com.dgm.ui.util.BreakNode;
import com.dgm.ui.FolderNode;
import com.dgm.ui.MyTreeNode;
import com.dgm.db.po.Node;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.util.Key;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class NodeMapper extends DBUtils {

    private static final String fullSelectId = "`id`";
    private static final String fullSelectFull = "`sort_index`,`parent_id`, `node_name`, `protocol`, `color`, `wave_color`, `text_style`, `level`, `dgm_type`, `checked`, `file_path`, `jar_name`, `icon_path`, `origin_text`, `line_number`, `column`, `line_end_offset`, `location`, `state`";
    private static final String fullSelectSql = fullSelectId + ", " + fullSelectFull;

    public static Key<NodeMapper> key = new Key(NodeMapper.class.getName());
    private String tableName;
    private List<TreePath> treePaths = new ArrayList<>();

    public NodeMapper(ApplicationContext app, DefaultMutableTreeNode root, String tableName) {
        super(app);
        this.root = root;
        this.tableName = tableName;
        try {
            LogUtils.initNodeMapper(app, "tableName %s", tableName);
            init();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            Notification notification = new Notification("group", AllIcons.General.Error, NotificationType.ERROR);
            notification.setContent("db:" + getTableName() +"初始化失败！！");
            Notifications.Bus.notify(notification);
        }
    }

    @Override
    public String getTableName() {
        return TABLE_NAME_PRE + "_" + tableName;
    }

    /**
     * 分页查询
     *
     * @param root
     * @return 节点列表
     */
    public void listByRoot(DefaultMutableTreeNode root) {
        String parentId = null;
        if (root instanceof MyTreeNode) {
            parentId = ((MyTreeNode) root).getId();
        } else {
            parentId = DGMConstant.ROOT;
        }

        Statement statement = null;
        try {
            statement = connect.getConnection().createStatement();

            ResultSet resultSet = statement.executeQuery("select " + fullSelectSql + " from " + getTableName() + " where parent_id = '" + parentId + "' order by sort_index;");

            while (resultSet.next()) {
                Node e = new Node();
                createNode(resultSet, e);

                if (e.getDgmType() == DGMConstant.NODE_GROUP) {
                    FolderNode folderNode = new FolderNode(app, e);
                    root.add(folderNode);
                    listByRoot(folderNode);
                    if(e.getState() == DGMConstant.NODE_EXPANDED) {
                        treePaths.add(new TreePath(folderNode.getPath()));
                    }
                } else {
                    BookNode bookNode = new BookNode(app, tableName, e);
                    root.add(bookNode);
                    app.getProject().getUserData(BreakNode.KEY).add(bookNode);
                }
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
    }
    /**
     * 分页查询
     *
     * @param level
     * @return 节点列表
     */
    public List<Node> listByFilter(String id, int level, String filterStr) {
        List<Node> nodes = new ArrayList<>();
        Statement statement = null;
        try {
            statement = connect.getConnection().createStatement();

            ResultSet resultSet = null;
            if ("".equals(id)) {
                resultSet = statement.executeQuery("select " + fullSelectSql + " from " + getTableName() + " where node_name like '%" + filterStr + "%' and level = '1' order by sort_index;");
            } else {
                resultSet = statement.executeQuery("select " + fullSelectSql + " from " + getTableName() + " where node_name like '%" + filterStr + "%' and id = '" + id + "' and level = '" + level + "' order by sort_index;");
            }


            while (resultSet.next()) {
                Node e = new Node();
                createNode(resultSet, e);
                nodes.add(e);
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
        return nodes;
    }

    private void createNode(ResultSet resultSet, Node e) throws SQLException {
        e.setDgmType(resultSet.getInt("dgm_type"));
        e.setId(resultSet.getString("id"));
        e.setSortIndex(resultSet.getInt("sort_index"));
        e.setParentId(resultSet.getString("parent_id"));
        e.setNodeName(resultSet.getString("node_name"));
        e.setProtocol(resultSet.getString("protocol"));
        e.setColor(resultSet.getString("color"));
        e.setWaveColor(resultSet.getString("wave_color"));
        e.setTextStyle(resultSet.getString("text_style"));
        e.setLevel(resultSet.getInt("level"));
        e.setChecked(resultSet.getBoolean("checked"));
        e.setFilePath(resultSet.getString("file_path"));
        e.setJarName(resultSet.getString("jar_name"));
        e.setIconPath(resultSet.getString("icon_path"));
        e.setOriginText(resultSet.getString("origin_text"));
        e.setLineNumber(resultSet.getInt("line_number"));
        e.setColumn(resultSet.getInt("column"));
        e.setLineEndOffset(resultSet.getInt("line_end_offset"));
        e.setLocation(resultSet.getInt("location"));
        e.setState(resultSet.getInt("state"));
    }

    public List<Node> listByFilter(String filterStr) {
        return listByFilter("", 1, filterStr);
    }

    public void buildTrees() {
        listByRoot(root);
    }


    @Override
    public void insertNode(Node node) throws SQLException {
        connect.getConnection().createStatement().execute("" +
                "insert INTO " + getTableName() + " (" + fullSelectSql + ")\n" +
                "values (" +
                "'" + node.getId() + "'," +
                (node.getSortIndex() == -1 ? "(select count(1) from " + getTableName() + " where level = '" + node.getLevel() + "' and parent_id = '" +  node.getParentId() + "')," : "'"+node.getSortIndex()+"',") +
                "'" + node.getParentId() + "'," +
                "'" + node.getNodeName() + "'," +
                "'" + node.getProtocol() + "'," +
                "'" + node.getColor() + "'," +
                "'" + node.getWaveColor() + "'," +
                "'" + node.getTextStyle() + "'," +
                "'" + node.getLevel() + "'," +
                "'" + node.getDgmType() + "'," +
                "'" + (node.isChecked() ? "1":"0") + "'," +
                "'" + node.getFilePath() + "'," +
                "'" + node.getJarName() + "'," +
                "'" + node.getIconPath() + "'," +
                "'" + node.getOriginText() + "'," +
                "'" + node.getLineNumber() + "'," +
                "'" + node.getColumn() + "'," +
                "'" + node.getLineEndOffset() + "'," +
                "'" + node.getLocation() + "'," +
                "'" + node.getState() + "'" +
                ");"
        );
        LogUtils.saveNode(app, "node name %s", node.getNodeName());
    }

    public List<TreePath> getExpandTreePaths() {
        return treePaths;
    }
}
