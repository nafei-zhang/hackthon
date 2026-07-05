import Empty from '@/components/Empty';
import { TAB_LABEL_MAP, type TabKey } from '@/constants/tabs';
import { getTableColumns } from '@/components/tables/tableConfigs';
import { fetchTabTableData } from '@/services/caseService';
import type { SortOrder } from '@/types/case';
import { ProTable } from '@ant-design/pro-components';
import { Button, Input, Space, Typography } from 'antd';
import { Search, RotateCcw } from 'lucide-react';
import { useMemo, useState } from 'react';

type BusinessTableProps = {
  tabKey: TabKey;
  caseId: string;
  refreshSeed: number;
};

export function BusinessTable({ tabKey, caseId, refreshSeed }: BusinessTableProps) {
  const [globalSearch, setGlobalSearch] = useState('');
  const [appliedSearch, setAppliedSearch] = useState('');
  const columns = useMemo(() => getTableColumns(tabKey), [tabKey]);

  return (
    <ProTable
      rowKey="id"
      cardBordered
      columns={columns}
      tableLayout="auto"
      params={{ caseId, globalSearch: appliedSearch, refreshSeed }}
      search={{
        labelWidth: 'auto',
        defaultCollapsed: false,
      }}
      pagination={{
        showQuickJumper: true,
        showSizeChanger: true,
        defaultPageSize: 8,
      }}
      options={{
        density: true,
        setting: true,
        reload: true,
      }}
      tableAlertRender={false}
      scroll={{ x: 'max-content' }}
      request={async (params, sort, filter) => {
        if (!caseId) {
          return {
            data: [],
            total: 0,
            success: true,
          };
        }

        const sortField = Object.keys(sort ?? {})[0];
        const sortOrder = (sortField ? sort?.[sortField] : undefined) as SortOrder | undefined;

        return fetchTabTableData(tabKey, caseId, {
          page: params.current,
          pageSize: params.pageSize,
          keyword: typeof params.keyword === 'string' ? params.keyword : '',
          globalSearch: typeof params.globalSearch === 'string' ? params.globalSearch : '',
          sortField,
          sortOrder,
          filters: Object.fromEntries(
            Object.entries(filter ?? {}).map(([key, value]) => [
              key,
              Array.isArray(value) ? value.map((item) => String(item)) : [],
            ]),
          ),
        });
      }}
      locale={{
        emptyText: (
          <Empty
            title={caseId ? `No ${TAB_LABEL_MAP[tabKey]} data found` : 'Set a case ID first'}
            description={
              caseId
                ? 'Try adjusting the keyword, filters, or switch to another tab.'
                : 'You entered through Skip. Submit a valid case ID to load data for the active tab.'
            }
          />
        ),
      }}
      className="workspace-pro-table"
      headerTitle={
        <Space direction="vertical" size={2} className="table-header-title">
          <Typography.Text strong className="table-title-text">
            {TAB_LABEL_MAP[tabKey]}
          </Typography.Text>
          <Typography.Text type="secondary" className="table-title-desc">
            Supports global search, keyword search, column filters, and paging.
          </Typography.Text>
        </Space>
      }
      toolBarRender={() => [
        <Input
          key="global-search"
          size="small"
          allowClear
          prefix={<Search size={14} />}
          placeholder="Search this tab"
          className="table-global-search"
          value={globalSearch}
          onChange={(event) => setGlobalSearch(event.target.value)}
          onPressEnter={() => setAppliedSearch(globalSearch.trim())}
          style={{ width: 240 }}
        />,
        <Button
          key="apply-search"
          type="primary"
          className="table-toolbar-button"
          onClick={() => setAppliedSearch(globalSearch.trim())}
        >
          Search
        </Button>,
        <Button
          key="reset-search"
          icon={<RotateCcw size={14} />}
          className="table-toolbar-button"
          onClick={() => {
            setGlobalSearch('');
            setAppliedSearch('');
          }}
        >
          Reset
        </Button>,
      ]}
    />
  );
}
