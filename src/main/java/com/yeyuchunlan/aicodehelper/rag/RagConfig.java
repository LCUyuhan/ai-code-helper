package com.yeyuchunlan.aicodehelper.rag;


import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * RAG（检索增强生成）配置。
 * 启动时把 docs 目录下的文档切片、向量化后存入向量库；
 * 之后每次问答先检索最相关的片段，再连同问题一起交给模型。
 * 通过 rag.enabled 控制是否启用：关闭后应用不加载知识库，也不消耗 embedding 额度。
 */
@Configuration
@ConditionalOnProperty(prefix = "rag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RagConfig {

    @Resource
    private EmbeddingModel qwenEmbeddingModel;      // 文本转向量的模型（text-embedding-v4）

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;  // 向量库，由 starter 自动提供，默认是内存实现

    @Bean
    public ContentRetriever contentRetriever() {
        // ------ RAG ------
        // 1. 从 classpath 加载文档：打包成 jar 后也能正常读取
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("docs");
        // 2. 文档切割：按段落切，每段最多 1000 字，相邻段重叠 200 字，避免答案被切断
        DocumentByParagraphSplitter paragraphSplitter = new DocumentByParagraphSplitter(1000, 200);
        // 3. 向量化并入库
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(paragraphSplitter)
                // 每个片段前面拼上来源文件名再向量化，让文件名里的信息也参与检索匹配
                .textSegmentTransformer(textSegment -> TextSegment.from(
                        textSegment.metadata().getString("file_name") + "\n" + textSegment.text(),
                        textSegment.metadata()
                ))
                // 使用指定的向量模型
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        // 应用启动时执行一次：读取全部文档、切片、向量化、写入向量库
        ingestor.ingest(documents);
        // 4. 检索器：问答时用它从向量库里找相关片段
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5)   // 最多返回 5 个最相似的片段
                .minScore(0.75)  // 相似度低于 0.75 的片段直接丢弃
                .build();
        return contentRetriever;
    }
}
