package com.yeyuchunlan.aicodehelper.tools;


import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 面试题检索工具，通过 @Tool 暴露给模型。
 * 框架会把方法名、value 里的描述和参数说明生成 JSON Schema 发给模型；
 * 模型决定调用后由框架执行本方法，再把返回的字符串回传给模型。
 */
@Slf4j
public class InterviewQuestionTool {

    /**
     * 从面试鸭网站获取关键词相关的面试题列表
     *
     * @param keyword 搜索关键词（如"redis"、"java多线程"）
     * @return 面试题列表，若失败则返回错误信息
     */
    @Tool(name = "interviewQuestionSearch", value = """
            当用户需要查找面试题、题库，或询问某个技术方向有哪些高频面试题时调用。
            参数是技术关键词，例如 "Redis"、"Java 并发"、"MySQL 索引"。
            返回面试鸭网站上的相关题目列表。
            """
    )
    public String searchInterviewQuestions(@P(value = "要搜索的技术关键词，例如 Redis") String keyword) {
        List<String> questions = new ArrayList<>();
        // 构建搜索URL（编码关键词以支持中文）
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = "https://www.mianshiya.com/search/all?searchText=" + encodedKeyword;
        // 发送请求并解析页面
        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();
        } catch (IOException e) {
            log.error("get web error", e);
            return e.getMessage();
        }
        // 提取面试题：按面试鸭搜索页的表格结构取题目链接
        Elements questionElements = doc.select(".ant-table-cell > a");
        questionElements.forEach(el -> questions.add(el.text().trim()));
        return String.join("\n", questions);
    }
}

