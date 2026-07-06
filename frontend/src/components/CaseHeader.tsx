import type { CaseSummary } from '@/types/case';
import { isValidCaseId, normalizeCaseId } from '@/utils/case';
import { Alert, Button, Card, Form, Input, Space, Tag, Typography } from 'antd';
import { FileSearch, ShieldAlert } from 'lucide-react';
import { useEffect } from 'react';

type CaseHeaderProps = {
  caseId: string;
  summary?: CaseSummary | null;
  loading: boolean;
  showWarning: boolean;
  onSubmit: (caseId: string) => void;
};

export function CaseHeader({ caseId, summary, loading, showWarning, onSubmit }: CaseHeaderProps) {
  const [form] = Form.useForm<{ caseId: string }>();

  useEffect(() => {
    form.setFieldsValue({ caseId });
  }, [caseId, form]);

  return (
    <div className="case-header-shell">
      <Card className="case-header-card">
        <div className="case-header-grid">
          <div className="case-header-main">
            <Space align="center" size={10}>
              <div className="header-icon">
                <FileSearch size={18} />
              </div>
              <div className="case-title-block">
                <Typography.Title level={4} className="case-title">
                  Core Workspace
                </Typography.Title>
                <div className="case-subtitle-row">
                  <Typography.Text type="secondary" className="case-subtitle">
                    Review and search case data across four business views.
                  </Typography.Text>
                  {!summary ? (
                    <Tag color={caseId ? 'processing' : 'warning'} className="case-status-tag">
                      {caseId || 'Case ID not set'}
                    </Tag>
                  ) : null}
                </div>
              </div>
            </Space>

            <Space wrap size={[8, 8]} className="case-meta">
              {summary ? <Tag color="processing">{caseId}</Tag> : null}
              {summary ? <Tag>{summary.owner}</Tag> : null}
              {summary ? <Tag>{new Date(summary.updatedAt).toLocaleString('en-US')}</Tag> : null}
            </Space>
          </div>

          <Form
            form={form}
            layout="inline"
            className="case-header-form"
            onFinish={({ caseId: value }) => onSubmit(normalizeCaseId(value))}
          >
            <Form.Item
              name="caseId"
              rules={[
                { required: true, message: 'Please enter a case ID.' },
                {
                  validator: async (_, value) => {
                    if (!value || isValidCaseId(normalizeCaseId(value))) {
                      return Promise.resolve();
                    }

                    return Promise.reject(new Error('Use letters and digits only, for example FC260305617670.'));
                  },
                },
              ]}
            >
              <Input size="small" placeholder="FC260305617670" className="case-header-input" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} className="case-header-submit">
                Apply
              </Button>
            </Form.Item>
          </Form>
        </div>

        {showWarning ? (
          <Alert
            className="case-header-alert"
            type="warning"
            showIcon
            icon={<ShieldAlert size={16} />}
            message="You entered through Skip. The active tab will load after you submit a valid case ID."
          />
        ) : null}
      </Card>
    </div>
  );
}
