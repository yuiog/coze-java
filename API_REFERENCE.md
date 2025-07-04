# Coze Java SDK - API Reference

This document provides a comprehensive reference for all public APIs, classes, and interfaces in the Coze Java SDK.

## Core Classes

### CozeAPI

The main entry point for the Coze API SDK.

```java
public class CozeAPI
```

#### Constructor

```java
// Use CozeAPI.Builder to create instances
CozeAPI coze = new CozeAPI.Builder()
    .auth(auth)
    .baseURL("https://api.coze.com")
    .build();
```

#### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `workspaces()` | `WorkspaceService` | Access workspace operations |
| `bots()` | `BotService` | Access bot management operations |
| `conversations()` | `ConversationService` | Access conversation operations |
| `files()` | `FileService` | Access file operations |
| `datasets()` | `DatasetService` | Access dataset/knowledge base operations |
| `workflows()` | `WorkflowService` | Access workflow operations |
| `chat()` | `ChatService` | Access chat operations |
| `audio()` | `AudioService` | Access audio operations |
| `templates()` | `TemplateService` | Access template operations |
| `websockets()` | `WebsocketsClient` | Access WebSocket operations |
| `shutdownExecutor()` | `void` | Clean up resources |

#### CozeAPI.Builder

Builder class for configuring the CozeAPI client.

```java
public static class CozeAPI.Builder
```

| Method | Parameter Type | Description |
|--------|---------------|-------------|
| `auth(Auth auth)` | `Auth` | Set authentication method (required) |
| `baseURL(String url)` | `String` | Set API base URL (default: api.coze.com) |
| `client(OkHttpClient client)` | `OkHttpClient` | Set custom HTTP client |
| `readTimeout(int timeout)` | `int` | Set read timeout in milliseconds |
| `connectTimeout(int timeout)` | `int` | Set connection timeout in milliseconds |
| `logger(Logger logger)` | `Logger` | Set custom logger |
| `build()` | `CozeAPI` | Build the client instance |

## Authentication Classes

### TokenAuth

Personal Access Token authentication.

```java
public class TokenAuth extends Auth
```

#### Constructor

```java
TokenAuth auth = new TokenAuth("your-access-token");
```

### WebOAuthClient

Web-based OAuth authentication flow.

```java
public class WebOAuthClient
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `getAuthURL(String redirectURI, String state)` | `String, String` | `String` | Generate authorization URL |
| `getAccessToken(String redirectURI, String code)` | `String, String` | `OAuthResp` | Exchange code for token |
| `refreshToken(String refreshToken)` | `String` | `OAuthResp` | Refresh access token |

#### WebOAuthClient.WebOAuthBuilder

```java
WebOAuthClient oauth = new WebOAuthClient.WebOAuthBuilder()
    .clientID("client-id")
    .clientSecret("client-secret")
    .baseURL("https://api.coze.com")
    .build();
```

### JWTOAuthClient

JWT-based OAuth authentication.

```java
public class JWTOAuthClient
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `getAccessToken()` | - | `OAuthResp` | Get access token using JWT |

#### JWTBuilder Interface

```java
public interface JWTBuilder {
    String build();
}
```

### PKCEOAuthClient

PKCE OAuth flow for mobile and single-page applications.

```java
public class PKCEOAuthClient
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `generateCodeVerifier()` | - | `String` | Generate PKCE code verifier |
| `generateCodeChallenge(String verifier)` | `String` | `String` | Generate code challenge |
| `getAuthURL(String redirectURI, String challenge, String state)` | `String, String, String` | `String` | Generate auth URL |
| `getAccessToken(String redirectURI, String code, String verifier)` | `String, String, String` | `OAuthResp` | Exchange code for token |

### DeviceOAuthClient

Device authorization flow for headless devices.

```java
public class DeviceOAuthClient
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `startDeviceAuthorization()` | - | `DeviceCodeResp` | Start device auth flow |
| `getAccessToken(String deviceCode)` | `String` | `OAuthResp` | Poll for access token |

## Chat API

### ChatService

Main service for chat operations.

