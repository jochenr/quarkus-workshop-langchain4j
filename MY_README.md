# Meine Zusatz-Dokumentation

Im original Workshop/Tutorial wird "OpenAI’s gpt-4o model" verwendet.

Weil ich in Zukunft, aus Datenschutz, etc. Gründen, keien Cloud-AI nutzen möchte, passe ich das Tutorial auch gleich so an, dass es mit in Container gehostetem Ollama läuft.

## Ollama lokal

### Download & install

 * https://github.com/ollama/ollama
 * https://github.com/ollama/ollama/releases

Datei "ollama-windows-amd64.zip" herunterladen und in Zielordner entpacken.  
Die Modelle landen trotzdem standardmäßig im Benutzerprofil, nicht neben der EXE. Unter Windows typischerweise in:  

C:\Users\<Benutzer>\.ollama\models  

Speicherort kann über die Umgebungsvariable  

OLLAMA_MODELS  

geändert werden.  

### Start  

.\ollama.exe serve  

In einem zweiten Terminal:  
.\ollama.exe pull qwen3:0.6b  
das Modell herunterladen  
  
Zur Verfügung stehende Modelle anzeigen:  
.\ollama.exe list  




## Ollama in Podman  

ACHTUNG: "localhost" funktioniert nur per IPv6:  
http://[::1]:11434/v1

### Image holen
```powershell
podman pull docker.io/ollama/ollama:latest
```

### CPU-only starten

```powershell
podman run -d --name ollama -p 11434:11434 -v ollama:/root/.ollama docker.io/ollama/ollama:latest

# wieder stoppen
podman stop ollama
# wieder starten
podman start ollama

# container wieder löschen
podman rm -f ollama
```


### Test

```powershell
curl http://localhost:11434/api/tags
```


### Modell herunterladen

```powershell
podman exec -it ollama ollama pull qwen3:14b
# oder
podman exec -it ollama ollama pull qwen3:8b
# oder
podman exec -it ollama ollama pull qwen3:0.6b
```


### Liste der Modelle

```powershell
podman exec -it ollama ollama list
```


### Modell verwenden

```powershell
# Direkt im Container:
podman exec -it ollama ollama run qwen3:14b
# oder
podman exec -it ollama ollama run qwen3:8b
# oder
podman exec -it ollama ollama run qwen3:0.6b

# oder per REST API:
curl http://localhost:11434/api/generate -d "{\"model\":\"qwen3:14b\",\"prompt\":\"Hallo\"}"
# oder
curl http://localhost:11434/api/generate -d "{\"model\":\"qwen3:8b\",\"prompt\":\"Hallo\"}"
# oder
curl http://localhost:11434/api/generate -d "{\"model\":\"qwen3:0.6b\",\"prompt\":\"Hallo\"}"
```

### alternative kleine Modelle / Somstiges

podman exec -it ollama ollama pull qwen3:0.6b
curl http://localhost:11434/api/generate -d '{"model":"qwen3:0.6b","prompt":"Hallo"}'

### Embedding Modell herunterladen (für RAG)

```powershell
podman exec -it ollama ollama pull nomic-embed-text
```


## Quarkus App auf Ollama umkonfigurieren

In der application.properties folgende Änderungen machen:

```
quarkus.langchain4j.openai.api-key=${OPENAI_API_KEY:egal-for-local-llm}
quarkus.langchain4j.openai.chat-model.model-name=qwen3:8b
quarkus.langchain4j.openai.base-url=http://localhost:11434/v1
```
