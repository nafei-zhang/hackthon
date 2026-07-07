import type { TabKey } from '@/constants/tabs';
import { fetchAssistantHtmlReply, fetchAssistantStreamReply } from '@/services/assistantService';
import type { AssistantMessage, AssistantMode } from '@/types/assistant';
import { Button, Empty, Input, Modal, Segmented, Space, Typography } from 'antd';
import { Bot, LoaderCircle, MessageSquare, Send, UserRound, XCircle } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { useEffect, useMemo, useRef, useState } from 'react';
import { MermaidRenderer } from './MermaidRenderer';

const { TextArea } = Input;

type AssistantChatProps = {
  caseId: string;
  activeTab: TabKey;
};

const modeOptions = [
  { label: 'XHR Request', value: 'xhr' },
  { label: 'Stream Request', value: 'stream' },
] as const;

function createMessageId() {
  return `msg-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
}

export function AssistantChat({ caseId, activeTab }: AssistantChatProps) {
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState<AssistantMode>('xhr');
  const [prompt, setPrompt] = useState('');
  const [messages, setMessages] = useState<AssistantMessage[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const scrollRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!scrollRef.current) {
      return;
    }

    scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
  }, [messages, open]);

  const headerDescription = useMemo(() => {
    return caseId ? `Current case: ${caseId} | Active tab: ${activeTab}` : `Active tab: ${activeTab}`;
  }, [activeTab, caseId]);

  const handleSubmit = async () => {
    const normalizedPrompt = prompt.trim();

    if (!normalizedPrompt || submitting) {
      return;
    }

    setPrompt('');
    setSubmitting(true);

    const userMessage: AssistantMessage = {
      id: createMessageId(),
      role: 'user',
      mode,
      text: normalizedPrompt,
      status: 'idle',
    };

    const assistantId = createMessageId();

    setMessages((previous) => [
      ...previous,
      userMessage,
      {
        id: assistantId,
        role: 'assistant',
        mode,
        text: mode === 'stream' ? '' : undefined,
        status: 'loading',
      },
    ]);

    try {
      if (mode === 'xhr') {
        const response = await fetchAssistantHtmlReply({
          prompt: normalizedPrompt,
          caseId,
          activeTab,
        });

        setMessages((previous) =>
          previous.map((item) =>
            item.id === assistantId
              ? {
                  ...item,
                  html: response.html,
                  status: 'idle',
                }
              : item,
          ),
        );
      } else {
        await fetchAssistantStreamReply(
          {
            prompt: normalizedPrompt,
            caseId,
            activeTab,
          },
          {
            onChunk: (chunk) => {
              setMessages((previous) =>
                previous.map((item) =>
                  item.id === assistantId
                    ? {
                        ...item,
                        text: `${item.text ?? ''}${chunk}`,
                        status: 'loading',
                      }
                    : item,
                ),
              );
            },
          },
        );

        setMessages((previous) =>
          previous.map((item) =>
            item.id === assistantId
              ? {
                  ...item,
                  status: 'idle',
                }
              : item,
          ),
        );
      }
    } catch (error) {
      const text = error instanceof Error ? error.message : 'The request failed. Please try again.';

      setMessages((previous) =>
        previous.map((item) =>
          item.id === assistantId
            ? {
                ...item,
                text,
                html: undefined,
                status: 'error',
              }
            : item,
        ),
      );
    } finally {
      setSubmitting(false);
    }
  };

  const renderMessage = (message: AssistantMessage) => {
    const isAssistant = message.role === 'assistant';

    return (
      <div
        key={message.id}
        className={`assistant-message ${isAssistant ? 'assistant-message-assistant' : 'assistant-message-user'} ${
          message.status === 'error' ? 'assistant-message-error' : ''
        }`}
      >
        <div className="assistant-message-avatar">
          {isAssistant ? <Bot size={16} /> : <UserRound size={16} />}
        </div>
        <div className="assistant-message-content">
          <Typography.Text className="assistant-message-role">{isAssistant ? 'Assistant' : 'You'}</Typography.Text>
          {message.status === 'loading' && !message.text && !message.html ? (
            <div className="assistant-thinking" data-testid="assistant-thinking">
              <LoaderCircle size={16} className="assistant-thinking-icon" />
              <Typography.Text className="assistant-thinking-text">Thinking...</Typography.Text>
            </div>
          ) : message.mode === 'stream' && message.text ? (
            <div className="assistant-message-markdown">
              <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                components={{
                  table: ({ node, ...props }) => {
                    void node;

                    return (
                      <div className="assistant-message-table-wrap">
                        <table {...props} />
                      </div>
                    );
                  },
                  code({ node, inline, className, children, ...props }) {
                    void node;
                    const match = /language-(\w+)/.exec(className || '');
                    if (!inline && match && match[1] === 'mermaid') {
                      return <MermaidRenderer code={String(children).replace(/\n$/, '')} />;
                    }
                    return (
                      <code className={className} {...props}>
                        {children}
                      </code>
                    );
                  },
                }}
              >
                {message.text}
              </ReactMarkdown>
            </div>
          ) : message.html ? (
            <div
              className="assistant-message-html"
              dangerouslySetInnerHTML={{ __html: message.html }}
            />
          ) : (
            <Typography.Paragraph className="assistant-message-text">
              {message.text || (message.status === 'loading' ? 'Thinking...' : '')}
            </Typography.Paragraph>
          )}
          {message.status === 'loading' && Boolean(message.text || message.html) ? (
            <Space size={8} className="assistant-message-loading">
              <LoaderCircle size={14} className="assistant-thinking-icon" />
              <Typography.Text type="secondary">Thinking...</Typography.Text>
            </Space>
          ) : null}
        </div>
      </div>
    );
  };

  return (
    <>
      <div className="assistant-fab-shell">
        <Button
          type="primary"
          shape="circle"
          size="large"
          aria-label="Open AI assistant"
          className="assistant-fab"
          icon={<MessageSquare size={20} />}
          onClick={() => setOpen(true)}
        />
      </div>

      <Modal
        title="Investigator Assistant"
        open={open}
        onCancel={() => setOpen(false)}
        footer={null}
        centered
        destroyOnHidden={false}
        rootClassName="assistant-modal-root"
        className="assistant-modal"
      >
        <div className="assistant-modal-head">
          <Typography.Text type="secondary">{headerDescription}</Typography.Text>
          <Segmented
            options={modeOptions as unknown as { label: string; value: AssistantMode }[]}
            value={mode}
            onChange={(value) => {
              setMode(value as AssistantMode);
              setMessages([]);
              setPrompt('');
            }}
            disabled={submitting}
          />
        </div>

        <div className="assistant-modal-body" ref={scrollRef}>
          {messages.length ? messages.map(renderMessage) : <Empty description="Ask a question to start the conversation." />}
        </div>

        <div className="assistant-modal-input">
          <TextArea
            value={prompt}
            onChange={(event) => setPrompt(event.target.value)}
            placeholder="Ask about the current case, the active tab, or next steps."
            autoSize={{ minRows: 3, maxRows: 5 }}
            onPressEnter={(event) => {
              if (!event.shiftKey) {
                event.preventDefault();
                void handleSubmit();
              }
            }}
          />
          <div className="assistant-modal-actions">
            <Button
              icon={<XCircle size={16} />}
              onClick={() => {
                setMessages([]);
                setPrompt('');
              }}
              disabled={submitting || !messages.length}
            >
              Clear
            </Button>
            <Button
              type="primary"
              icon={<Send size={16} />}
              onClick={() => void handleSubmit()}
              loading={submitting}
              disabled={!prompt.trim()}
            >
              Send
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}
