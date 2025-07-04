# Coze Java API SDK - Complete Documentation

[![Maven Central Version](https://img.shields.io/maven-central/v/com.coze/coze-api)](https://central.sonatype.com/artifact/com.coze/coze-api)
[![codecov](https://codecov.io/github/coze-dev/coze-java/graph/badge.svg?token=UXitaQ0wp7)](https://codecov.io/github/coze-dev/coze-java)

## Table of Contents

1. [Overview](#overview)
2. [Installation](#installation)
3. [Quick Start](#quick-start)
4. [Authentication](#authentication)
5. [Core APIs](#core-apis)
6. [Advanced Features](#advanced-features)
7. [Error Handling](#error-handling)
8. [Examples](#examples)
9. [API Reference](#api-reference)
10. [WebSocket Support](#websocket-support)

## Overview

The Coze Java API SDK is a comprehensive library for integrating with Coze's AI platform. It provides full support for all Coze APIs with both synchronous and asynchronous operations, streaming capabilities, and robust error handling.

### Key Features

- ✅ **Complete API Coverage**: All Coze open APIs and authentication methods
- ✅ **Dual Operation Modes**: Both synchronous and asynchronous SDK calls
- ✅ **Streaming Support**: Optimized streaming APIs with reactive programming
- ✅ **Pagination**: Iterator-based pagination for list operations
- ✅ **Type Safety**: Full Java type safety with comprehensive models
- ✅ **WebSocket Support**: Real-time communication capabilities
- ✅ **Flexible Authentication**: Multiple auth methods (PAT, OAuth, JWT, PKCE, Device)
- ✅ **Error Handling**: Comprehensive exception hierarchy
- ✅ **Logging**: Built-in logging support
- ✅ **Timeout Configuration**: Configurable timeouts for all operations

## Installation

### Maven
```xml
<dependency>
    <groupId>com.coze</groupId>
    <artifactId>coze-api</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle
```groovy
dependencies {
    implementation 'com.coze:coze-api:0.1.0'
}
```

## Quick Start

### Basic Setup

```java
import com.coze.openapi.service.service.CozeAPI;
import com.coze.openapi.service.auth.TokenAuth;

public class QuickStart {
    public static void main(String[] args) {
        // Initialize with Personal Access Token
        String token = System.getenv("COZE_API_TOKEN");
        TokenAuth auth = new TokenAuth(token);
        
        // Create the Coze API client
        CozeAPI coze = new CozeAPI.Builder()
                .baseURL(System.getenv("COZE_API_BASE")) // Optional: defaults to api.coze.com
                .auth(auth)
                .readTimeout(10000)  // 10 seconds
                .connectTimeout(5000) // 5 seconds
                .build();
        
        // Your API calls here...
    }
}
```

### First Chat Example

```java
import com.coze.openapi.client.chat.CreateChatReq;
import com.coze.openapi.client.chat.model.Message;
import com.coze.openapi.service.service.chat.ChatPoll;

// Simple non-streaming chat
String botID = System.getenv("PUBLISHED_BOT_ID");
String userID = System.getenv("USER_ID");

CreateChatReq req = CreateChatReq.builder()
    .botID(botID)
    .userID(userID)
    .messages(Collections.singletonList(
        Message.buildUserQuestionText("Hello, what can you do?")))
    .build();

ChatPoll chatResult = coze.chat().createAndPoll(req);
for (Message message : chatResult.messages) {
    System.out.println(message.content);
}
```

## Authentication

The SDK supports multiple authentication methods for different use cases.

### 1. Personal Access Token (PAT)

**Use Case**: Server-to-server applications, development, testing

```java
import com.coze.openapi.service.auth.TokenAuth;

String token = System.getenv("COZE_API_TOKEN");
TokenAuth auth = new TokenAuth(token);

CozeAPI coze = new CozeAPI.Builder()
    .auth(auth)
    .build();
```

**Getting a PAT**:
1. Visit https://www.coze.com/open/oauth/pats (or https://www.coze.cn/open/oauth/pats for CN)
2. Click "Add Token"
3. Configure name, expiration, and permissions
4. Store the token securely

### 2. Web OAuth

**Use Case**: Web applications where users authorize your app

```java
import com.coze.openapi.service.auth.WebOAuthClient;

// Step 1: Create OAuth client
String clientID = System.getenv("COZE_WEB_OAUTH_CLIENT_ID");
String clientSecret = System.getenv("COZE_WEB_OAUTH_CLIENT_SECRET");
String redirectURI = System.getenv("COZE_WEB_OAUTH_REDIRECT_URI");

WebOAuthClient oauth = new WebOAuthClient.WebOAuthBuilder()
    .clientID(clientID)
    .clientSecret(clientSecret)
    .baseURL("https://api.coze.com")
    .build();

// Step 2: Generate authorization URL
String authURL = oauth.getAuthURL(redirectURI, "state123");
// Redirect user to authURL

// Step 3: Exchange code for token (after user authorizes)
String code = "auth_code_from_callback";
OAuthResp tokenResp = oauth.getAccessToken(redirectURI, code);

// Step 4: Use the access token
TokenAuth auth = new TokenAuth(tokenResp.accessToken);
CozeAPI coze = new CozeAPI.Builder().auth(auth).build();
```

### 3. JWT OAuth

**Use Case**: Service accounts, backend applications

```java
import com.coze.openapi.service.auth.JWTOAuthClient;
import com.coze.openapi.service.auth.JWTBuilder;

// Implement JWT builder
public class MyJWTBuilder implements JWTBuilder {
    @Override
    public String build() {
        // Your JWT generation logic
        return generateJWT();
    }
}

JWTOAuthClient jwtOAuth = new JWTOAuthClient.JWTOAuthBuilder()
    .jwtBuilder(new MyJWTBuilder())
    .baseURL("https://api.coze.com")
    .build();

OAuthResp tokenResp = jwtOAuth.getAccessToken();
TokenAuth auth = new TokenAuth(tokenResp.accessToken);
```

### 4. PKCE OAuth

**Use Case**: Mobile apps, single-page applications (more secure)

```java
import com.coze.openapi.service.auth.PKCEOAuthClient;

String clientID = System.getenv("COZE_PKCE_OAUTH_CLIENT_ID");

PKCEOAuthClient pkceOAuth = new PKCEOAuthClient.PKCEOAuthBuilder()
    .clientID(clientID)
    .baseURL("https://api.coze.com")
    .build();

// Generate code verifier and challenge
String codeVerifier = pkceOAuth.generateCodeVerifier();
String codeChallenge = pkceOAuth.generateCodeChallenge(codeVerifier);

// Generate auth URL with PKCE
String authURL = pkceOAuth.getAuthURL(redirectURI, codeChallenge, "state123");

// Exchange code for token
OAuthResp tokenResp = pkceOAuth.getAccessToken(redirectURI, code, codeVerifier);
```

### 5. Device Flow OAuth

**Use Case**: Devices without web browsers (IoT, CLI tools)

```java
import com.coze.openapi.service.auth.DeviceOAuthClient;

String clientID = System.getenv("COZE_DEVICE_OAUTH_CLIENT_ID");

DeviceOAuthClient deviceOAuth = new DeviceOAuthClient.DeviceOAuthBuilder()
    .clientID(clientID)
    .baseURL("https://api.coze.com")
    .build();

// Step 1: Start device authorization
DeviceCodeResp deviceCode = deviceOAuth.startDeviceAuthorization();

// Step 2: Show user the verification URL and code
System.out.println("Go to: " + deviceCode.verificationUri);
System.out.println("Enter code: " + deviceCode.userCode);

// Step 3: Poll for token
OAuthResp tokenResp = deviceOAuth.getAccessToken(deviceCode.deviceCode);
```

## Core APIs

### Chat API

The Chat API provides both streaming and non-streaming chat capabilities.

#### Non-Streaming Chat

```java
import com.coze.openapi.client.chat.CreateChatReq;
import com.coze.openapi.client.chat.model.*;

// Basic chat request
CreateChatReq req = CreateChatReq.builder()
    .botID("your-bot-id")
    .userID("user-123")
    .messages(Arrays.asList(
        Message.buildUserQuestionText("What is machine learning?"),
        Message.buildAssistantText("Machine learning is...")
    ))
    .stream(false) // Non-streaming
    .build();

// Execute and poll for completion
ChatPoll result = coze.chat().createAndPoll(req);

// Access results
Chat chat = result.chat;
List<Message> messages = result.messages;

System.out.println("Status: " + chat.status);
System.out.println("Token usage: " + chat.usage.tokenCount);

for (Message message : messages) {
    System.out.println(message.role + ": " + message.content);
}
```

#### Streaming Chat

```java
import io.reactivex.Flowable;
import com.coze.openapi.client.chat.model.ChatEvent;
import com.coze.openapi.client.chat.model.ChatEventType;

CreateChatReq streamReq = CreateChatReq.builder()
    .botID("your-bot-id")
    .userID("user-123")
    .messages(Collections.singletonList(
        Message.buildUserQuestionText("Tell me a story")))
    .stream(true)
    .build();

Flowable<ChatEvent> stream = coze.chat().stream(streamReq);

stream.blockingForEach(event -> {
    switch (event.getEvent()) {
        case CONVERSATION_MESSAGE_DELTA:
            // Streaming message content
            System.out.print(event.getMessage().getContent());
            break;
        case CONVERSATION_CHAT_COMPLETED:
            // Chat completed
            System.out.println("\nCompleted. Token usage: " + 
                event.getChat().getUsage().getTokenCount());
            break;
        case ERROR:
            System.err.println("Error: " + event.getError());
            break;
    }
});
```

#### Chat with Images

```java
// Create message with image
Message imageMessage = Message.builder()
    .role(MessageRole.USER)
    .content("What do you see in this image?")
    .contentType(MessageContentType.OBJECT_STRING)
    .objectContent(Arrays.asList(
        MessageObjectString.buildText("What do you see in this image?"),
        MessageObjectString.buildImageByURL("https://example.com/image.jpg")
    ))
    .build();

CreateChatReq req = CreateChatReq.builder()
    .botID("your-bot-id")
    .userID("user-123")
    .messages(Collections.singletonList(imageMessage))
    .build();

ChatPoll result = coze.chat().createAndPoll(req);
```

#### Tool/Plugin Integration

```java
// Handle tool calls in chat
CreateChatReq req = CreateChatReq.builder()
    .botID("your-bot-id")
    .userID("user-123")
    .messages(Collections.singletonList(
        Message.buildUserQuestionText("What's the weather like?")))
    .build();

// If the bot uses tools, you might need to handle tool outputs
SubmitToolOutputsReq toolOutputReq = SubmitToolOutputsReq.builder()
    .toolOutputs(Arrays.asList(
        ToolOutput.builder()
            .toolCallId("call_123")
            .output("Weather: 22°C, sunny")
            .build()
    ))
    .build();

Flowable<ChatEvent> stream = coze.chat().streamSubmitToolOutputs(
    "conversation-id", "chat-id", toolOutputReq);
```

### Bot Management API

#### Creating Bots

```java
import com.coze.openapi.client.bots.*;
import com.coze.openapi.client.bots.model.*;

// Step 1: Upload avatar
String avatarPath = "/path/to/avatar.jpg";
FileInfo avatarInfo = coze.files().upload(avatarPath);

// Step 2: Create bot configuration
BotPromptInfo promptInfo = new BotPromptInfo(
    "You are a helpful AI assistant specialized in Java programming.");

BotOnboardingInfo onboardingInfo = BotOnboardingInfo.builder()
    .prologue("Hello! I'm here to help you with Java programming questions.")
    .suggestedQuestions(Arrays.asList(
        "How do I create a ArrayList in Java?",
        "What's the difference between == and equals()?",
        "How to handle exceptions in Java?"
    ))
    .build();

// Step 3: Create the bot
CreateBotReq createReq = CreateBotReq.builder()
    .spaceID("your-workspace-id")
    .name("Java Programming Assistant")
    .description("An AI assistant that helps with Java programming")
    .promptInfo(promptInfo)
    .onboardingInfo(onboardingInfo)
    .iconFileID(avatarInfo.getID())
    .build();

CreateBotResp createResp = coze.bots().create(createReq);
String botID = createResp.getBotID();

// Step 4: Publish the bot
PublishBotReq publishReq = PublishBotReq.of(botID);
PublishBotResp publishResp = coze.bots().publish(publishReq);

System.out.println("Bot published with ID: " + botID);
```

#### Retrieving and Listing Bots

```java
// Get a specific bot
RetrieveBotReq retrieveReq = RetrieveBotReq.of("bot-id");
Bot bot = coze.bots().retrieve(retrieveReq);

System.out.println("Bot Name: " + bot.name);
System.out.println("Description: " + bot.description);

// List all bots in a workspace
ListBotReq listReq = ListBotReq.builder()
    .spaceID("workspace-id")
    .pageIndex(1)
    .pageSize(20)
    .build();

ListBotResp listResp = coze.bots().list(listReq);
for (Bot listedBot : listResp.bots) {
    System.out.println("Bot: " + listedBot.name + " (ID: " + listedBot.botID + ")");
}
```

#### Updating Bots

```java
// Update bot configuration
UpdateBotReq updateReq = UpdateBotReq.builder()
    .botID("your-bot-id")
    .name("Updated Bot Name")
    .description("Updated description")
    .promptInfo(new BotPromptInfo("Updated system prompt"))
    .build();

coze.bots().update(updateReq);

// Republish after updates
coze.bots().publish(PublishBotReq.of("your-bot-id"));
```

### Conversation Management API

#### Creating and Managing Conversations

```java
import com.coze.openapi.client.connversations.*;
import com.coze.openapi.client.connversations.model.*;

// Create a new conversation
CreateConversationReq createReq = new CreateConversationReq();
Conversation conversation = coze.conversations().create(createReq);
String conversationID = conversation.getId();

// Retrieve conversation details
RetrieveConversationReq retrieveReq = RetrieveConversationReq.of(conversationID);
Conversation retrieved = coze.conversations().retrieve(retrieveReq);

// List conversations
ListConversationReq listReq = ListConversationReq.builder()
    .limit(10)
    .build();

PageResult<Conversation> conversations = coze.conversations().list(listReq);
Iterator<Conversation> iterator = conversations.getIterator();
iterator.forEachRemaining(conv -> 
    System.out.println("Conversation: " + conv.getId()));
```

#### Message Management

```java
// Add message to conversation
CreateMessageReq messageReq = CreateMessageReq.builder()
    .conversationID(conversationID)
    .content("Hello, this is a test message")
    .contentType(MessageContentType.TEXT)
    .role(MessageRole.USER)
    .build();

Message message = coze.conversations().messages().create(messageReq);

// Add message with mixed content (text + image)
CreateMessageReq mixedContentReq = CreateMessageReq.builder()
    .conversationID(conversationID)
    .objectContent(Arrays.asList(
        MessageObjectString.buildText("Look at this image:"),
        MessageObjectString.buildImageByURL("https://example.com/image.jpg"),
        MessageObjectString.buildFileByURL("https://example.com/document.pdf")
    ))
    .build();

Message mixedMessage = coze.conversations().messages().create(mixedContentReq);

// Retrieve a specific message
RetrieveMessageReq retrieveMsgReq = RetrieveMessageReq.builder()
    .conversationID(conversationID)
    .messageID(message.getId())
    .build();

Message retrievedMessage = coze.conversations().messages().retrieve(retrieveMsgReq);

// Update message
UpdateMessageReq updateReq = UpdateMessageReq.builder()
    .conversationID(conversationID)
    .messageID(message.getId())
    .content("Updated message content")
    .contentType(MessageContentType.TEXT)
    .build();

Message updatedMessage = coze.conversations().messages().update(updateReq);

// Delete message
DeleteMessageReq deleteReq = DeleteMessageReq.builder()
    .conversationID(conversationID)
    .messageID(message.getId())
    .build();

coze.conversations().messages().delete(deleteReq);

// List messages with pagination
ListMessageReq listMsgReq = ListMessageReq.builder()
    .conversationID(conversationID)
    .limit(20)
    .build();

PageResult<Message> messages = coze.conversations().messages().list(listMsgReq);
Iterator<Message> msgIterator = messages.getIterator();
msgIterator.forEachRemaining(msg -> 
    System.out.println(msg.getRole() + ": " + msg.getContent()));
```

## Advanced Features

### File Management

```java
import com.coze.openapi.client.files.model.FileInfo;

// Upload file from path
String filePath = "/path/to/document.pdf";
FileInfo fileInfo = coze.files().upload(filePath);

// Upload file from byte array
byte[] fileData = Files.readAllBytes(Paths.get(filePath));
FileInfo fileInfo2 = coze.files().upload(fileData);

// Upload with specific filename
FileInfo fileInfo3 = coze.files().upload(fileData, "custom-name.pdf");

// Retrieve file information
FileInfo retrieved = coze.files().retrieve(fileInfo.getID());
System.out.println("File: " + retrieved.getFileName());
System.out.println("Size: " + retrieved.getBytes() + " bytes");
System.out.println("Type: " + retrieved.getContentType());
```

### Dataset Management

```java
import com.coze.openapi.client.dataset.*;
import com.coze.openapi.client.dataset.model.*;

// Create a dataset
CreateDatasetReq datasetReq = CreateDatasetReq.builder()
    .name("My Knowledge Base")
    .description("Collection of technical documentation")
    .build();

Dataset dataset = coze.datasets().create(datasetReq);
Long datasetID = Long.parseLong(dataset.getDatasetID());

// Add documents to dataset
CreateDocumentReq docReq = CreateDocumentReq.builder()
    .datasetID(datasetID)
    .documentBases(Arrays.asList(
        // From web page
        DocumentBase.buildWebPage("API Documentation", "https://docs.example.com"),
        // From local file
        DocumentBase.buildLocalFile("Manual", "File content here", "txt")
    ))
    .build();

CreateDocumentResp docResp = coze.datasets().documents().create(docReq);

// List documents
ListDocumentReq listDocReq = ListDocumentReq.builder()
    .datasetID(datasetID)
    .page(1)
    .size(10)
    .build();

PageResult<Document> documents = coze.datasets().documents().list(listDocReq);

// Update document
Long documentID = Long.parseLong(docResp.getDocumentInfos().get(0).getDocumentID());
UpdateDocumentReq updateDocReq = UpdateDocumentReq.builder()
    .documentID(documentID)
    .documentName("Updated Document Name")
    .build();

coze.datasets().documents().update(updateDocReq);
```

### Workflow Integration

#### Simple Workflow Execution

```java
import com.coze.openapi.client.workflows.*;
import com.coze.openapi.client.workflows.model.*;

// Prepare workflow parameters
Map<String, Object> parameters = new HashMap<>();
parameters.put("input_text", "Hello, world!");
parameters.put("max_length", 100);

// Execute workflow synchronously
RunWorkflowReq workflowReq = RunWorkflowReq.builder()
    .workflowID("your-workflow-id")
    .parameters(parameters)
    .build();

RunWorkflowResp response = coze.workflows().runs().run(workflowReq);
System.out.println("Workflow result: " + response.getData());
```

#### Streaming Workflow Execution

```java
// Execute workflow with streaming
Flowable<WorkflowEvent> stream = coze.workflows().runs().stream(workflowReq);

stream.blockingForEach(event -> {
    switch (event.getEvent()) {
        case WORKFLOW_STARTED:
            System.out.println("Workflow started");
            break;
        case WORKFLOW_COMPLETED:
            System.out.println("Workflow completed: " + event.getData());
            break;
        case WORKFLOW_INTERRUPTED:
            // Handle Q&A or user input requirements
            System.out.println("Workflow needs input: " + event.getInterruptData());
            break;
        case ERROR:
            System.err.println("Workflow error: " + event.getError());
            break;
    }
});
```

#### Workflow with Interactive Elements

```java
// Handle workflows that require user interaction
public void handleInteractiveWorkflow(String workflowID) {
    RunWorkflowReq req = RunWorkflowReq.builder()
        .workflowID(workflowID)
        .parameters(Collections.emptyMap())
        .build();
    
    Flowable<WorkflowEvent> stream = coze.workflows().runs().stream(req);
    
    stream.blockingForEach(event -> {
        if (event.getEvent() == WorkflowEventType.WORKFLOW_INTERRUPTED) {
            // Workflow is waiting for user input
            String question = event.getInterruptData().getQuestion();
            System.out.println("Question: " + question);
            
            // Get user input (this is just an example)
            String userResponse = getUserInput();
            
            // Resume workflow with user's response
            ResumeWorkflowReq resumeReq = ResumeWorkflowReq.builder()
                .answer(userResponse)
                .build();
            
            // Continue with the resumed stream
            Flowable<WorkflowEvent> resumedStream = coze.workflows().runs()
                .resume(event.getWorkflowRunID(), resumeReq);
            
            // Process the resumed stream recursively
            handleWorkflowStream(resumedStream);
        }
    });
}
```

### Audio APIs

#### Speech Synthesis

```java
import com.coze.openapi.client.audio.speech.*;
import com.coze.openapi.client.audio.speech.model.*;

// Text-to-speech
CreateSpeechReq speechReq = CreateSpeechReq.builder()
    .input("Hello, this is a test of speech synthesis.")
    .voiceID("voice-id")  // Get from voice list API
    .format(AudioFormat.MP3)
    .speed(1.0)
    .build();

SpeechResp speechResp = coze.audio().speech().create(speechReq);

// Save audio to file
Files.write(Paths.get("output.mp3"), speechResp.getAudioData());
```

#### Speech Transcription

```java
import com.coze.openapi.client.audio.transcriptions.*;

// Audio-to-text
String audioFilePath = "/path/to/audio.mp3";
CreateTranscriptionReq transcriptionReq = CreateTranscriptionReq.builder()
    .file(audioFilePath)
    .format(TranscriptionFormat.JSON)
    .build();

TranscriptionResp transcriptionResp = coze.audio().transcriptions()
    .create(transcriptionReq);

System.out.println("Transcription: " + transcriptionResp.getText());
```

#### Voice Management

```java
// List available voices
ListVoiceReq listVoiceReq = ListVoiceReq.builder()
    .pageIndex(1)
    .pageSize(20)
    .build();

ListVoiceResp voices = coze.audio().voices().list(listVoiceReq);
for (Voice voice : voices.getVoices()) {
    System.out.println("Voice: " + voice.getName() + " (" + voice.getLanguage() + ")");
}

// Clone a voice
CloneVoiceReq cloneReq = CloneVoiceReq.builder()
    .name("My Custom Voice")
    .audioFile("/path/to/sample.wav")
    .language("en")
    .build();

CloneVoiceResp cloneResp = coze.audio().voices().clone(cloneReq);
String newVoiceID = cloneResp.getVoiceID();
```

## WebSocket Support

The SDK provides WebSocket support for real-time communication.

### WebSocket Chat

```java
import com.coze.openapi.service.service.websocket.chat.*;

// Create WebSocket chat client
WebsocketsChatClient wsChat = coze.websockets().chat()
    .builder()
    .auth(auth)
    .build();

// Create chat request
WebsocketsChatCreateReq wsReq = WebsocketsChatCreateReq.builder()
    .botID("your-bot-id")
    .userID("user-123")
    .messages(Collections.singletonList(
        Message.buildUserQuestionText("Hello via WebSocket!")))
    .build();

// Connect and chat
wsChat.chat(wsReq, new ChatCallback() {
    @Override
    public void onMessage(ChatEvent event) {
        System.out.println("Received: " + event.getMessage().getContent());
    }
    
    @Override
    public void onError(String error) {
        System.err.println("WebSocket error: " + error);
    }
    
    @Override
    public void onComplete() {
        System.out.println("Chat completed");
    }
});
```

### WebSocket Audio

```java
// Real-time speech synthesis
WebsocketsAudioSpeechClient wsSpeech = coze.websockets().audio()
    .speechBuilder()
    .auth(auth)
    .build();

WebsocketsAudioSpeechCreateReq speechReq = WebsocketsAudioSpeechCreateReq.builder()
    .text("Hello, this is real-time speech")
    .voiceID("voice-id")
    .build();

wsSpeech.synthesize(speechReq, new AudioCallback() {
    @Override
    public void onAudioData(byte[] audioData) {
        // Play or save audio data
        playAudio(audioData);
    }
});

// Real-time transcription
WebsocketsAudioTranscriptionsClient wsTranscription = coze.websockets().audio()
    .transcriptionsBuilder()
    .auth(auth)
    .build();

WebsocketsAudioTranscriptionsCreateReq transcribeReq = 
    WebsocketsAudioTranscriptionsCreateReq.builder()
        .format(AudioFormat.PCM)
        .sampleRate(16000)
        .build();

wsTranscription.transcribe(transcribeReq, new TranscriptionCallback() {
    @Override
    public void onTranscription(String text) {
        System.out.println("Transcribed: " + text);
    }
});
```

## Error Handling

The SDK provides comprehensive error handling with specific exception types.

### Exception Hierarchy

```java
try {
    ChatPoll result = coze.chat().createAndPoll(chatReq);
} catch (CozeAPIException e) {
    // General API errors
    System.err.println("API Error: " + e.getMessage());
    System.err.println("Error Code: " + e.getCode());
} catch (AuthenticationException e) {
    // Authentication failures
    System.err.println("Auth Error: " + e.getMessage());
} catch (RateLimitException e) {
    // Rate limiting
    System.err.println("Rate limited. Retry after: " + e.getRetryAfter());
} catch (ValidationException e) {
    // Request validation errors
    System.err.println("Validation Error: " + e.getMessage());
} catch (IOException e) {
    // Network errors
    System.err.println("Network Error: " + e.getMessage());
}
```

### Retry Logic

```java
// Implement retry logic for transient errors
public <T> T executeWithRetry(Supplier<T> operation, int maxRetries) {
    int attempt = 0;
    while (attempt < maxRetries) {
        try {
            return operation.get();
        } catch (RateLimitException e) {
            // Wait for the specified retry-after period
            sleep(e.getRetryAfter() * 1000);
            attempt++;
        } catch (IOException e) {
            // Retry network errors with exponential backoff
            sleep((long) Math.pow(2, attempt) * 1000);
            attempt++;
        }
    }
    throw new RuntimeException("Operation failed after " + maxRetries + " retries");
}

// Usage
ChatPoll result = executeWithRetry(() -> coze.chat().createAndPoll(req), 3);
```

### Request Logging and Debugging

```java
// Enable detailed logging
Logger logger = LoggerFactory.getLogger("CozeSDK");
CozeAPI coze = new CozeAPI.Builder()
    .auth(auth)
    .logger(logger)
    .build();

// Get request ID for debugging
try {
    ChatPoll result = coze.chat().createAndPoll(req);
} catch (CozeAPIException e) {
    String requestId = e.getRequestId();
    logger.error("Request failed with ID: " + requestId, e);
}
```

## Configuration and Customization

### Timeout Configuration

```java
// Configure various timeouts
CozeAPI coze = new CozeAPI.Builder()
    .auth(auth)
    .readTimeout(30000)    // 30 seconds for read operations
    .connectTimeout(10000) // 10 seconds for connection
    .build();

// Per-request timeout using custom HTTP client
OkHttpClient customClient = new OkHttpClient.Builder()
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .connectTimeout(30, TimeUnit.SECONDS)
    .build();

CozeAPI customCoze = new CozeAPI.Builder()
    .auth(auth)
    .client(customClient)
    .build();
```

### Custom HTTP Client

```java
// Use custom HTTP client with interceptors
OkHttpClient customClient = new OkHttpClient.Builder()
    .addInterceptor(new LoggingInterceptor())
    .addInterceptor(new RetryInterceptor())
    .addNetworkInterceptor(new NetworkInterceptor())
    .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
    .build();

CozeAPI coze = new CozeAPI.Builder()
    .auth(auth)
    .client(customClient)
    .build();
```

## Best Practices

### 1. Authentication Management

```java
// Use environment variables for sensitive data
public class CozeConfig {
    public static CozeAPI createClient() {
        String token = System.getenv("COZE_API_TOKEN");
        if (token == null) {
            throw new IllegalStateException("COZE_API_TOKEN environment variable not set");
        }
        
        return new CozeAPI.Builder()
            .auth(new TokenAuth(token))
            .baseURL(System.getenv("COZE_API_BASE"))
            .readTimeout(30000)
            .build();
    }
}
```

### 2. Resource Management

```java
// Properly manage resources
public class ChatManager implements AutoCloseable {
    private final CozeAPI coze;
    
    public ChatManager() {
        this.coze = CozeConfig.createClient();
    }
    
    public void performChat() {
        // Chat operations
    }
    
    @Override
    public void close() {
        // Clean up resources
        coze.shutdownExecutor();
    }
}

// Usage with try-with-resources
try (ChatManager chatManager = new ChatManager()) {
    chatManager.performChat();
}
```

### 3. Pagination Handling

```java
// Efficient pagination handling
public List<Bot> getAllBots(String spaceID) {
    List<Bot> allBots = new ArrayList<>();
    int pageIndex = 1;
    int pageSize = 50;
    
    while (true) {
        ListBotReq req = ListBotReq.builder()
            .spaceID(spaceID)
            .pageIndex(pageIndex)
            .pageSize(pageSize)
            .build();
        
        ListBotResp response = coze.bots().list(req);
        allBots.addAll(response.bots);
        
        if (response.bots.size() < pageSize) {
            break; // Last page
        }
        pageIndex++;
    }
    
    return allBots;
}

// Or use the built-in iterator
PageResult<Bot> pageResult = coze.bots().list(req);
Iterator<Bot> iterator = pageResult.getIterator();
iterator.forEachRemaining(bot -> processBot(bot));
```

### 4. Async Operations

```java
// Use CompletableFuture for async operations
public CompletableFuture<ChatPoll> chatAsync(CreateChatReq req) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            return coze.chat().createAndPoll(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });
}

// Multiple concurrent chats
List<CompletableFuture<ChatPoll>> futures = requests.stream()
    .map(this::chatAsync)
    .collect(Collectors.toList());

CompletableFuture<List<ChatPoll>> allChats = CompletableFuture.allOf(
    futures.toArray(new CompletableFuture[0]))
    .thenApply(v -> futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList()));
```

## API Reference Summary

### Main Client

- `CozeAPI` - Main entry point
- `CozeAPI.Builder` - Builder for configuring the client

### Authentication

- `TokenAuth` - Personal Access Token authentication
- `WebOAuthClient` - Web OAuth flow
- `JWTOAuthClient` - JWT-based OAuth
- `PKCEOAuthClient` - PKCE OAuth flow
- `DeviceOAuthClient` - Device authorization flow

### Core Services

- `ChatService` - Chat operations (streaming/non-streaming)
- `BotService` - Bot management (create, update, publish, list)
- `ConversationService` - Conversation and message management
- `FileService` - File upload and management
- `DatasetService` - Knowledge base management
- `WorkflowService` - Workflow execution
- `AudioService` - Speech synthesis and transcription
- `WorkspaceService` - Workspace operations
- `TemplateService` - Template management
- `WebsocketsClient` - Real-time communication

### Data Models

All request and response objects are strongly typed with builder patterns for easy construction.

---

This comprehensive documentation covers all major features and APIs provided by the Coze Java SDK. For specific implementation details, refer to the individual API classes and the extensive example code provided in the `example/` module.