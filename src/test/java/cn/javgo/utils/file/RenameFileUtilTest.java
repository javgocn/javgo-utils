package cn.javgo.utils.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * Desc: 批量递归修改文件名测试类
 *
 * @author javgo
 * @create 2024-08-04 12:51
 */
public class RenameFileUtilTest {

    private RenameFileUtil renameFileUtil;

    @BeforeEach
    public void setUp() {
        List<String> blacklists = List.of("【海量资源：666java.com】");
        renameFileUtil = new RenameFileUtil(blacklists);
    }

    @Test
    public void testRenameFilesInDirectory() throws IOException {
        String rootPath = "E:\\Downloads\\test";
        renameFileUtil.renameFilesInDirectory(rootPath);
    }
}
