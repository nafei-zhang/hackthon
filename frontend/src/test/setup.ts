import '@testing-library/jest-dom';
import { handlers } from '@/mocks/handlers';
import { resetCaseStore } from '@/store/useCaseStore';
import { cleanup } from '@testing-library/react';
import { afterAll, afterEach, beforeAll } from 'vitest';
import { setupServer } from 'msw/node';

export const server = setupServer(...handlers);

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => undefined,
    removeListener: () => undefined,
    addEventListener: () => undefined,
    removeEventListener: () => undefined,
    dispatchEvent: () => false,
  }),
});

class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}

const originalGetComputedStyle = window.getComputedStyle.bind(window);

window.getComputedStyle = ((element: Element) => originalGetComputedStyle(element)) as typeof window.getComputedStyle;
window.ResizeObserver = ResizeObserverMock;
window.scrollTo = () => undefined;

beforeAll(() => {
  server.listen({ onUnhandledRequest: 'error' });
});

afterEach(() => {
  cleanup();
  resetCaseStore();
  server.resetHandlers();
  window.history.replaceState({}, '', '/');
});

afterAll(() => {
  server.close();
});
