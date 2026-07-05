import { handlers } from '@/mocks/handlers';
import { setupWorker } from 'msw/browser';

export const worker = setupWorker(...handlers);

export async function startMockWorker() {
  if (typeof window === 'undefined') {
    return;
  }

  await worker.start({
    onUnhandledRequest: 'bypass',
    serviceWorker: {
      url: '/mockServiceWorker.js',
    },
  });
}
