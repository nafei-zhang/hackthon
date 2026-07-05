# Investigator Workspace

Investigator Workspace is a full-stack case investigation demo project with a React frontend and a Spring Boot backend.

## Tech Stack

- Frontend: React 18, Vite, TypeScript, Ant Design, ProComponents
- Backend: Spring Boot 3.3, Java 17, Maven
- Assistant rendering:
  - XHR mode returns formatted HTML
  - Stream mode returns Markdown (GFM)

## Project Structure

```text
hackthon/
├── frontend/   # React + Vite application
├── backend/    # Spring Boot API service
└── README.md
```

## Frontend

Run the frontend in the `frontend/` directory:

```bash
npm install
npm run dev
```

Default dev URL:

```text
http://localhost:5173
```

Useful commands:

```bash
npm run check
npm run lint
npm run test
```

## Backend

Run the backend in the `backend/` directory:

```bash
mvn spring-boot:run
```

Or build and run the jar:

```bash
mvn package
java -jar target/investigator-backend-0.0.1-SNAPSHOT.jar
```

Default API URL:

```text
http://localhost:8080
```

Run backend tests:

```bash
mvn test
```

## Assistant AI Routing

The backend supports an optional OpenAI-compatible routing layer for deciding which assistant workflow to use.

Example environment variables:

```bash
export ASSISTANT_AI_ENABLED=true
export ASSISTANT_AI_PROVIDER=github
export ASSISTANT_AI_BASE_URL=https://models.github.ai
export ASSISTANT_AI_CHAT_COMPLETIONS_PATH=/inference/chat/completions
export ASSISTANT_AI_MODEL=openai/gpt-4.1
export ASSISTANT_AI_API_KEY=your_token
```

When AI routing is unavailable, the backend falls back to local rule-based routing.

## Notes

- The frontend proxies API calls to the backend on `localhost:8080`.
- The assistant UI is English-only.
- The backend includes mock case data and assistant response workflows for local development.
