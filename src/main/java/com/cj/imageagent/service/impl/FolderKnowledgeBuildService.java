package com.cj.imageagent.service.impl;

import com.cj.imageagent.Utils.TextSplitUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class FolderKnowledgeBuildService {

    @Autowired
    @Qualifier("myVectorStore")
    private VectorStore vectorStore;

    private TextSplitUtils textSplitUtil=new TextSplitUtils();

    // ======================
    // 功能 1：单个文件上传导入（用户上传用）
    // ======================
    public String importSingleFile(MultipartFile file) {
        try {
            List<Document> documents = readFileContent(file);
            List<Document> splitDocs = textSplitUtil.splitByCharacterCount(documents);
            vectorStore.add(splitDocs);
            return "✅ 单个文件导入成功！共 " + splitDocs.size() + " 条知识块";
        } catch (Exception e) {
            return "❌ 导入失败：" + e.getMessage();
        }
    }

    // ======================
    // 功能 2：文件夹批量导入（本地构建用）
    // ======================
    public String importFromFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return "文件夹不存在";

        List<File> documentFiles = listAllSupportedFiles(folder);
        List<Document> allDocs = new ArrayList<>();

        if (documentFiles.isEmpty()) return "文件夹为空";

        for (File file : documentFiles) {
            try {
                List<Document> docs = readFileContent(file);
                allDocs.addAll(textSplitUtil.splitByCharacterCount(docs));
                System.out.println("已处理：" + file.getName());
            } catch (Exception e) {
                System.err.println("跳过：" + file.getName());
            }
        }
        vectorStore.add(allDocs);
        return "✅ 批量导入完成，共 " + allDocs.size() + " 条知识块";
    }

    // ======================
    // 统一文件读取MultipartFile 和 File
    // ======================
    public List<Document> readFileContent(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename().toLowerCase();
        Resource resource = new ByteArrayResource(file.getBytes());

        if (fileName.endsWith(".pdf")) {
            ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(
                    resource,
                    PdfDocumentReaderConfig.builder()
                            .withPageTopMargin(0)
                            .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                    .withNumberOfTopTextLinesToDelete(0)
                                    .build())
                            .withPagesPerDocument(1)
                            .build());
            return pdfReader.read();
        } else {
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            return reader.read();
        }
    }
    // 处理本地File文件
    public List<Document> readFileContent(File file) throws Exception {
        String fileName = file.getName().toLowerCase();
        Resource resource = new FileSystemResource(file);
        if (fileName.endsWith(".pdf")) {
            ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(
                    resource,
                    PdfDocumentReaderConfig.builder()
                            .withPageTopMargin(0)            // 页顶部边距
                            .withPageBottomMargin(0)         // 页底部边距
                            .withPagesPerDocument(1)
                            .build());
            return pdfReader.read();
        } else {
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            return reader.read();
        }
    }


    // 遍历文件夹，只获取支持的文档类型
    private List<File> listAllSupportedFiles(File folder) {
        List<File> result = new ArrayList<>();
        File[] files = folder.listFiles();

        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) continue; // 不递归子目录

            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".pdf")
                    || fileName.endsWith(".docx")
                    || fileName.endsWith(".txt")
                    || fileName.endsWith(".md")) {
                result.add(file);
            }
        }
        return result;
    }

//    /**
//     * 上传 PDF / DOCX / TXT → 解析 → 分块 → 入库知识库
//     */
//    public String importFileToKnowledge(MultipartFile file) throws IOException {
//        // 1. 转临时文件
//        File tempFile = convertMultiPartToFile(file);
//
//        try {
//            // 2. Tika 自动解析（支持 PDF、DOCX、TXT、HTML…）
//            TikaDocumentReader reader = new TikaDocumentReader(tempFile);
//            List<Document> documents = reader.read();
//
//            // 3. 文本分块（关键！）
//            List<Document> splitDocs = textSplitter.split(documents);
//
//            // 4. 存入向量库
//            myVectorStore.add(splitDocs);
//
//            return "文件导入成功！共解析出 " + splitDocs.size() + " 条知识块";
//        } finally {
//            tempFile.delete(); // 删除临时文件
//        }
//    }

}
