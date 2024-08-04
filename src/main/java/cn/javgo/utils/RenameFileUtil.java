package cn.javgo.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Desc: 批量递归修改文件名工具类
 * Use:
 * 1. 初始化 blacklists：List<String> blacklists = Arrays.asList("【海量资源：www.abcdefg.com】");
 * 2. 初始化工具类：RenameFileUtil renameFileUtil = new RenameFileUtil(blacklists);
 * 3. 重命名：renameFileUtil.renameFilesInDirectory(rootPath);
 *
 * @author javgo
 * @create 2024-08-03 23:22
 */
public class RenameFileUtil {

    private final List<String> blacklists;

    public RenameFileUtil(List<String> blacklists) {
        this.blacklists = new ArrayList<>(blacklists);
    }

    /**
     * 根据提供的目录路径和黑名单替换内容重命名文件
     * @param rootPath 要替换的目录路径
     * @throws IOException 如果文件操作失败
     */
    public void renameFilesInDirectory(String rootPath) throws IOException {
        File rootDir = new File(rootPath);

        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new IllegalArgumentException("提供的路径不存在或不是目录: " + rootPath);
        }

        List<File> files = new ArrayList<>();
        findFiles(files, rootDir);

        for (File file : files) {
            renameFile(file);
        }
    }

    /**
     * 遍历目录，找到所有文件
     * @param files 存储所有文件的列表
     * @param dir 要遍历的目录
     */
    private void findFiles(List<File> files, File dir) {
        File[] listedFiles = dir.listFiles();
        if (listedFiles != null) {
            for (File file : listedFiles) {
                if (file.isFile()) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    findFiles(files, file);
                }
            }
        }
    }

    /**
     * 重命名文件，根据黑名单替换文件路径中的指定内容
     * @param file 要重命名的文件
     * @throws IOException 如果重命名操作失败
     */
    private void renameFile(File file) throws IOException {
        String originalPath = file.getAbsolutePath();
        String newPath = originalPath;

        for (String blacklist : blacklists) {
            newPath = newPath.replace(blacklist, "");
        }

        if (!originalPath.equals(newPath)) {
            Path source = Paths.get(originalPath);
            Path target = Paths.get(newPath);

            // 确保目标文件不存在
            if (Files.exists(target)) {
                throw new IOException("目标文件已存在: " + newPath);
            }

            Files.move(source, target);
            System.out.println("重命名文件: " + originalPath + " -> " + newPath);
        }
    }
}
