import type { AssistantHtmlResponse, AssistantRequest } from '@/types/assistant';

type StreamOptions = {
  timeoutMs?: number;
  onChunk: (chunk: string) => void;
};

async function fetchWithTimeout(input: RequestInfo | URL, init: RequestInit = {}, timeoutMs = 15000) {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);

  try {
    const response = await fetch(input, {
      ...init,
      signal: controller.signal,
    });

    return response;
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new Error('The request timed out. Please try again.');
    }

    throw error;
  } finally {
    window.clearTimeout(timeoutId);
  }
}

export async function fetchAssistantHtmlReply(payload: AssistantRequest, timeoutMs = 15000) {
  const response = await fetchWithTimeout(
    '/api/assistant/xhr',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    },
    timeoutMs,
  );

  if (!response.ok) {
    throw new Error('The assistant could not complete the request.');
  }

  return (await response.json()) as AssistantHtmlResponse;
}

export async function fetchAssistantStreamReply(
  payload: AssistantRequest,
  { onChunk, timeoutMs = 15000 }: StreamOptions,
) {
  const response = await fetchWithTimeout(
    '/api/assistant/stream',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    },
    timeoutMs,
  );

  if (!response.ok) {
    throw new Error('The assistant could not complete the request.');
  }

  if (!response.body) {
    throw new Error('Streaming is not available in this browser.');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();

    if (done) {
      break;
    }

    onChunk(decoder.decode(value, { stream: true }));
  }
}
