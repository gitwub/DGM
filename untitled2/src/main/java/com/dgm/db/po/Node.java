package com.dgm.db.po;

import com.dgm.ui.renderer.CheckboxTreeCellRenderer;
import com.dgm.DGMConstant;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public Node() {
    }

    public Node(String id, String nodeName) {
        this.id = id;
        this.nodeName = nodeName;
    }

    /**
     * 主键
     */
    private String id;
    /**
     * 主键
     */
    private String parentId;
    /**
     * 显示名称
     */
    private String nodeName;
    /**
     * 协议
     */
    private String protocol;
    /**
     * 排序
     */
    private int sortIndex;
    /**
     * 颜色
     */
    private String color = CheckboxTreeCellRenderer.flow;
    /**
     * 树级别
     */
    private int level;
    /**
     * 0.文件类型
     * 1.jar包类型
     */
    private String fileSystem;
    /**
     * 节点类型
     * 0.叶子节点
     * 1.组节点
     */
    private int dgmType;

    /**
     * 节点类型
     * 0.收缩
     * 1.展开
     */
    private int state;

    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 是否是jar
     */
    private String jarName;
    /**
     * 图标路径
     */
    private String iconPath;
    /**
     * 加入时的文本
     */
    private String originText;
    /**
     * 是否选择
     */
    private boolean checked;
    /**
     * 行
     */
    private int lineNumber;
    /**
     * 列
     */
    private int column;
    /**
     * 光标偏移
     */
    private int lineEndOffset;
    /**
     * 0 out
     * 1 inner
     */
    private int location;
    /**
     * 风格颜色
     */
    private String waveColor = CheckboxTreeCellRenderer.flow;
    /**
     * 文本风格
     */
    private String textStyle = CheckboxTreeCellRenderer.flow;

    private String editorTextColor = CheckboxTreeCellRenderer.flow;
    private String editorBgColor = CheckboxTreeCellRenderer.flow;
    private String editorStyleColor = CheckboxTreeCellRenderer.flow;
    private String editorStyle = CheckboxTreeCellRenderer.flow;
    private String locked;

    public String getId() {
        return id;
    }

    public Node setId(String id) {
        this.id = id;
        return this;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Node setNodeName(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public Node setProtocol(String protocol) {
        if(dgmType == DGMConstant.NODE_LEAF && protocol == null) {
            throw new RuntimeException("叶子节点protocol不能为空");
        }
        this.protocol = protocol;
        return this;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public Node setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
        return this;
    }

    public String getColor() {
        return color;
    }

    public Node setColor(String color) {
        this.color = color;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public Node setLevel(int level) {
        this.level = level;
        return this;
    }

    public String getFileSystem() {
        return fileSystem;
    }

    public Node setFileSystem(String fileSystem) {
        this.fileSystem = fileSystem;
        return this;
    }

    public int getDgmType() {
        return dgmType;
    }

    public Node setDgmType(int dgmType) {
        this.dgmType = dgmType;
        return this;
    }

    public int getState() {
        return state;
    }

    public Node setState(int state) {
        this.state = state;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public Node setFilePath(String filePath) {
        if(dgmType == DGMConstant.NODE_LEAF && filePath == null) {
            throw new RuntimeException("叶子节点filePath不能为空");
        }
        this.filePath = filePath;
        return this;
    }

    public String getJarName() {
        return jarName;
    }

    public Node setJarName(String jarName) {
        this.jarName = jarName;
        return this;
    }

    public String getOriginText() {
        return originText;
    }

    public Node setOriginText(String originText) {
        this.originText = originText;
        return this;
    }

    public boolean isChecked() {
        return checked;
    }

    public Node setChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public Node setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }

    public int getColumn() {
        return column;
    }

    public Node setColumn(int column) {
        this.column = column;
        return this;
    }

    public int getLineEndOffset() {
        return lineEndOffset;
    }

    public Node setLineEndOffset(int lineEndOffset) {
        this.lineEndOffset = lineEndOffset;
        return this;
    }

    public int getLocation() {
        return location;
    }

    public Node setLocation(int location) {
        this.location = location;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public Node setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getWaveColor() {
        return waveColor;
    }

    public Node setWaveColor(String waveColor) {
        this.waveColor = waveColor;
        return this;
    }

    public String getTextStyle() {
        return textStyle;
    }

    public Node setTextStyle(String textStyle) {
        this.textStyle = textStyle;
        return this;
    }

    private List<Node> childrenNodes = new ArrayList<>();

    public List<Node> getChildrenNodes() {
        return childrenNodes;
    }

    public Node setChildrenNodes(List<Node> childrenNodes) {
        this.childrenNodes = childrenNodes;
        return this;
    }

    private Node parentNode;

    public Node getParentNode() {
        return parentNode;
    }

    public Node setParentNode(Node parentNode) {
        this.parentNode = parentNode;
        return this;
    }


    public String getEditorTextColor() {
        return editorTextColor;
    }

    public Node setEditorTextColor(String editorTextColor) {
        this.editorTextColor = editorTextColor;
        return this;
    }

    public String getEditorBgColor() {
        return editorBgColor;
    }

    public Node setEditorBgColor(String editorBgColor) {
        this.editorBgColor = editorBgColor;
        return this;
    }

    public String getEditorStyleColor() {
        return editorStyleColor;
    }

    public Node setEditorStyleColor(String editorStyleColor) {
        this.editorStyleColor = editorStyleColor;
        return this;
    }

    public String getEditorStyle() {
        return editorStyle;
    }

    public Node setEditorStyle(String editorStyle) {
        this.editorStyle = editorStyle;
        return this;
    }

    public String getLocked() {
        return locked;
    }

    public Node setLocked(String locked) {
        this.locked = locked;
        return this;
    }
}
