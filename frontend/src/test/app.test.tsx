import App from '@/App';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

function setViewport(width: number) {
  window.innerWidth = width;
  window.dispatchEvent(new Event('resize'));
}

describe('Case management app', () => {
  it('continues with a valid case id and loads the default tab data', async () => {
    window.history.pushState({}, '', '/');
    const user = userEvent.setup();
    render(<App />);

    await user.type(screen.getByPlaceholderText('CASE-2026-0001'), 'case-2026-0001');
    await user.click(screen.getByRole('button', { name: 'Continue' }));

    expect(await screen.findByRole('heading', { name: 'Core Workspace' })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'KYC profile' })).toBeInTheDocument();
    expect(await screen.findByText(/CASE-2026-0001/)).toBeInTheDocument();
    expect((await screen.findAllByText(/CUST-/)).length).toBeGreaterThan(0);
  });

  it('allows entering through skip and loading data after submitting a case id', async () => {
    window.history.pushState({}, '', '/');
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole('button', { name: 'Skip' }));

    expect(await screen.findByText('Case ID is not set')).toBeInTheDocument();
    expect(screen.getByText(/Once a valid case ID is submitted/i)).toBeInTheDocument();

    const headerInput = screen.getAllByPlaceholderText('CASE-2026-0001')[0];
    await user.clear(headerInput);
    await user.type(headerInput, 'CASE-2026-0099');
    await user.click(screen.getByRole('button', { name: 'Apply' }));

    expect(await screen.findByText(/CASE-2026-0099/)).toBeInTheDocument();
    expect((await screen.findAllByText(/CUST-/)).length).toBeGreaterThan(0);
  });

  it('supports tab switching, keyword search, and responsive rendering', async () => {
    window.history.pushState({}, '', '/workspace?caseId=CASE-2026-0003');
    setViewport(1280);

    const user = userEvent.setup();
    render(<App />);

    expect(await screen.findByRole('heading', { name: 'Case Analysis Workspace' })).toBeInTheDocument();

    await user.click(screen.getByRole('tab', { name: 'Transaction review' }));
    expect((await screen.findAllByText(/Atlas Holdings|North Ridge|Silver Axis|Morning Peak/)).length).toBeGreaterThan(0);

    const globalSearch = within(screen.getByRole('tabpanel')).getByPlaceholderText('Search this tab');
    await user.type(globalSearch, 'Atlas');
    await user.click(screen.getByRole('button', { name: 'Search' }));
    expect((await screen.findAllByText(/Atlas Holdings/)).length).toBeGreaterThan(0);

    setViewport(768);
    await waitFor(() => {
      expect(screen.getByRole('tablist')).toBeInTheDocument();
      expect(screen.getByRole('heading', { name: 'Core Workspace' })).toBeInTheDocument();
    });

    const tabPanel = screen.getByRole('tabpanel');
    expect(within(tabPanel).getByRole('table')).toBeInTheDocument();
  });
});
