package com.dgm.ui.util;

import com.dgm.ui.MyTreeNode;
import com.dgm.db.po.Node;
import com.dgm.ui.renderer.CheckboxTreeCellRenderer;

import org.apache.commons.lang3.StringUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/6/25 1:12
 * @description
 */
public class ColorsUtil {

    public static final String BLACK = "黑色";
    public static final String DEF_TYPE = "0";

    public static Map<String, Object> colors = new HashMap<>();
    public static List<String> colorArr = new ArrayList<>();

    public static Map<String, Object> nodeStyle = new HashMap<>();
    public static List<String> nodeStyleArr = new ArrayList<>();

    public static Map<String, Object> editStyle = new HashMap<>();
    public static List<String> editStyleArr = new ArrayList<>();

    static {
        editStyle.put(CheckboxTreeCellRenderer.flow, CheckboxTreeCellRenderer.flow);
        editStyle.put("line underscore", "LINE_UNDERSCORE");
        editStyle.put("wave underscore", "WAVE_UNDERSCORE");
        editStyle.put("boxed", "BOXED");
        editStyle.put("strikeout", "STRIKEOUT");
        editStyle.put("bold line underscore", "BOLD_LINE_UNDERSCORE");
        editStyle.put("bold dotted line", "BOLD_DOTTED_LINE");
        editStyle.put("search match", "SEARCH_MATCH");
        editStyle.put("rounded box", "ROUNDED_BOX");

        editStyleArr.add(CheckboxTreeCellRenderer.flow);
        editStyleArr.add("line underscore");
        editStyleArr.add("wave underscore");
        editStyleArr.add("boxed");
        editStyleArr.add("strikeout");
        editStyleArr.add("bold line underscore");
        editStyleArr.add("bold dotted line");
        editStyleArr.add("search match");
        editStyleArr.add("rounded box");

        nodeStyle.put(CheckboxTreeCellRenderer.flow, CheckboxTreeCellRenderer.flow);
        nodeStyle.put("plain","0");
        nodeStyle.put("bold","1");
        nodeStyle.put("italic","2");
        nodeStyle.put("ask","3");
        nodeStyle.put("strikeout","4");
        nodeStyle.put("waved","8");
        nodeStyle.put("underline","16");
        nodeStyle.put("bold dotted line","32");
        nodeStyle.put("search match","64");
        nodeStyle.put("smaller","128");
        nodeStyle.put("opaque","256");
        nodeStyle.put("clickable","512");
        nodeStyle.put("hovered","1024");
        nodeStyle.put("no border","2048");

        nodeStyleArr.add(CheckboxTreeCellRenderer.flow);
        nodeStyleArr.add("plain");
        nodeStyleArr.add("bold");
        nodeStyleArr.add("italic");
        nodeStyleArr.add("ask");
        nodeStyleArr.add("strikeout");
        nodeStyleArr.add("waved");
        nodeStyleArr.add("underline");
        nodeStyleArr.add("bold dotted line");
        nodeStyleArr.add("search match");
        nodeStyleArr.add("smaller");
        nodeStyleArr.add("opaque");
        nodeStyleArr.add("clickable");
        nodeStyleArr.add("hovered");
        nodeStyleArr.add("no border");

        colorArr.add(CheckboxTreeCellRenderer.flow);
        colorArr.add("金棒");
        colorArr.add("淡金色棒");
        colorArr.add("黑色卡其色");
        colorArr.add("黄褐色");
        colorArr.add("橄榄");
        colorArr.add("黄色");
        colorArr.add("黄绿色");
        colorArr.add("深橄榄绿色");
        colorArr.add("橄榄色单调");
        colorArr.add("草坪绿");
        colorArr.add("图表重用");
        colorArr.add("黄绿色");
        colorArr.add("深绿色");
        colorArr.add("绿色");
        colorArr.add("森林绿");
        colorArr.add("酸橙");
        colorArr.add("柠檬绿");
        colorArr.add("浅绿色");
        colorArr.add("淡绿色");
        colorArr.add("深海绿色");
        colorArr.add("中春绿色");
        colorArr.add("春天绿色");
        colorArr.add("海绿色");
        colorArr.add("中型水上海洋");
        colorArr.add("中海绿色");
        colorArr.add("浅海绿色");
        colorArr.add("深板岩灰色");
        colorArr.add("蓝绿色");
        colorArr.add("深青色");
        colorArr.add("水色");
        colorArr.add("青色");
        colorArr.add("浅青色");
        colorArr.add("深蓝绿色");
        colorArr.add("绿松石");
        colorArr.add("中绿松石色");
        colorArr.add("淡绿色");
        colorArr.add("水上海洋");
        colorArr.add("粉蓝色");
        colorArr.add("学员蓝色");
        colorArr.add("钢蓝");
        colorArr.add("玉米花蓝色");
        colorArr.add("深天蓝色");
        colorArr.add("道奇蓝");
        colorArr.add("浅蓝");
        colorArr.add("天蓝色");
        colorArr.add("浅天蓝色");
        colorArr.add("午夜蓝");
        colorArr.add("海军");
        colorArr.add("深蓝");
        colorArr.add("中蓝");
        colorArr.add("蓝色");
        colorArr.add("宝蓝色");
        colorArr.add("紫罗兰色");
        colorArr.add("靛青");
        colorArr.add("深板岩蓝");
        colorArr.add("板岩蓝");
        colorArr.add("中板岩蓝");
        colorArr.add("中紫色");
        colorArr.add("深洋红色");
        colorArr.add("深紫色");
        colorArr.add("黑兰花");
        colorArr.add("中兰花");
        colorArr.add("紫色");
        colorArr.add("蓟");
        colorArr.add("李子");
        colorArr.add("紫色");
        colorArr.add("洋红色");
        colorArr.add("兰花");
        colorArr.add("中等紫红色");
        colorArr.add("浅紫红色");
        colorArr.add("深粉红色");
        colorArr.add("亮粉色");
        colorArr.add("浅粉红色");
        colorArr.add("粉");
        colorArr.add("仿古白");
        colorArr.add("米色");
        colorArr.add("浓汤");
        colorArr.add("杏仁变白");
        colorArr.add("小麦");
        colorArr.add("玉米丝");
        colorArr.add("柠檬雪纺");
        colorArr.add("浅金黄色");
        colorArr.add("浅黄色");
        colorArr.add("鞍棕色");
        colorArr.add("赭色");
        colorArr.add("巧克力");
        colorArr.add("秘鲁");
        colorArr.add("沙棕色");
        colorArr.add("魁梧的木头");
        colorArr.add("棕褐色");
        colorArr.add("玫瑰红");
        colorArr.add("莫卡辛");
        colorArr.add("纳瓦霍白");
        colorArr.add("桃粉扑");
        colorArr.add("朦胧的玫瑰");
        colorArr.add("薰衣草腮红");
        colorArr.add("麻布");
        colorArr.add("老花边");
        colorArr.add("木瓜鞭");
        colorArr.add("海贝壳");
        colorArr.add("薄荷奶油");
        colorArr.add("板岩灰");
        colorArr.add("浅灰色");
        colorArr.add("浅钢蓝色");
        colorArr.add("薰衣草");
        colorArr.add("花白色");
        colorArr.add("爱丽丝蓝");
        colorArr.add("幽灵白");
        colorArr.add("甘露");
        colorArr.add("象牙");
        colorArr.add("天蓝色");
        colorArr.add("雪");
        colorArr.add("黑色");
        colorArr.add("暗灰色");
        colorArr.add("灰色");
        colorArr.add("深灰色");
        colorArr.add("银");
        colorArr.add("浅灰色");
        colorArr.add("恩斯伯勒");
        colorArr.add("白色的烟");
        colorArr.add("白色");
        colors.put(CheckboxTreeCellRenderer.flow, CheckboxTreeCellRenderer.flow);
        colors.put("金棒",convertColor("218,165,32"));
        colors.put("淡金色棒",convertColor("238,232,170"));
        colors.put("黑色卡其色",convertColor("189,183,107"));
        colors.put("黄褐色",convertColor("240,230,140"));
        colors.put("橄榄",convertColor("128,128,0"));
        colors.put("黄色",convertColor("255,255,0"));
        colors.put("黄绿色",convertColor("154,205,50"));
        colors.put("深橄榄绿色",convertColor("85,107,47"));
        colors.put("橄榄色单调",convertColor("107,142,35"));
        colors.put("草坪绿",convertColor("124,252,0"));
        colors.put("图表重用",convertColor("127,255,0"));
        colors.put("黄绿色",convertColor("173,255,47"));
        colors.put("深绿色",convertColor("0,100,0"));
        colors.put("绿色",convertColor("0,128,0"));
        colors.put("森林绿",convertColor("34,139,34"));
        colors.put("酸橙",convertColor("0,255,0"));
        colors.put("柠檬绿",convertColor("50,205,50"));
        colors.put("浅绿色",convertColor("144,238,144"));
        colors.put("淡绿色",convertColor("152,251,152"));
        colors.put("深海绿色",convertColor("143,188,143"));
        colors.put("中春绿色",convertColor("0,250,154"));
        colors.put("春天绿色",convertColor("0,255,127"));
        colors.put("海绿色",convertColor("46,139,87"));
        colors.put("中型水上海洋",convertColor("102,205,170"));
        colors.put("中海绿色",convertColor("60,179,113"));
        colors.put("浅海绿色",convertColor("32,178,170"));
        colors.put("深板岩灰色",convertColor("47,79,79"));
        colors.put("蓝绿色",convertColor("0,128,128"));
        colors.put("深青色",convertColor("0,139,139"));
        colors.put("水色",convertColor("0,255,255"));
        colors.put("青色",convertColor("0,255,255"));
        colors.put("浅青色",convertColor("224,255,255"));
        colors.put("深蓝绿色",convertColor("0,206,209"));
        colors.put("绿松石",convertColor("64,224,208"));
        colors.put("中绿松石色",convertColor("72,209,204"));
        colors.put("淡绿色",convertColor("175,238,238"));
        colors.put("水上海洋",convertColor("127,255,212"));
        colors.put("粉蓝色",convertColor("176,224,230"));
        colors.put("学员蓝色",convertColor("95,158,160"));
        colors.put("钢蓝",convertColor("70,130,180"));
        colors.put("玉米花蓝色",convertColor("100,149,237"));
        colors.put("深天蓝色",convertColor("0,191,255"));
        colors.put("道奇蓝",convertColor("30,144,255"));
        colors.put("浅蓝",convertColor("173,216,230"));
        colors.put("天蓝色",convertColor("135,206,235"));
        colors.put("浅天蓝色",convertColor("135,206,250"));
        colors.put("午夜蓝",convertColor("25,25,112"));
        colors.put("海军",convertColor("0,0,128"));
        colors.put("深蓝",convertColor("0,0,139"));
        colors.put("中蓝",convertColor("0,0,205"));
        colors.put("蓝色",convertColor("0,0,255"));
        colors.put("宝蓝色",convertColor("65,105,225"));
        colors.put("紫罗兰色",convertColor("138,43,226"));
        colors.put("靛青",convertColor("75,0,130"));
        colors.put("深板岩蓝",convertColor("72,61,139"));
        colors.put("板岩蓝",convertColor("106,90,205"));
        colors.put("中板岩蓝",convertColor("123,104,238"));
        colors.put("中紫色",convertColor("147,112,219"));
        colors.put("深洋红色",convertColor("139,0,139"));
        colors.put("深紫色",convertColor("148,0,211"));
        colors.put("黑兰花",convertColor("153,50,204"));
        colors.put("中兰花",convertColor("186,85,211"));
        colors.put("紫色",convertColor("128,0,128"));
        colors.put("蓟",convertColor("216,191,216"));
        colors.put("李子",convertColor("221,160,221"));
        colors.put("紫色",convertColor("238,130,238"));
        colors.put("洋红色",convertColor("255,0,255"));
        colors.put("兰花",convertColor("218,112,214"));
        colors.put("中等紫红色",convertColor("199,21,133"));
        colors.put("浅紫红色",convertColor("219,112,147"));
        colors.put("深粉红色",convertColor("255,20,147"));
        colors.put("亮粉色",convertColor("255,105,180"));
        colors.put("浅粉红色",convertColor("255,182,193"));
        colors.put("粉",convertColor("255,192,203"));
        colors.put("仿古白",convertColor("250,235,215"));
        colors.put("米色",convertColor("245,245,220"));
        colors.put("浓汤",convertColor("255,228,196"));
        colors.put("杏仁变白",convertColor("255,235,205"));
        colors.put("小麦",convertColor("245,222,179"));
        colors.put("玉米丝",convertColor("255,248,220"));
        colors.put("柠檬雪纺",convertColor("255,250,205"));
        colors.put("浅金黄色",convertColor("250,250,210"));
        colors.put("浅黄色",convertColor("255,255,224"));
        colors.put("鞍棕色",convertColor("139,69,19"));
        colors.put("赭色",convertColor("160,82,45"));
        colors.put("巧克力",convertColor("210,105,30"));
        colors.put("秘鲁",convertColor("205,133,63"));
        colors.put("沙棕色",convertColor("244,164,96"));
        colors.put("魁梧的木头",convertColor("222,184,135"));
        colors.put("棕褐色",convertColor("210,180,140"));
        colors.put("玫瑰红",convertColor("188,143,143"));
        colors.put("莫卡辛",convertColor("255,228,181"));
        colors.put("纳瓦霍白",convertColor("255,222,173"));
        colors.put("桃粉扑",convertColor("255,218,185"));
        colors.put("朦胧的玫瑰",convertColor("255,228,225"));
        colors.put("薰衣草腮红",convertColor("255,240,245"));
        colors.put("麻布",convertColor("250,240,230"));
        colors.put("老花边",convertColor("253,245,230"));
        colors.put("木瓜鞭",convertColor("255,239,213"));
        colors.put("海贝壳",convertColor("255,245,238"));
        colors.put("薄荷奶油",convertColor("245,255,250"));
        colors.put("板岩灰",convertColor("112,128,144"));
        colors.put("浅灰色",convertColor("119,136,153"));
        colors.put("浅钢蓝色",convertColor("176,196,222"));
        colors.put("薰衣草",convertColor("230,230,250"));
        colors.put("花白色",convertColor("255,250,240"));
        colors.put("爱丽丝蓝",convertColor("240,248,255"));
        colors.put("幽灵白",convertColor("248,248,255"));
        colors.put("甘露",convertColor("240,255,240"));
        colors.put("象牙",convertColor("255,255,240"));
        colors.put("天蓝色",convertColor("240,255,255"));
        colors.put("雪",convertColor("255,250,250"));
        colors.put("黑色",convertColor("0,0,0"));
        colors.put("暗灰色",convertColor("105,105,105"));
        colors.put("灰色",convertColor("128,128,128"));
        colors.put("深灰色",convertColor("169,169,169"));
        colors.put("银",convertColor("192,192,192"));
        colors.put("浅灰色",convertColor("211,211,211"));
        colors.put("恩斯伯勒",convertColor("220,220,220"));
        colors.put("白色的烟",convertColor("245,245,245"));
        colors.put("白色",convertColor("255,255,255"));
    }

