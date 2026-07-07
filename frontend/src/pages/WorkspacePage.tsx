import { AssistantChat } from '@/components/AssistantChat';
import { CaseHeader } from '@/components/CaseHeader';
import { RiskChainGraph } from '@/components/RiskChainGraph';
import { BusinessTable } from '@/components/tables/BusinessTable';
import { TAB_ITEMS, type TabKey } from '@/constants/tabs';
import { fetchCaseSummary } from '@/services/caseService';
import { useCaseStore } from '@/store/useCaseStore';
import type { CaseSummary } from '@/types/case';
import { isValidCaseId, normalizeCaseId } from '@/utils/case';
import { Col, Row, Skeleton, Statistic, Tabs, Typography } from 'antd';
import { Network, SearchCode, ShieldCheck } from 'lucide-react';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';

const statItems = [
  {
    title: 'Views',
    value: 4,
    prefix: <SearchCode size={16} />,
  },
  {
    title: 'Search',
    value: 'Global + column',
    prefix: <ShieldCheck size={16} />,
  },
  {
    title: 'Data Mode',
    value: 'Case data',
    prefix: <Network size={16} />,
  },
] as const;

export function WorkspacePage() {
  const initializedRef = useRef(false);
  const [searchParams, setSearchParams] = useSearchParams();
  const [summary, setSummary] = useState<CaseSummary | null>(null);
  const [loadingSummary, setLoadingSummary] = useState(false);
  const caseId = useCaseStore((state) => state.caseId);
  const entryMode = useCaseStore((state) => state.entryMode);
  const activeTab = useCaseStore((state) => state.activeTab);
  const refreshSeed = useCaseStore((state) => state.refreshSeed);
  const initializeWorkspace = useCaseStore((state) => state.initializeWorkspace);
  const setCaseId = useCaseStore((state) => state.setCaseId);
  const setActiveTab = useCaseStore((state) => state.setActiveTab);
  const bumpRefreshSeed = useCaseStore((state) => state.bumpRefreshSeed);

  const tabItems = useMemo(
    () =>
      TAB_ITEMS.map((item) => ({
        key: item.key,
        label: item.label,
        forceRender: true,
        children:
          item.key === 'risk-chain' ? (
            <RiskChainGraph caseId={caseId} />
          ) : (
            <BusinessTable tabKey={item.key} caseId={caseId} refreshSeed={refreshSeed} />
          ),
      })),
    [caseId, refreshSeed],
  );

  useEffect(() => {
    if (initializedRef.current) {
      return;
    }

    const caseIdParam = searchParams.get('caseId');
    const normalizedCaseId = caseIdParam ? normalizeCaseId(caseIdParam) : '';

    if (normalizedCaseId && isValidCaseId(normalizedCaseId)) {
      initializeWorkspace({ caseId: normalizedCaseId, mode: 'continue' });
    } else {
      initializeWorkspace({ caseId: '', mode: 'skip' });
    }

    initializedRef.current = true;
  }, [initializeWorkspace, searchParams]);

  useEffect(() => {
    document.title = caseId ? `${caseId} | Investigator Workspace` : 'Investigator Workspace';
  }, [caseId]);

  useEffect(() => {
    if (!caseId) {
      setSummary(null);
      return;
    }

    let active = true;
    setLoadingSummary(true);

    fetchCaseSummary(caseId)
      .then((response) => {
        if (active) {
          setSummary(response);
        }
      })
      .catch(() => {
        if (active) {
          setSummary(null);
        }
      })
      .finally(() => {
        if (active) {
          setLoadingSummary(false);
        }
      });

    return () => {
      active = false;
    };
  }, [caseId]);

  const handleCaseSubmit = (nextCaseId: string) => {
    setCaseId(nextCaseId);
    bumpRefreshSeed();
    setSearchParams({ caseId: nextCaseId });
  };

  return (
    <div className="workspace-page">
      <CaseHeader
        caseId={caseId}
        summary={summary}
        loading={loadingSummary}
        showWarning={entryMode === 'skip' && !caseId}
        onSubmit={handleCaseSubmit}
      />

      <main className="workspace-main">
        <section className="workspace-intro">
          <div className="workspace-section-head">
            <Typography.Title level={3} className="workspace-section-title">
              Case Analysis Workspace
            </Typography.Title>
            <Typography.Paragraph type="secondary" className="workspace-section-desc">
              The four tab titles match the reference exactly and support paging, keyword search, global search, and column filters.
            </Typography.Paragraph>
          </div>

          <Row gutter={[16, 16]}>
            {statItems.map((item) => (
              <Col key={item.title} xs={24} md={8}>
                <div className="workspace-stat">
                  <Statistic title={item.title} value={item.value} prefix={item.prefix} />
                </div>
              </Col>
            ))}
          </Row>

          {loadingSummary && caseId ? (
            <div className="workspace-skeleton">
              <Skeleton active paragraph={{ rows: 2 }} />
            </div>
          ) : null}

          {!caseId ? (
            <div className="workspace-inline-hint">
              <Typography.Text strong>Case ID is not set</Typography.Text>
              <Typography.Text type="secondary">
                You can review the workspace first. Once a valid case ID is submitted, data for the active tab will load automatically.
              </Typography.Text>
            </div>
          ) : null}
        </section>

        <section className="workspace-tabs">
          <Tabs
            activeKey={activeTab}
            animated={{ inkBar: true, tabPane: false }}
            onChange={(key) => setActiveTab(key as TabKey)}
            items={tabItems}
          />
        </section>
      </main>

      <AssistantChat caseId={caseId} activeTab={activeTab} />
    </div>
  );
}
