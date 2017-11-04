package org.humanityx.util;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * Cache web pages on disk.
 * The md5 of the url is used for determining the subdir and filename.
 * Created by arvid on 18-9-15.
 */
public class PageCache {

    private File baseDir;
    private int chars = 2;
    private int levels = 2;

    public PageCache(File baseDir) {
        this.baseDir = baseDir;
    }

    public PageCache(File baseDir, int chars, int levels) {
        this.baseDir = baseDir;
        this.chars = chars;
        this.levels = levels;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public PageCache setBaseDir(File baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    private File getFile(String url, boolean makeDirs){
        return getFileByMd5(Hash.md5(url), makeDirs);
    }

    private File getFileByMd5(String md5, boolean makeDirs){
        File dir = baseDir;
        if(levels * chars <= md5.length()) {
            for (int i = 0; i < levels; i++) {
                dir = new File(dir, md5.substring(i * chars, i * chars + chars));
            }
        }
        if(makeDirs){
            dir.mkdirs();
        }
        return new File(dir, md5);
    }

    public boolean contains(String url){
        return getFile(url, false).exists();
    }

    public void set(String url, String content) throws IOException {
        File file = getFile(url, true);
        Files.write(content, file, Charsets.UTF_8);
    }

    public String get(String url)  {
        File file = getFile(url, false);
        if(file.exists()){
            try {
                return Files.toString(file, Charsets.UTF_8);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public String getByMd5(String md5) {
        File file = getFileByMd5(md5, false);
        if(file.exists()){
            try {
                return Files.toString(file, Charsets.UTF_8);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "PageCache{" +
                "baseDir=" + baseDir +
                ", chars=" + chars +
                ", levels=" + levels +
                '}';
    }
}
