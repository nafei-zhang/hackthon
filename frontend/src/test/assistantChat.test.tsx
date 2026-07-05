import App from '@/App';
import { fetchAssistantHtmlReply } from '@/services/assistantService';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { delay, http, HttpResponse } from 'msw';
import { server } from '@/test/setup';
import { describe, expect, it } from 'vitest';

describe('Assistant chat', () => {
  it('opens and closes the floating assistant modal', async () => {
    window.history.pushState({}, '', '/workspace?caseId=CASE-2026-0001');
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole('button', { name: 'Open AI assistant' }));
    expect(await screen.findByRole('dialog', { name: 'Investigator Assistant' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Close' }));
    await waitFor(() => {
      expect(screen.queryByRole('dialog', { name: 'Investigator Assistant' })).not.toBeInTheDocument();
    });
  });

  it('clears the input immediately and shows loading in xhr mode', async () => {
    server.use(
      http.post('/api/assistant/xhr', async () => {
        await delay(120);
        return HttpResponse.json({
          html: '<section><h3>Case guidance summary</h3><p><strong>Question:</strong> Summarize this case</p></section>',
        });
      }),
    );

    window.history.pushState({}, '', '/workspace?caseId=CASE-2026-0001');
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole('button', { name: 'Open AI assistant' }));
    const dialog = await screen.findByRole('dialog', { name: 'Investigator Assistant' });
    const input = within(dialog).getByPlaceholderText(/Ask about the current case/i);

    await user.type(input, 'Summarize this case');
    await user.click(within(dialog).getByRole('button', { name: 'Send' }));

    await waitFor(() => {
      expect(input).toHaveValue('');
    });
    expect(within(dialog).getByTestId('assistant-thinking')).toBeInTheDocument();
    expect(await within(dialog).findByText('Case guidance summary')).toBeInTheDocument();
    expect(within(dialog).getByText(/Question:/)).toBeInTheDocument();
  });

  it('supports stream mode and incrementally renders the reply', async () => {
    window.history.pushState({}, '', '/workspace?caseId=CASE-2026-0002');
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole('button', { name: 'Open AI assistant' }));
    const dialog = await screen.findByRole('dialog', { name: 'Investigator Assistant' });
    const input = within(dialog).getByPlaceholderText(/Ask about the current case/i);

    await user.click(within(dialog).getByText('Stream Request'));
    await user.type(input, 'Review transaction risks');
    await user.click(within(dialog).getByRole('button', { name: 'Send' }));

    await waitFor(() => {
      expect(input).toHaveValue('');
    });
    expect(await within(dialog).findByText(/Case guidance summary/i, {}, { timeout: 5000 })).toBeInTheDocument();
    expect(await within(dialog).findByText(/Recommended review actions/i, {}, { timeout: 5000 })).toBeInTheDocument();
    await waitFor(() => {
      expect(dialog.querySelector('.assistant-message-markdown table')).toBeInTheDocument();
    }, { timeout: 5000 });
  }, 10000);

  it('shows a friendly error when the request fails', async () => {
    window.history.pushState({}, '', '/workspace?caseId=CASE-2026-0001');
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole('button', { name: 'Open AI assistant' }));
    const dialog = await screen.findByRole('dialog', { name: 'Investigator Assistant' });

    await user.type(within(dialog).getByPlaceholderText(/Ask about the current case/i), 'trigger error');
    await user.click(within(dialog).getByRole('button', { name: 'Send' }));

    expect(await within(dialog).findByText('The assistant could not complete the request.')).toBeInTheDocument();
  });

  it('supports consecutive submissions without losing loading state', async () => {
    server.use(
      http.post('/api/assistant/xhr', async ({ request }) => {
        const body = (await request.json()) as { prompt: string };

        await delay(120);
        return HttpResponse.json({
          html: `<section><h3>Case guidance summary</h3><p><strong>Question:</strong> ${body.prompt}</p></section>`,
        });
      }),
    );

    window.history.pushState({}, '', '/workspace?caseId=CASE-2026-0001');
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole('button', { name: 'Open AI assistant' }));
    const dialog = await screen.findByRole('dialog', { name: 'Investigator Assistant' });
    const input = within(dialog).getByPlaceholderText(/Ask about the current case/i);

    await user.type(input, 'First question');
    await user.click(within(dialog).getByRole('button', { name: 'Send' }));
    expect(await within(dialog).findByText('Case guidance summary')).toBeInTheDocument();

    await user.type(input, 'Second question');
    await user.click(within(dialog).getByRole('button', { name: 'Send' }));
    expect(within(dialog).getByTestId('assistant-thinking')).toBeInTheDocument();
    await waitFor(() => {
      const assistantReplies = dialog.querySelectorAll('.assistant-message-html');
      expect(assistantReplies).toHaveLength(2);
      expect(assistantReplies[1]?.textContent).toContain('Question: Second question');
    });
  });

  it('times out slow XHR requests', async () => {
    server.use(
      http.post('/api/assistant/xhr', async () => {
        await delay(100);
        return HttpResponse.json({ html: '<p>Late response</p>' });
      }),
    );

    await expect(
      fetchAssistantHtmlReply(
        {
          prompt: 'slow request',
          caseId: 'CASE-2026-0001',
          activeTab: 'kyc-profile',
        },
        10,
      ),
    ).rejects.toThrow('The request timed out. Please try again.');
  });
});
