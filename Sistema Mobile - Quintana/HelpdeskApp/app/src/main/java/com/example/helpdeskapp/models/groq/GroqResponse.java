package com.example.helpdeskapp.models.groq;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GroqResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("choices")
    private List<Choice> choices;

    @SerializedName("usage")
    private Usage usage;

    public String getId() { return id; }
    public List<Choice> getChoices() { return choices; }
    public Usage getUsage() { return usage; }

    public static class Choice {
        @SerializedName("message")
        private Message message;

        @SerializedName("finish_reason")
        private String finishReason;

        @SerializedName("index")
        private int index;

        public Message getMessage() { return message; }
        public String getFinishReason() { return finishReason; }
        public int getIndex() { return index; }
    }

    public static class Message {
        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        public String getRole() { return role; }
        public String getContent() { return content; }
    }

    public static class Usage {
        @SerializedName("prompt_tokens")
        private int promptTokens;

        @SerializedName("completion_tokens")
        private int completionTokens;

        @SerializedName("total_tokens")
        private int totalTokens;

        public int getPromptTokens() { return promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public int getTotalTokens() { return totalTokens; }
    }
}