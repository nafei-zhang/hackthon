import type { TabKey } from '@/constants/tabs';

export type AssistantMode = 'xhr' | 'stream';

export type AssistantRequest = {
  prompt: string;
  caseId?: string;
  activeTab: TabKey;
};

export type AssistantHtmlResponse = {
  html: string;
};

export type AssistantMessage = {
  id: string;
  role: 'user' | 'assistant';
  mode: AssistantMode;
  text?: string;
  html?: string;
  status?: 'idle' | 'loading' | 'error';
};
