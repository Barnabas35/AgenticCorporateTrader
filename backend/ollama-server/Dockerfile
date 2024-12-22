# Start from the official Ollama image
FROM ollama/ollama:latest

# Temporarily start the Ollama server to pull the model
RUN ollama serve & \
    sleep 2 && \
    ollama pull llama3.2 && \
    ollama pull llama3.1

# Expose the default port
EXPOSE 11434

# Run Ollama on container startup
#CMD ["ollama", "run llama3.2"]
