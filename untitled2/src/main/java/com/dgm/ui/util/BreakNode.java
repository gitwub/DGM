package com.dgm.ui.util;

import com.dgm.ui.BookNode;
import com.intellij.openapi.util.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * @author 王银飞
 * @email 371964363@qq.com
 * @date 2023/5/7 20:18
 * @description
 */
public class BreakNode extends ConcurrentHashMap<String, List<BookNode>> {
    public static Key<BreakNode> KEY = new Key<>(BreakNode.class.getName());
    public void add(BookNode book) {
        if (!containsKey(book.getJavaBreakKey())) {
            put(book.getJavaBreakKey(),new CopyOnWriteArrayList<>());
        }
        List<BookNode> bookNodes = get(book.getJavaBreakKey());
        bookNodes.add(book);
    }

    public void remove(BookNode o, Supplier consumer){
        if (!containsKey(o.getJavaBreakKey())) {
            put(o.getJavaBreakKey(),new ArrayList<>());
        }
        List<BookNode> bookNodes = get(o.getJavaBreakKey());
        bookNodes.remove(o);
        consumer.get();
        if(bookNodes.isEmpty()) {
            remove(o.getJavaBreakKey());
        }
    }

    public void checked(String filePath, boolean checked) {
        List<BookNode> bookNodes = get(filePath);
        if(bookNodes != null) {
            bookNodes.forEach(e->{
                e.checked(checked);
            });
        }
    }

}
