package com.tree.ncov.Util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;

/**
 * @ClassName com.tree.ncov.Util
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-16 16:11
 * @Version 1.0
 */
@Slf4j
public class FileUtil {

    /**
     *
     * @param zipFileUrl
     * @param destDir
     * @return
     */
    public String unzipFile(String zipFileUrl, String destDir){

        return "";
    }

    public static void toZip(String srcDir, String outPathFile,boolean isDelSrcFile) throws Exception {
        long start = System.currentTimeMillis();
        FileOutputStream out = null;
        ZipOutputStream zos = null;
        try {
            out = new FileOutputStream(new File(outPathFile));
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            if(!sourceFile.exists()){
                throw new Exception("需压缩文件或者文件夹不存在");
            }
//            compress(sourceFile, zos, sourceFile.getName());
//            if(isDelSrcFile){
//                delDir(srcDir);
//            }
            log.info("原文件:{}. 压缩到:{}完成. 是否删除原文件:{}. 耗时:{}ms. ",srcDir,outPathFile,isDelSrcFile,System.currentTimeMillis()-start);
        } catch (Exception e) {
            log.error("zip error from ZipUtils: {}. ",e.getMessage());
            throw new Exception("zip error from ZipUtils");
        } finally {
            try {
                if (zos != null) {zos.close();}
                if (out != null) {out.close();}
            } catch (Exception e) {}
        }
    }

}
