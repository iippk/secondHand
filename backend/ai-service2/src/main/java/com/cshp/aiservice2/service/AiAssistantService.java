package com.cshp.aiservice2.service;

import com.cshp.aiservice2.config.QianwenConfig;
import com.cshp.aiservice2.dto.*;
import com.cshp.aiservice2.entity.Order;
import com.cshp.aiservice2.repository.ProductRepository;
import com.cshp.aiservice2.repository.OrderRepository;
import com.cshp.aiservice2.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiAssistantService {

    private final WebClient webClient;
    private final QianwenConfig qianwenConfig;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // 商品分类关键词映射（保留用于商品展示分类）
    private static final Map<String, String> CATEGORY_KEYWORDS = new HashMap<>();
    static {
        // 手机类
        CATEGORY_KEYWORDS.put("手机", "手机");
        CATEGORY_KEYWORDS.put("iphone", "手机");
        CATEGORY_KEYWORDS.put("苹果", "手机");
        CATEGORY_KEYWORDS.put("小米", "手机");
        CATEGORY_KEYWORDS.put("华为", "手机");
        CATEGORY_KEYWORDS.put("荣耀", "手机");
        CATEGORY_KEYWORDS.put("oppo", "手机");
        CATEGORY_KEYWORDS.put("vivo", "手机");
        CATEGORY_KEYWORDS.put("三星", "手机");
        CATEGORY_KEYWORDS.put("一加", "手机");
        CATEGORY_KEYWORDS.put("realme", "手机");
        CATEGORY_KEYWORDS.put("魅族", "手机");

        // 电脑类
        CATEGORY_KEYWORDS.put("电脑", "电脑");
        CATEGORY_KEYWORDS.put("笔记本", "电脑");
        CATEGORY_KEYWORDS.put("macbook", "电脑");
        CATEGORY_KEYWORDS.put("联想", "电脑");
        CATEGORY_KEYWORDS.put("戴尔", "电脑");
        CATEGORY_KEYWORDS.put("华硕", "电脑");
        CATEGORY_KEYWORDS.put("惠普", "电脑");
        CATEGORY_KEYWORDS.put("surface", "电脑");

        // 平板类
        CATEGORY_KEYWORDS.put("平板", "平板");
        CATEGORY_KEYWORDS.put("ipad", "平板");

        // 耳机类
        CATEGORY_KEYWORDS.put("耳机", "耳机");
        CATEGORY_KEYWORDS.put("airpods", "耳机");
        CATEGORY_KEYWORDS.put("蓝牙耳机", "耳机");

        // 相机类
        CATEGORY_KEYWORDS.put("相机", "相机");
        CATEGORY_KEYWORDS.put("单反", "相机");
        CATEGORY_KEYWORDS.put("微单", "相机");
        CATEGORY_KEYWORDS.put("佳能", "相机");
        CATEGORY_KEYWORDS.put("尼康", "相机");

        // 其他电子产品
        CATEGORY_KEYWORDS.put("手表", "智能手表");
        CATEGORY_KEYWORDS.put("智能手表", "智能手表");
        CATEGORY_KEYWORDS.put("手环", "智能手环");
    }

    public AiAssistantService(QianwenConfig qianwenConfig,
                              ProductRepository productRepository,
                              OrderRepository orderRepository) {
        this.qianwenConfig = qianwenConfig;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;

        this.webClient = WebClient.builder()
                .baseUrl(qianwenConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + qianwenConfig.getApiKey())
                .build();
    }

    public ChatResponse chatWithAI(ChatRequest chatRequest, String studentId) {
        try {
            // 1. 首先获取完整的商品数据（不进行预筛选）
            String allProductsContext = getAllProductsContext();
            String userContext = getUserContext(studentId);

            // 2. 构建更智能的系统提示词
            String systemPrompt = buildSmartSystemPrompt(allProductsContext, userContext);

            // 构建请求消息
            QianwenRequest.Message systemMessage = new QianwenRequest.Message("system", systemPrompt);
            QianwenRequest.Message userMessage = new QianwenRequest.Message("user", chatRequest.getMessage());

            // 构建请求
            QianwenRequest request = new QianwenRequest();
            request.setModel(qianwenConfig.getModel());

            QianwenRequest.Input input = new QianwenRequest.Input();
            input.setMessages(Arrays.asList(systemMessage, userMessage));
            request.setInput(input);

            QianwenRequest.Parameters parameters = new QianwenRequest.Parameters();
            parameters.setTemperature(qianwenConfig.getTemperature());
            parameters.setMax_tokens(qianwenConfig.getMaxTokens());
            request.setParameters(parameters);

            // 调用阿里千问API
            QianwenResponse response = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(QianwenResponse.class)
                    .block();

            if (response != null && response.getOutput() != null &&
                    response.getOutput().getChoices() != null &&
                    !response.getOutput().getChoices().isEmpty()) {

                String aiResponse = response.getOutput().getChoices().get(0).getMessage().getContent();
                String conversationId = chatRequest.getConversationId() != null ?
                        chatRequest.getConversationId() : UUID.randomUUID().toString();

                log.info("AI响应成功，会话ID: {}", conversationId);
                return ChatResponse.success(aiResponse, conversationId);

            } else {
                log.error("AI响应格式异常");
                return ChatResponse.error("AI服务响应异常");
            }

        } catch (Exception e) {
            log.error("调用AI服务失败: {}", e.getMessage(), e);
            return ChatResponse.error("AI服务暂时不可用，请稍后重试");
        }
    }

    private String getAllProductsContext() {
        // 获取更多商品数据，让千问自己筛选
        List<Product> allProducts = productRepository.findRecentProducts(50);

        StringBuilder context = new StringBuilder();
        context.append("【平台商品数据】\n");
        context.append("以下是平台当前在售的商品列表：\n\n");

        if (allProducts.isEmpty()) {
            context.append("目前平台暂无商品\n");
            return context.toString();
        }

        // 按分类组织商品
        Map<String, List<Product>> productsByCategory = allProducts.stream()
                .collect(Collectors.groupingBy(p -> detectProductCategory(p.getTitle())));

        for (Map.Entry<String, List<Product>> entry : productsByCategory.entrySet()) {
            String category = entry.getKey();
            List<Product> products = entry.getValue();

            context.append("【").append(category).append("】\n");
            for (Product product : products) {
                String condition = product.getCondition() != null ? product.getCondition() : "未知";
                String description = product.getDescription() != null ?
                        (product.getDescription().length() > 50 ?
                                product.getDescription().substring(0, 50) + "..." :
                                product.getDescription()) : "暂无描述";

                context.append(String.format("- %s：%.2f元，成色：%s，描述：%s\n",
                        product.getTitle(), product.getPrice(), condition, description));
            }
            context.append("\n");
        }

        // 添加分类统计
        context.append("【分类统计】\n");
        productsByCategory.forEach((category, products) ->
                context.append(String.format("- %s：%d个商品\n", category, products.size())));

        // 添加价格统计
        DoubleSummaryStatistics priceStats = allProducts.stream()
                .mapToDouble(p -> p.getPrice() != null ? p.getPrice().doubleValue() : 0)
                .summaryStatistics();

        context.append(String.format("\n【价格概况】最低价：%.2f元，最高价：%.2f元，平均价：%.2f元\n",
                priceStats.getMin(), priceStats.getMax(), priceStats.getAverage()));

        return context.toString();
    }

    private String buildSmartSystemPrompt(String productsContext, String userContext) {
        return "你是一个校园二手交易平台的AI助手，具有以下能力：\n\n" +
                "1. **商品识别** - 能够识别各种商品类型，包括手机（如小米17promax）、电脑、耳机等\n" +
                "2. **意图理解** - 能够理解用户是想购买、出售、咨询价格还是寻求推荐\n" +
                "3. **数据查询** - 能够基于平台商品数据给出准确建议\n\n" +
                "重要说明：\n" +
                "- 你是AI大模型，具备强大的语义理解能力，能够识别用户提到的任何商品类型\n" +
                "- 即使商品名称不完整或包含新型号（如'小米17promax'），你也能识别出它是手机\n" +
                "- 当用户询问价格时，你能从商品数据中提取相关价格信息\n" +
                "- 当用户寻求推荐时，你能基于商品数据给出个性化建议\n\n" +
                "平台商品数据：\n" + productsContext + "\n\n" +
                "用户数据：\n" + userContext + "\n\n" +
                "回答要求：\n" +
                "1. 如果平台有用户询问的商品，请具体推荐并说明价格\n" +
                "2. 如果平台没有完全匹配的商品，请推荐类似商品\n" +
                "3. 对于价格咨询，基于平台数据给出合理价格范围\n" +
                "4. 保持回答友好、专业、实用\n\n" +
                "示例：\n" +
                "用户：'有什么7000左右的手机推荐吗'\n" +
                "你应该：从商品数据中找出价格在7000元左右的手机进行推荐\n\n" +
                "用户：'小米17promax多少钱'\n" +
                "你应该：识别这是手机，然后查找平台是否有这个型号或类似手机的价格参考";
    }

    private String getUserContext(String studentId) {
        if (studentId == null || studentId.isEmpty()) {
            return "当前用户为访客用户，无历史交易数据\n";
        }

        StringBuilder userInfo = new StringBuilder();
        userInfo.append("【用户个人数据】\n");

        try {
            // 获取购买记录
            List<Order> buyOrders = orderRepository.findOrdersByBuyer(studentId, 3);
            if (!buyOrders.isEmpty()) {
                userInfo.append("最近购买记录：\n");
                for (Order order : buyOrders) {
                    userInfo.append(String.format("- %s：%.2f元\n",
                            order.getProductTitle(), order.getPrice()));
                }
            } else {
                userInfo.append("暂无购买记录\n");
            }

            // 获取交易统计
            Long buyCount = orderRepository.countCompletedPurchases(studentId);
            Long sellCount = orderRepository.countCompletedSales(studentId);

            userInfo.append(String.format("交易统计：购买%d次，卖出%d次\n",
                    buyCount != null ? buyCount : 0,
                    sellCount != null ? sellCount : 0));

        } catch (Exception e) {
            log.warn("获取用户交易数据失败: {}", e.getMessage());
            userInfo.append("用户交易数据暂不可用\n");
        }

        return userInfo.toString();
    }

    // 商品分类识别（保留用于商品展示分类）
    private String detectProductCategory(String productTitle) {
        if (productTitle == null) return "其他";

        String title = productTitle.toLowerCase();

        // 品牌型号识别（如小米17promax）
        if (title.matches(".*(小米\\d+.*|红米\\d+.*|华为\\w+\\d+.*|荣耀\\d+.*|iphone\\s*\\d+.*).*")) {
            return "手机";
        }

        // 关键词匹配
        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (title.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "其他";
    }

    // 专门针对二手物品价格评估的方法（保留，因为这是专门的功能）
    public ChatResponse evaluateItemPrice(String itemName, String condition, String description, String studentId) {
        // 获取商品数据作为参考
        List<Product> allProducts = productRepository.findRecentProducts(30);

        // 筛选类似商品
        List<Product> similarProducts = allProducts.stream()
                .filter(p -> p.getTitle() != null &&
                        (p.getTitle().toLowerCase().contains(itemName.toLowerCase()) ||
                                detectProductCategory(p.getTitle()).equals(detectProductCategory(itemName))))
                .collect(Collectors.toList());

        StringBuilder context = new StringBuilder();
        context.append("【价格评估参考数据】\n");

        if (!similarProducts.isEmpty()) {
            context.append("平台类似商品价格：\n");
            for (Product product : similarProducts) {
                context.append(String.format("- %s：%.2f元（成色：%s）\n",
                        product.getTitle(), product.getPrice(), product.getCondition()));
            }

            // 计算价格统计
            DoubleSummaryStatistics stats = similarProducts.stream()
                    .mapToDouble(p -> p.getPrice().doubleValue())
                    .summaryStatistics();

            context.append(String.format("\n价格统计：最低%.2f元，最高%.2f元，平均%.2f元\n",
                    stats.getMin(), stats.getMax(), stats.getAverage()));
        } else {
            context.append("平台暂无类似商品参考\n");
        }

        String prompt = String.format(
                "请基于以下平台数据评估这个二手物品的合理价格：\n%s\n\n" +
                        "待评估物品：\n" +
                        "物品名称：%s\n" +
                        "成色：%s\n" +
                        "描述：%s\n\n" +
                        "请给出合理的价格区间和建议售价，并说明理由。",
                context.toString(), itemName, condition, description
        );

        ChatRequest request = new ChatRequest();
        request.setMessage(prompt);

        return chatWithAI(request, studentId);
    }
}