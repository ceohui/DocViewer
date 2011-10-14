package com.log4ic.utils.convert;

import com.log4ic.DocViewer;
import com.log4ic.utils.FileUtils;
import com.log4ic.utils.convert.office.OfficeConverter;
import com.log4ic.utils.convert.pdf.PDFConverter;

import java.io.File;
import java.util.LinkedList;

/**
 * @author: 张立鑫
 * @date: 11-8-19 下午4:38
 */
public class DocViewerConverter {
    private static OfficeConverter officeConverter;
    private static PDFConverter pdfConverter;
    private static Object lock = new Object();

    private static LinkedList<File> runningQueue = new LinkedList<File>();

    public static File deploy(File file, String outPath) {

        String fileName = FileUtils.getFilePrefix(file);

        File dir = new File(FileUtils.appendFileSeparator(outPath) + fileName);

        if (dir.isFile()) {
            dir.renameTo(new File(dir.getPath() + ".backup"));
        } else {
            dir.mkdirs();
        }

        return dir;
    }

    public static LinkedList<File> getRunningQueue() {
        return runningQueue;
    }

    public static File toSwf(File file, String outPath) throws Exception {
        synchronized (lock) {
            if (pdfConverter == null) {
                //PDFConverter.loadConfig();
                pdfConverter = new PDFConverter();
            }
        }
        runningQueue.add(file);
        String suffix = FileUtils.getFileSuffix(file);
        if (suffix == null) {
            throw new Exception("The file not has a suffix!");
        }
        if (!suffix.toLowerCase().equals("pdf")) {
            file = toPDF(file, outPath);
        }

        File out = pdfConverter.convert(file, outPath, DocViewer.isSplitPage(), false);
        runningQueue.remove(file);
        return out;
    }

    public static File toPDF(File file, String outPath) throws Exception {
        synchronized (lock) {
            if (officeConverter == null) {
                officeConverter = new OfficeConverter();
            }
        }

        runningQueue.add(file);
        File pdf = null;

        File dir = deploy(file, outPath);
        pdf = new File(dir.getPath() + File.separator + FileUtils.getFilePrefix(file) + ".pdf");
        if (!pdf.exists()) {
            pdf = officeConverter.toPDF(file, dir.getPath());
        }
        runningQueue.remove(file);
        return pdf;
    }
}