# Coze Java SDK - Examples Guide

This guide provides practical examples for common use cases with the Coze Java SDK.

## Table of Contents

1. [Authentication Examples](#authentication-examples)
2. [Basic Chat Examples](#basic-chat-examples)
3. [Advanced Chat Features](#advanced-chat-features)
4. [Bot Management](#bot-management)
5. [File and Dataset Operations](#file-and-dataset-operations)
6. [Workflow Integration](#workflow-integration)
7. [Audio Processing](#audio-processing)
8. [WebSocket Operations](#websocket-operations)
9. [Error Handling Patterns](#error-handling-patterns)
10. [Production Patterns](#production-patterns)

## Authentication Examples

### Personal Access Token

```java
// Simple PAT authentication
public class TokenAuthExample {
    public static void main(String[] args) {
        String token = System.getenv("COZE_API_TOKEN");
        CozeAPI coze = new CozeAPI.Builder()
            .auth(new TokenAuth(token))
            .build();
        
        // Use the client...
    }
}
```

### Web OAuth Flow

```java
public class WebOAuthExample {
    private static final String CLIENT_ID = System.getenv("COZE_WEB_OAUTH_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("COZE_WEB_OAUTH_CLIENT_SECRET");
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    
    public static void main(String[] args) throws Exception {
        // 1. Setup OAuth client
        WebOAuthClient oauth = new WebOAuthClient.WebOAuthBuilder()
            .clientID(CLIENT_ID)
            .clientSecret(CLIENT_SECRET)
            .baseURL("https://api.coze.com")
            .build();
        
        // 2. Generate authorization URL
        String state = "secure-random-state";
        String authURL = oauth.getAuthURL(REDIRECT_URI, state);
        System.out.println("Visit: " + authURL);
        
        // 3. After user authorizes, exchange code for token
        String authCode = "code_from_callback"; // Get from your web server
        OAuthResp tokenResp = oauth.getAccessToken(REDIRECT_URI, authCode);
        
        // 4. Create Coze client
        CozeAPI coze = new CozeAPI.Builder()
            .auth(new TokenAuth(tokenResp.accessToken))
            .build();
    }
}
```

## Basic Chat Examples

### Simple Q&A Chat

```java
public class SimpleChatExample {
    public static void main(String[] args) {
        CozeAPI coze = createClient();
        
        CreateChatReq req = CreateChatReq.builder()
            .botID(System.getenv("BOT_ID"))
            .userID("user-123")
            .messages(Collections.singletonList(
                Message.buildUserQuestionText("What is the capital of France?")))
            .build();
        
        ChatPoll result = coze.chat().createAndPoll(req);
        
        for (Message message : result.messages) {
            if (message.role == MessageRole.ASSISTANT) {
                System.out.println("Bot: " + message.content);
            }
        }
        
        System.out.println("Tokens used: " + result.chat.usage.tokenCount);
    }
}
```

### Streaming Chat

```java
public class StreamingChatExample {
    public static void main(String[] args) {
        CozeAPI coze = createClient();
        
        CreateChatReq req = CreateChatReq.builder()
            .botID(System.getenv("BOT_ID"))
            .userID("user-123")
            .messages(Collections.singletonList(
                Message.buildUserQuestionText("Tell me a story about a brave knight.")))
            .stream(true)
            .build();
        
        StringBuilder response = new StringBuilder();
        
        Flowable<ChatEvent> stream = coze.chat().stream(req);
        stream.blockingForEach(event -> {
            switch (event.getEvent()) {
                case CONVERSATION_MESSAGE_DELTA:
                    String delta = event.getMessage().getContent();
                    System.out.print(delta);
                    response.append(delta);
                    break;
                case CONVERSATION_CHAT_COMPLETED:
                    System.out.println("\n\nCompleted! Tokens: " + 
                        event.getChat().getUsage().getTokenCount());
                    break;
                case ERROR:
                    System.err.println("Error: " + event.getError());
                    break;
            }
        });
    }
}
```

## Advanced Chat Features

### Multi-turn Conversation

```java
public class ConversationExample {
    private final CozeAPI coze;
    private final List<Message> conversationHistory;
    
    public ConversationExample() {
        this.coze = createClient();
        this.conversationHistory = new ArrayList<>();
    }
    
    public void chat(String userMessage) {
        // Add user message to history
        conversationHistory.add(Message.buildUserQuestionText(userMessage));
        
        CreateChatReq req = CreateChatReq.builder()
            .botID(System.getenv("BOT_ID"))
            .userID("user-123")
            .messages(new ArrayList<>(conversationHistory))
            .build();
        
        ChatPoll result = coze.chat().createAndPoll(req);
        
        // Add assistant's response to history
        for (Message message : result.messages) {
            if (message.role == MessageRole.ASSISTANT) {
                conversationHistory.add(message);
                System.out.println("Bot: " + message.content);
            }
        }
    }
    
    public static void main(String[] args) {
        ConversationExample conv = new ConversationExample();
        conv.chat("Hello, I'm planning a trip to Japan.");
        conv.chat("What are the must-visit places in Tokyo?");
        conv.chat("How much should I budget for food per day?");
    }
}
```

### Chat with Images

```java
public class ImageChatExample {
    public static void main(String[] args) {
        CozeAPI coze = createClient();
        
        // Upload image first
        String imagePath = "/path/to/image.jpg";
        FileInfo imageFile = coze.files().upload(imagePath);
        
        // Create message with image
        Message imageMessage = Message.builder()
            .role(MessageRole.USER)
            .contentType(MessageContentType.OBJECT_STRING)
            .objectContent(Arrays.asList(
                MessageObjectString.buildText("What do you see in this image?"),
                MessageObjectString.buildImageByURL("file://" + imageFile.getID())
            ))
            .build();
        
        CreateChatReq req = CreateChatReq.builder()
            .botID(System.getenv("BOT_ID"))
            .userID("user-123")
            .messages(Collections.singletonList(imageMessage))
            .build();
        
        ChatPoll result = coze.chat().createAndPoll(req);
        
        for (Message message : result.messages) {
            if (message.role == MessageRole.ASSISTANT) {
                System.out.println("Bot's analysis: " + message.content);
            }
        }
    }
}
```

## Bot Management

### Complete Bot Lifecycle

```java
public class BotManagementExample {
    private final CozeAPI coze;
    
    public BotManagementExample() {
        this.coze = createClient();
    }
    
    public String createAndPublishBot() {
        try {
            // 1. Upload avatar
            String avatarPath = "/path/to/avatar.png";
            FileInfo avatar = coze.files().upload(avatarPath);
            
            // 2. Create bot
            BotPromptInfo promptInfo = new BotPromptInfo(
                "You are a helpful customer service assistant. " +
                "Always be polite and provide accurate information."
            );
            
            BotOnboardingInfo onboarding = BotOnboardingInfo.builder()
                .prologue("Hello! I'm here to help you with any questions.")
                .suggestedQuestions(Arrays.asList(
                    "How can I track my order?",
                    "What is your return policy?",
                    "How do I contact support?"
                ))
                .build();
            
            CreateBotReq createReq = CreateBotReq.builder()
                .spaceID(System.getenv("WORKSPACE_ID"))
                .name("Customer Service Bot")
                .description("AI-powered customer service assistant")
                .promptInfo(promptInfo)
                .onboardingInfo(onboarding)
                .iconFileID(avatar.getID())
                .build();
            
            CreateBotResp createResp = coze.bots().create(createReq);
            String botID = createResp.getBotID();
            System.out.println("Bot created with ID: " + botID);
            
            // 3. Publish bot
            PublishBotReq publishReq = PublishBotReq.of(botID);
            PublishBotResp publishResp = coze.bots().publish(publishReq);
            System.out.println("Bot published successfully");
            
            return botID;
            
        } catch (Exception e) {
            System.err.println("Failed to create bot: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    public void updateBot(String botID) {
        UpdateBotReq updateReq = UpdateBotReq.builder()
            .botID(botID)
            .name("Enhanced Customer Service Bot")
            .description("Updated AI assistant with improved capabilities")
            .build();
        
        coze.bots().update(updateReq);
        
        // Republish after update
        coze.bots().publish(PublishBotReq.of(botID));
        System.out.println("Bot updated and republished");
    }
    
    public void listAllBots() {
        ListBotReq req = ListBotReq.builder()
            .spaceID(System.getenv("WORKSPACE_ID"))
            .pageSize(50)
            .build();
        
        ListBotResp resp = coze.bots().list(req);
        
        System.out.println("Available bots:");
        for (Bot bot : resp.bots) {
            System.out.printf("- %s (ID: %s) - %s%n", 
                bot.name, bot.botID, bot.description);
        }
    }
}
```

## File and Dataset Operations

### Knowledge Base Management

```java
public class KnowledgeBaseExample {
    private final CozeAPI coze;
    
    public KnowledgeBaseExample() {
        this.coze = createClient();
    }
    
    public Long createKnowledgeBase() {
        // Create dataset
        CreateDatasetReq datasetReq = CreateDatasetReq.builder()
            .name("Company Documentation")
            .description("Internal company knowledge base")
            .build();
        
        Dataset dataset = coze.datasets().create(datasetReq);
        Long datasetID = Long.parseLong(dataset.getDatasetID());
        
        // Add documents from various sources
        CreateDocumentReq docReq = CreateDocumentReq.builder()
            .datasetID(datasetID)
            .documentBases(Arrays.asList(
                // From company website
                DocumentBase.buildWebPage("Company FAQ", "https://company.com/faq"),
                // From local policy document
                DocumentBase.buildLocalFile(
                    "Employee Handbook", 
                    loadFileContent("/docs/handbook.txt"), 
                    "txt"
                ),
                // From API documentation
                DocumentBase.buildWebPage("API Docs", "https://api.company.com/docs")
            ))
            .build();
        
        CreateDocumentResp docResp = coze.datasets().documents().create(docReq);
        System.out.println("Added " + docResp.getDocumentInfos().size() + " documents");
        
        return datasetID;
    }
    
    public void manageDocuments(Long datasetID) {
        // List existing documents
        ListDocumentReq listReq = ListDocumentReq.builder()
            .datasetID(datasetID)
            .page(1)
            .size(10)
            .build();
        
        PageResult<Document> documents = coze.datasets().documents().list(listReq);
        System.out.println("Found " + documents.getItems().size() + " documents");
        
        // Update first document
        if (!documents.getItems().isEmpty()) {
            Document firstDoc = documents.getItems().get(0);
            Long docID = Long.parseLong(firstDoc.getDocumentID());
            
            UpdateDocumentReq updateReq = UpdateDocumentReq.builder()
                .documentID(docID)
                .documentName("Updated " + firstDoc.getDocumentName())
                .build();
            
            coze.datasets().documents().update(updateReq);
            System.out.println("Updated document: " + firstDoc.getDocumentName());
        }
    }
    
    private String loadFileContent(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }
}
```

## Workflow Integration

### Interactive Workflow

```java
public class WorkflowExample {
    private final CozeAPI coze;
    
    public WorkflowExample() {
        this.coze = createClient();
    }
    
    public void runInteractiveWorkflow() {
        String workflowID = System.getenv("WORKFLOW_ID");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_input", "I need help with my order");
        parameters.put("priority", "high");
        
        RunWorkflowReq req = RunWorkflowReq.builder()
            .workflowID(workflowID)
            .parameters(parameters)
            .build();
        
        Flowable<WorkflowEvent> stream = coze.workflows().runs().stream(req);
        
        stream.blockingForEach(event -> {
            switch (event.getEvent()) {
                case WORKFLOW_STARTED:
                    System.out.println("Workflow started");
                    break;
                    
                case WORKFLOW_INTERRUPTED:
                    // Workflow needs user input
                    handleWorkflowInteraction(event);
                    break;
                    
                case WORKFLOW_COMPLETED:
                    System.out.println("Workflow completed with result: " + 
                        event.getData());
                    break;
                    
                case ERROR:
                    System.err.println("Workflow error: " + event.getError());
                    break;
            }
        });
    }
    
    private void handleWorkflowInteraction(WorkflowEvent event) {
        // Get question from workflow
        String question = event.getInterruptData().getQuestion();
        System.out.println("Workflow asks: " + question);
        
        // Simulate user response (in real app, get from user input)
        String userResponse = getUserResponse(question);
        
        // Resume workflow with answer
        ResumeWorkflowReq resumeReq = ResumeWorkflowReq.builder()
            .answer(userResponse)
            .build();
        
        // Continue processing the resumed stream
        Flowable<WorkflowEvent> resumedStream = coze.workflows().runs()
            .resume(event.getWorkflowRunID(), resumeReq);
        
        // Process resumed stream recursively
        resumedStream.blockingForEach(this::handleWorkflowEvent);
    }
    
    private String getUserResponse(String question) {
        // In a real application, this would prompt the user
        // For demo purposes, return a sample response
        return "Please provide the order number and I'll help you track it.";
    }
    
    private void handleWorkflowEvent(WorkflowEvent event) {
        // Handle events from resumed workflow
        System.out.println("Resumed workflow event: " + event.getEvent());
    }
}
```

## Production Patterns

### Production-Ready Client

```java
public class ProductionCozeClient {
    private final CozeAPI coze;
    private final Logger logger;
    
    public ProductionCozeClient() {
        this.logger = LoggerFactory.getLogger(ProductionCozeClient.class);
        this.coze = createProductionClient();
    }
    
    private CozeAPI createProductionClient() {
        // Custom HTTP client with connection pooling and timeouts
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(new RetryInterceptor(3))
            .addInterceptor(new LoggingInterceptor(logger))
            .build();
        
        String token = System.getenv("COZE_API_TOKEN");
        if (token == null) {
            throw new IllegalStateException("COZE_API_TOKEN environment variable required");
        }
        
        return new CozeAPI.Builder()
            .auth(new TokenAuth(token))
            .baseURL(System.getenv("COZE_API_BASE"))
            .client(httpClient)
            .logger(logger)
            .build();
    }
    
    public CompletableFuture<String> chatAsync(String botID, String userID, String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CreateChatReq req = CreateChatReq.builder()
                    .botID(botID)
                    .userID(userID)
                    .messages(Collections.singletonList(
                        Message.buildUserQuestionText(message)))
                    .build();
                
                ChatPoll result = executeWithRetry(() -> coze.chat().createAndPoll(req));
                
                return result.messages.stream()
                    .filter(msg -> msg.role == MessageRole.ASSISTANT)
                    .map(msg -> msg.content)
                    .collect(Collectors.joining("\n"));
                    
            } catch (Exception e) {
                logger.error("Chat failed for user {}", userID, e);
                throw new RuntimeException(e);
            }
        });
    }
    
    private <T> T executeWithRetry(Supplier<T> operation) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return operation.get();
            } catch (RateLimitException e) {
                if (attempt == maxRetries) throw e;
                sleep(e.getRetryAfter() * 1000);
            } catch (IOException e) {
                if (attempt == maxRetries) throw e;
                sleep((long) Math.pow(2, attempt) * 1000);
            }
        }
        throw new RuntimeException("Operation failed after " + maxRetries + " retries");
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @PreDestroy
    public void cleanup() {
        coze.shutdownExecutor();
    }
}
```

### Batch Processing Example

```java
public class BatchProcessingExample {
    private final CozeAPI coze;
    private final ExecutorService executor;
    
    public BatchProcessingExample() {
        this.coze = createClient();
        this.executor = Executors.newFixedThreadPool(10);
    }
    
    public void processBatch(List<ChatRequest> requests) {
        List<CompletableFuture<ChatResult>> futures = requests.stream()
            .map(this::processRequestAsync)
            .collect(Collectors.toList());
        
        CompletableFuture<Void> allComplete = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));
        
        allComplete.thenRun(() -> {
            List<ChatResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            System.out.println("Processed " + results.size() + " requests");
            results.forEach(result -> 
                System.out.println("User " + result.userID + ": " + result.response));
        });
    }
    
    private CompletableFuture<ChatResult> processRequestAsync(ChatRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CreateChatReq chatReq = CreateChatReq.builder()
                    .botID(request.botID)
                    .userID(request.userID)
                    .messages(Collections.singletonList(
                        Message.buildUserQuestionText(request.message)))
                    .build();
                
                ChatPoll result = coze.chat().createAndPoll(chatReq);
                
                String response = result.messages.stream()
                    .filter(msg -> msg.role == MessageRole.ASSISTANT)
                    .map(msg -> msg.content)
                    .collect(Collectors.joining("\n"));
                
                return new ChatResult(request.userID, response);
                
            } catch (Exception e) {
                System.err.println("Failed to process request for user " + request.userID);
                return new ChatResult(request.userID, "Error: " + e.getMessage());
            }
        }, executor);
    }
    
    public static class ChatRequest {
        public final String botID;
        public final String userID;
        public final String message;
        
        public ChatRequest(String botID, String userID, String message) {
            this.botID = botID;
            this.userID = userID;
            this.message = message;
        }
    }
    
    public static class ChatResult {
        public final String userID;
        public final String response;
        
        public ChatResult(String userID, String response) {
            this.userID = userID;
            this.response = response;
        }
    }
}
```

---

These examples demonstrate common patterns and best practices for using the Coze Java SDK in real-world applications. For complete API reference, see the API_REFERENCE.md file.