    public static String getStyle(DefaultMutableTreeNode node, Function<Node, String> fuc, String def) {
        String color = fuc.apply(((MyTreeNode) node).node());

        if (node.getParent() instanceof MyTreeNode) {
            if (CheckboxTreeCellRenderer.flow.equals(color)) {
                return getStyle((DefaultMutableTreeNode) node.getParent(), fuc, def);
            } else {
                if (StringUtils.isEmpty(color)) {
                    return def;
                }
                return color;
            }
        } else {
            if (CheckboxTreeCellRenderer.flow.equals(color)) {
                return def;
            } else {
                if (StringUtils.isEmpty(color)) {
                    return def;
                }
                return color;
            }
        }
    }

    public static Color getColor(DefaultMutableTreeNode node, Function<Node, String> fuc, String def) {
        String color = fuc.apply(((MyTreeNode) node).node());

        if (node.getParent() instanceof MyTreeNode) {
            if (CheckboxTreeCellRenderer.flow.equals(color)) {
                return getColor((DefaultMutableTreeNode) node.getParent(), fuc, def);
            } else {
                if (StringUtils.isEmpty(color)) {
                    return convertColor(def);
                }
                return convertColor(color);
            }
        } else {
            if (CheckboxTreeCellRenderer.flow.equals(color)) {
                return convertColor(def);
            } else {
                if (StringUtils.isEmpty(color)) {
                    return convertColor(def);
                }
                return convertColor(color);
            }
        }
    }


    private static Color convertColor(String color){
        if (CheckboxTreeCellRenderer.flow.equals(color)) {
            return null;
        } else {
            String[] split = color.split(",");
            return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        }
    }

    public static String convertColorRGB(String color){
        Color c = (Color) colors.get(color);
        return c.getRed()+"," + c.getGreen() + "," + c.getBlue();
    }
}
