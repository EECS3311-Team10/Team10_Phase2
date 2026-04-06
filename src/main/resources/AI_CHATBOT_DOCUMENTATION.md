• Description of Chatbot Functionality
The ConsultLink AI Assistant is a real-time, context-aware support interface integrated into the Client Dashboard. It serves as the primary touchpoint for users to navigate platform complexities without manual staff intervention. The chatbot is designed to be stateless, ensuring high availability and low latency by processing each request as an independent transactional unit.

• Examples of Questions
The Assistant is optimized to handle queries regarding the specific business logic defined in the platform's operational mandates:

Policy Queries: "How do I cancel a booking?"

General Info: "What services are currently offered on ConsultLink?"

• System Context Provided to the AI
The Assistant uses a Retrieval-Augmented Generation (RAG) Light architecture. Upon initialization, the system injects a specialized platform-policies.md markdown file into the LLM’s system prompt. This ensures:

Groundedness: The AI only answers based on ConsultLink’s specific 24-hour cancellation rules.

Persona Control: The AI is instructed to act as a professional, helpful "ConsultLink Assistant" and avoid answering off-topic or personal questions.

• Privacy and Safety Measures

PII Stripping: Users are advised not to share sensitive data; the backend does not persist chat logs to the database in this phase to maintain Zero-Data Retention.

API Security: The Groq API key is managed via Docker Environment Variables and is never exposed to the client-side (Frontend).

Prompt Injection Mitigation: The system prompt is hard-coded on the server-side, preventing users from "jailbreaking" the bot's instructions via the UI.

• API Integration Approach
The system follows a modern Decoupled Service Architecture:

Frontend: A vanilla JavaScript fetch call sends a JSON payload to a dedicated REST controller.

Backend Controller: A Spring Boot @RestController handles the request and passes it to the ChatbotService.

AI Engine: The backend utilizes the Spring AI Framework with an OpenAI-compatible adapter to communicate with Groq’s Llama 3.1-8b-instant model via a secure TLS-encrypted tunnel.