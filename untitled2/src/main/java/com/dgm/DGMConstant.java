package com.dgm;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/5/7 21:13
 * @description
 */
public class DGMConstant {
    public static String DMG_ROOT_PATH = "root-dgm.db";
    public static String SYSTEM_FILE = "file";
    public static String STATE = "state";
    public static String EXPANDED = "expanded";
    public static String FILE_PATH = "filePath";
    public static String LOCATION = "location";
    public static int LOCATION_INNER = 1;
    public static int LOCATION_OUT = 0;
    public static String ICON_PATH = "iconPath";
    public static String LINE_NUMBER = "lineNumber";
    public static String COLUMN = "column";
    public static String LINE_END_OFFSET = "lineEndOffset";
    public static String PROTOCOL = "protocol";
    public static String NEW_FOLDER = "NewFolder";
    public static String ROOT = "root";
    public static String COLLAPSED = "collapsed";

    public static String NAME = "name";;
    public static String JAR = "jar";;
    public static String JRT = "jrt";;
    public static String JAR_NAME = "jarName";;
    public static String ACTIVE = "active";

    public static String CHECKED = "checked";
    public static int NODE_LEAF = 0;
    public static int NODE_GROUP = 1;
    public static int NODE_EXPANDED = NODE_GROUP;
    public static int NODE_COLLAPSED = NODE_LEAF;
    public static boolean NODE_CHECKED_ON = true;
    public static boolean NODE_CHECKED_NO = false;
    public static int NODE_LOCATION_OUT = NODE_LEAF;
    public static int NODE_LOCATION_INNER = NODE_GROUP;

    public static class Bookmark{
        public static String NAME = "bookmark";

        public static String ORIGIN_TEXT = "originText";
    }

    public static class BookGroup{
        public static String NAME = "group";
    }

}