```java
public class ChatService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `create(CreateChatReq req)` | `CreateChatReq` | `Chat` | Create a chat (non-blocking) |
| `createAndPoll(CreateChatReq req)` | `CreateChatReq` | `ChatPoll` | Create chat and poll until completion |
| `stream(CreateChatReq req)` | `CreateChatReq` | `Flowable<ChatEvent>` | Create streaming chat |
| `retrieve(RetrieveChatReq req)` | `RetrieveChatReq` | `Chat` | Retrieve chat by ID |
| `cancel(CancelChatReq req)` | `CancelChatReq` | `Chat` | Cancel ongoing chat |
| `submitToolOutputs(SubmitToolOutputsReq req)` | `SubmitToolOutputsReq` | `Chat` | Submit tool outputs |
| `streamSubmitToolOutputs(SubmitToolOutputsReq req)` | `SubmitToolOutputsReq` | `Flowable<ChatEvent>` | Submit tool outputs (streaming) |

### Request Classes

#### CreateChatReq

```java
public class CreateChatReq
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `botID` | `String` | Yes | Bot identifier |
| `userID` | `String` | Yes | User identifier |
| `messages` | `List<Message>` | Yes | Chat messages |
| `stream` | `Boolean` | No | Enable streaming (default: false) |
| `customVariables` | `Map<String, String>` | No | Custom variables |
| `autoSaveHistory` | `Boolean` | No | Auto-save chat history |
| `additionalMessages` | `List<Message>` | No | Additional context messages |

#### Message

```java
public class Message
```

| Field | Type | Description |
|-------|------|-------------|
| `role` | `MessageRole` | Message role (USER, ASSISTANT, SYSTEM) |
| `content` | `String` | Message content |
| `contentType` | `MessageContentType` | Content type (TEXT, OBJECT_STRING) |
| `objectContent` | `List<MessageObjectString>` | Structured content for multimodal |

#### Static Factory Methods

```java
// Text message
Message.buildUserQuestionText(String content)
Message.buildAssistantText(String content)
Message.buildSystemText(String content)

// Multimodal content
MessageObjectString.buildText(String text)
MessageObjectString.buildImageByURL(String url)
MessageObjectString.buildFileByURL(String url)
```

### Response Classes

#### Chat

```java
public class Chat
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Chat identifier |
| `conversationID` | `String` | Conversation identifier |
| `botID` | `String` | Bot identifier |
| `status` | `ChatStatus` | Chat status |
| `createdAt` | `Long` | Creation timestamp |
| `completedAt` | `Long` | Completion timestamp |
| `usage` | `Usage` | Token usage information |

#### ChatPoll

```java
public class ChatPoll
```

| Field | Type | Description |
|-------|------|-------------|
| `chat` | `Chat` | Chat information |
| `messages` | `List<Message>` | All messages in the chat |

#### ChatEvent

```java
public class ChatEvent
```

| Field | Type | Description |
|-------|------|-------------|
| `event` | `ChatEventType` | Event type |
| `message` | `Message` | Message data (for message events) |
| `chat` | `Chat` | Chat data (for chat events) |
| `error` | `String` | Error information |

### Enums

#### ChatStatus

```java
public enum ChatStatus {
    CREATED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    REQUIRES_ACTION
}
```

#### ChatEventType

```java
public enum ChatEventType {
    CONVERSATION_MESSAGE_DELTA,
    CONVERSATION_CHAT_CREATED,
    CONVERSATION_CHAT_IN_PROGRESS,
    CONVERSATION_CHAT_COMPLETED,
    CONVERSATION_CHAT_FAILED,
    CONVERSATION_CHAT_REQUIRES_ACTION,
    ERROR,
    DONE
}
```

#### MessageRole

```java
public enum MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}
```

#### MessageContentType

```java
public enum MessageContentType {
    TEXT,
    OBJECT_STRING
}
```

## Bot Management API

### BotService

```java
public class BotService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `create(CreateBotReq req)` | `CreateBotReq` | `CreateBotResp` | Create a new bot |
| `update(UpdateBotReq req)` | `UpdateBotReq` | `void` | Update bot configuration |
| `publish(PublishBotReq req)` | `PublishBotReq` | `PublishBotResp` | Publish bot to API channel |
| `retrieve(RetrieveBotReq req)` | `RetrieveBotReq` | `Bot` | Get bot information |
| `list(ListBotReq req)` | `ListBotReq` | `ListBotResp` | List bots in workspace |

### Request Classes

#### CreateBotReq

