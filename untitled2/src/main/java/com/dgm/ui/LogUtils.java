package com.dgm.ui;

import com.dgm.ApplicationContext;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/7/29 23:26
 * @description
 */
public class LogUtils {


    public static void newNode(ApplicationContext context, String str, String ... pars){
        String format = String.format("新增节点：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }
    public static void renameNode(ApplicationContext context, String str, String ... pars){
        String format = String.format("修改节点：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }
    public static void delNode(ApplicationContext context, String str, String ... pars){
        String format = String.format("删除节点：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }

    public static void nodeUp(ApplicationContext context, String str, String ... pars){
        String format = String.format("节点上移：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }
    public static void nodeDown(ApplicationContext context, String str, String ... pars){
        String format = String.format("节点下移：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }
    public static void newTab(ApplicationContext context, String str, String ... pars){
        String format = String.format("新增tab：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }
    public static void delTab(ApplicationContext context, String str, String ... pars){
        String format = String.format("删除tab：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }
    public static void tabLeft(ApplicationContext context, String str, String ... pars){
        String format = String.format("tab左移：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }
    public static void tabRight(ApplicationContext context, String str, String ... pars){
        String format = String.format("tab右移：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }

    public static void newFolder(ApplicationContext context, String str, String ... pars){
        String format = String.format("新文件夹：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }
    public static void app(ApplicationContext context){
        String format = String.format("打开项目：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode());
        appendUtf8Lines(format);
    }

    public static void initDB(ApplicationContext context,String str, String ...pars){
        String format = String.format("打开db：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode())  + String.format(str, pars);
        appendUtf8Lines(format);
    }

    public static void saveDB(ApplicationContext context,String str, String ...pars){
        String format = String.format("保存db：app %s pro hash %s, win hash %s,", context.getProject().getName(), context.getProject().hashCode(),context.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }

    public static void saveNode(ApplicationContext app, String str, String ...pars) {
        String format = String.format("保存node：app %s pro hash %s, win hash %s,", app.getProject().getName(), app.getProject().hashCode(),app.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }

    public static void initNodeMapper(ApplicationContext app, String str, String ...pars) {
        String format = String.format("initNodeMapper：app %s pro hash %s, win hash %s,", app.getProject().getName(), app.getProject().hashCode(),app.getToolWindow().hashCode()) + String.format(str, pars);
        appendUtf8Lines(format);
    }

    private static void appendUtf8Lines(String str){
        BufferedWriter bufferedWriter = null;
        FileOutputStream out1 = null;
        OutputStreamWriter out = null;
        try {
            Charset cs = StandardCharsets.UTF_8;
            out1 = new FileOutputStream("D://log.txt", true);
            out = new OutputStreamWriter(out1, cs);
            bufferedWriter = new BufferedWriter(out);

            bufferedWriter.write("\n"+str);
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedWriter.close();
                out.close();
                out1.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void close(String name,String treeName) {
        String format = String.format("close：app %s ,treeName %s", name, treeName);
//        appendUtf8Lines(format);
    }
}
