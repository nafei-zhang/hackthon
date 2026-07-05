import {
  buildAssistantHtml,
  buildAssistantStreamChunks,
  buildBadConnections,
  buildCaseSummary,
  buildKycProfiles,
  buildPagedResponse,
  buildPreviousInvestigations,
  buildTransactionReviews,
} from '@/mocks/data';
import type { TableQuery } from '@/types/case';
import { delay, http, HttpResponse } from 'msw';

const ASSISTANT_RESPONSE_DELAY_MS = 2000;

function getQuery(request: Request): TableQuery {
  const url = new URL(request.url);
  const filters = url.searchParams.get('filters');

  return {
    page: Number(url.searchParams.get('page') ?? 1),
    pageSize: Number(url.searchParams.get('pageSize') ?? 10),
    keyword: url.searchParams.get('keyword') ?? '',
    globalSearch: url.searchParams.get('globalSearch') ?? '',
    sortField: url.searchParams.get('sortField') ?? undefined,
    sortOrder: (url.searchParams.get('sortOrder') as TableQuery['sortOrder']) ?? undefined,
    filters: filters ? JSON.parse(filters) : {},
  };
}

export const handlers = [
  http.post('/api/assistant/xhr', async ({ request }) => {
    const body = (await request.json()) as { prompt: string; caseId?: string; activeTab: string };

    if (body.prompt.toLowerCase().includes('error')) {
      return HttpResponse.json({ message: 'Assistant request failed.' }, { status: 500 });
    }

    await delay(ASSISTANT_RESPONSE_DELAY_MS);

    return HttpResponse.json({
      html: buildAssistantHtml(body.prompt, body.caseId, body.activeTab),
    });
  }),
  http.post('/api/assistant/stream', async ({ request }) => {
    const body = (await request.json()) as { prompt: string; caseId?: string; activeTab: string };

    if (body.prompt.toLowerCase().includes('error')) {
      return HttpResponse.json({ message: 'Assistant request failed.' }, { status: 500 });
    }

    const encoder = new TextEncoder();
    const chunks = buildAssistantStreamChunks(body.prompt, body.caseId, body.activeTab);

    const stream = new ReadableStream({
      start(controller) {
        let index = 0;

        const pushChunk = () => {
          if (index >= chunks.length) {
            controller.close();
            return;
          }

          controller.enqueue(encoder.encode(chunks[index]));
          index += 1;
          setTimeout(pushChunk, 15);
        };

        setTimeout(pushChunk, ASSISTANT_RESPONSE_DELAY_MS);
      },
    });

    return new HttpResponse(stream, {
      headers: {
        'Content-Type': 'text/plain; charset=utf-8',
      },
    });
  }),
  http.get('/api/cases/:caseId/summary', async ({ params }) => {
    const caseId = String(params.caseId);
    return HttpResponse.json(buildCaseSummary(caseId));
  }),
  http.get('/api/cases/:caseId/kyc-profile', async ({ params, request }) => {
    return HttpResponse.json(buildPagedResponse(buildKycProfiles(String(params.caseId)), getQuery(request)));
  }),
  http.get('/api/cases/:caseId/previous-investigation', async ({ params, request }) => {
    return HttpResponse.json(
      buildPagedResponse(buildPreviousInvestigations(String(params.caseId)), getQuery(request)),
    );
  }),
  http.get('/api/cases/:caseId/transaction-review', async ({ params, request }) => {
    return HttpResponse.json(
      buildPagedResponse(buildTransactionReviews(String(params.caseId)), getQuery(request)),
    );
  }),
  http.get('/api/cases/:caseId/bad-connections', async ({ params, request }) => {
    return HttpResponse.json(buildPagedResponse(buildBadConnections(String(params.caseId)), getQuery(request)));
  }),
];