```java
public class CreateBotReq
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `spaceID` | `String` | Yes | Workspace identifier |
| `name` | `String` | Yes | Bot name |
| `description` | `String` | No | Bot description |
| `promptInfo` | `BotPromptInfo` | Yes | Bot prompt configuration |
| `onboardingInfo` | `BotOnboardingInfo` | No | Onboarding configuration |
| `iconFileID` | `String` | No | Avatar file ID |

#### BotPromptInfo

```java
public class BotPromptInfo
```

| Field | Type | Description |
|-------|------|-------------|
| `prompt` | `String` | System prompt for the bot |

#### BotOnboardingInfo

```java
public class BotOnboardingInfo
```

| Field | Type | Description |
|-------|------|-------------|
| `prologue` | `String` | Welcome message |
| `suggestedQuestions` | `List<String>` | Suggested questions for users |

### Response Classes

#### Bot

```java
public class Bot
```

| Field | Type | Description |
|-------|------|-------------|
| `botID` | `String` | Bot identifier |
| `name` | `String` | Bot name |
| `description` | `String` | Bot description |
| `iconURL` | `String` | Bot avatar URL |
| `publishTime` | `String` | Publication timestamp |

## Conversation API

### ConversationService

```java
public class ConversationService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `create(CreateConversationReq req)` | `CreateConversationReq` | `Conversation` | Create conversation |
| `retrieve(RetrieveConversationReq req)` | `RetrieveConversationReq` | `Conversation` | Get conversation |
| `list(ListConversationReq req)` | `ListConversationReq` | `PageResult<Conversation>` | List conversations |
| `clear(ClearConversationReq req)` | `ClearConversationReq` | `ClearConversationResult` | Clear conversation history |
| `messages()` | - | `MessageService` | Access message operations |

### MessageService

```java
public class MessageService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `create(CreateMessageReq req)` | `CreateMessageReq` | `Message` | Add message to conversation |
| `retrieve(RetrieveMessageReq req)` | `RetrieveMessageReq` | `Message` | Get specific message |
| `update(UpdateMessageReq req)` | `UpdateMessageReq` | `Message` | Update message content |
| `delete(DeleteMessageReq req)` | `DeleteMessageReq` | `Message` | Delete message |
| `list(ListMessageReq req)` | `ListMessageReq` | `PageResult<Message>` | List messages in conversation |

## File API

### FileService

```java
public class FileService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `upload(String filePath)` | `String` | `FileInfo` | Upload file from path |
| `upload(byte[] data)` | `byte[]` | `FileInfo` | Upload file from byte array |
| `upload(byte[] data, String filename)` | `byte[], String` | `FileInfo` | Upload with custom filename |
| `retrieve(String fileID)` | `String` | `FileInfo` | Get file information |

### FileInfo

```java
public class FileInfo
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | File identifier |
| `fileName` | `String` | Original filename |
| `bytes` | `Integer` | File size in bytes |
| `contentType` | `String` | MIME type |
| `uploadedAt` | `Long` | Upload timestamp |

## Dataset API

### DatasetService

```java
public class DatasetService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `create(CreateDatasetReq req)` | `CreateDatasetReq` | `Dataset` | Create knowledge base |
| `update(UpdateDatasetReq req)` | `UpdateDatasetReq` | `void` | Update knowledge base |
| `delete(DeleteDatasetReq req)` | `DeleteDatasetReq` | `void` | Delete knowledge base |
| `list(ListDatasetReq req)` | `ListDatasetReq` | `PageResult<Dataset>` | List knowledge bases |
| `documents()` | - | `DocumentService` | Access document operations |
| `images()` | - | `ImageService` | Access image operations |

### DocumentService

```java
public class DocumentService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `create(CreateDocumentReq req)` | `CreateDocumentReq` | `CreateDocumentResp` | Add documents to dataset |
| `update(UpdateDocumentReq req)` | `UpdateDocumentReq` | `void` | Update document |
| `delete(DeleteDocumentReq req)` | `DeleteDocumentReq` | `void` | Delete documents |
| `list(ListDocumentReq req)` | `ListDocumentReq` | `PageResult<Document>` | List documents |

### DocumentBase

Factory class for creating different types of documents.

```java
// Create from web page
DocumentBase.buildWebPage(String name, String url)

// Create from local file content
DocumentBase.buildLocalFile(String name, String content, String format)
```

## Workflow API

### WorkflowService

```java
public class WorkflowService
```

#### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `runs()` | `WorkflowRunService` | Access workflow run operations |
| `runHistory()` | `WorkflowRunHistoryService` | Access run history operations |
| `chat()` | `WorkflowChatService` | Access workflow chat operations |

### WorkflowRunService

```java
public class WorkflowRunService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `run(RunWorkflowReq req)` | `RunWorkflowReq` | `RunWorkflowResp` | Execute workflow synchronously |
| `stream(RunWorkflowReq req)` | `RunWorkflowReq` | `Flowable<WorkflowEvent>` | Execute workflow with streaming |
| `resume(String runID, ResumeWorkflowReq req)` | `String, ResumeWorkflowReq` | `Flowable<WorkflowEvent>` | Resume interrupted workflow |

## Audio API

### AudioService

```java
public class AudioService
```

#### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `speech()` | `SpeechService` | Access speech synthesis |
| `transcriptions()` | `TranscriptionService` | Access speech-to-text |
| `voices()` | `VoiceService` | Access voice management |
| `rooms()` | `RoomService` | Access audio rooms |

### SpeechService

```java
public class SpeechService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `create(CreateSpeechReq req)` | `CreateSpeechReq` | `SpeechResp` | Convert text to speech |

### TranscriptionService

```java
public class TranscriptionService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `create(CreateTranscriptionReq req)` | `CreateTranscriptionReq` | `TranscriptionResp` | Convert speech to text |

### VoiceService

```java
public class VoiceService
```

#### Methods

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `list(ListVoiceReq req)` | `ListVoiceReq` | `ListVoiceResp` | List available voices |
| `clone(CloneVoiceReq req)` | `CloneVoiceReq` | `CloneVoiceResp` | Clone a voice |

## WebSocket API

### WebsocketsClient

```java
public class WebsocketsClient
```

#### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `chat()` | `WebsocketsChatClient` | Access WebSocket chat |
| `audio()` | `WebsocketsAudioClient` | Access WebSocket audio |

### WebsocketsChatClient

```java
public class WebsocketsChatClient
```

#### Methods

| Method | Parameters | Description |
|--------|------------|-------------|
| `chat(WebsocketsChatCreateReq req, ChatCallback callback)` | `WebsocketsChatCreateReq, ChatCallback` | Start WebSocket chat |

### WebsocketsAudioClient

```java
public class WebsocketsAudioClient
```

#### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `speechBuilder()` | `WebsocketsAudioSpeechBuilder` | Build speech synthesis client |
| `transcriptionsBuilder()` | `WebsocketsAudioTranscriptionsBuilder` | Build transcription client |

## Pagination

### PageResult<T>

```java
public class PageResult<T>
```

#### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getItems()` | `List<T>` | Get current page items |
| `getIterator()` | `Iterator<T>` | Get iterator for all pages |
| `hasMore()` | `boolean` | Check if more pages available |

#### Usage Example

```java
// Manual pagination
ListBotReq req = ListBotReq.builder()
    .spaceID("workspace-id")
    .pageSize(20)
    .build();

PageResult<Bot> result = coze.bots().list(req);

// Iterate through all pages automatically
Iterator<Bot> iterator = result.getIterator();
iterator.forEachRemaining(bot -> System.out.println(bot.name));

// Or handle pages manually
List<Bot> currentPage = result.getItems();
while (result.hasMore()) {
    // Get next page...
}
```

## Error Handling

### Exception Hierarchy

```java
CozeAPIException                    // Base exception
├── AuthenticationException         // 401 errors
├── AuthorizationException         // 403 errors
├── RateLimitException            // 429 errors
├── ValidationException           // 400 errors
├── NotFoundException            // 404 errors
└── InternalServerException      // 5xx errors
```

### Exception Properties

All exceptions extend `CozeAPIException` and provide:

| Property | Type | Description |
|----------|------|-------------|
| `message` | `String` | Error message |
| `code` | `String` | Error code |
| `requestId` | `String` | Request identifier for debugging |
| `httpStatus` | `int` | HTTP status code |

### RateLimitException

```java
public class RateLimitException extends CozeAPIException
```

Additional properties:
- `retryAfter` - Seconds to wait before retrying

## Reactive Streams

The SDK uses RxJava's `Flowable<T>` for streaming operations.

### Common Patterns

```java
// Basic subscription
Flowable<ChatEvent> stream = coze.chat().stream(req);
stream.subscribe(
    event -> handleEvent(event),
    error -> handleError(error),
    () -> handleComplete()
);

// Blocking iteration
stream.blockingForEach(event -> handleEvent(event));

// Filtering events
stream.filter(event -> event.getEvent() == ChatEventType.CONVERSATION_MESSAGE_DELTA)
      .subscribe(event -> handleMessageDelta(event));

// Error handling
stream.doOnError(error -> logger.error("Stream error", error))
      .retry(3)
      .subscribe(event -> handleEvent(event));
```

## Constants

### Base URLs

```java
public class Consts {
    public static final String COZE_COM_BASE_URL = "https://api.coze.com";
    public static final String COZE_CN_BASE_URL = "https://api.coze.cn";
}
```

---

This API reference provides comprehensive coverage of all public APIs in the Coze Java SDK. For implementation examples and usage patterns, refer to the main documentation and example code.