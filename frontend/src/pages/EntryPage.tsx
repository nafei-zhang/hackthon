import { useCaseStore } from '@/store/useCaseStore';
import { isValidCaseId, normalizeCaseId } from '@/utils/case';
import { Alert, Button, Card, Form, Input, Space, Typography } from 'antd';
import { ArrowRight, SkipForward } from 'lucide-react';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export function EntryPage() {
  const navigate = useNavigate();
  const [form] = Form.useForm<{ caseId: string }>();
  const reset = useCaseStore((state) => state.reset);

  useEffect(() => {
    document.title = 'Investigator Workspace';
    reset();
  }, [reset]);

  const handleContinue = async () => {
    const values = await form.validateFields();
    const normalizedCaseId = normalizeCaseId(values.caseId);

    if (!isValidCaseId(normalizedCaseId)) {
      return;
    }

    navigate(`/workspace?caseId=${normalizedCaseId}`);
  };

  return (
    <div className="entry-page">
      <div className="entry-panel">
        <Card className="entry-card">
          <Space direction="vertical" size={20} style={{ width: '100%' }}>
            <div>
              <Typography.Text className="eyebrow-text">Investigation Operations</Typography.Text>
              <Typography.Title level={2} style={{ marginTop: 12, marginBottom: 12 }}>
                Investigator Workspace
              </Typography.Title>
              <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
                Enter a valid case ID to open the investigation workspace, or skip this step and set it later from the header.
              </Typography.Paragraph>
            </div>

            <Alert
              type="info"
              showIcon
              message="Accepted Case ID Format"
              description="Use CASE-YYYY-NNNN, for example CASE-2026-0001."
            />

            <Form form={form} layout="vertical">
              <Form.Item
                label="Case ID"
                name="caseId"
                rules={[
                  {
                    validator: async (_, value) => {
                      if (!value) {
                        return Promise.resolve();
                      }

                      if (isValidCaseId(normalizeCaseId(value))) {
                        return Promise.resolve();
                      }

                      return Promise.reject(new Error('Format must be CASE-YYYY-NNNN, for example CASE-2026-0001.'));
                    },
                  },
                ]}
              >
                <Input
                  size="small"
                  placeholder="CASE-2026-0001"
                  onChange={(event) => form.setFieldValue('caseId', normalizeCaseId(event.target.value))}
                />
              </Form.Item>
            </Form>

            <div className="entry-actions">
              <Button type="primary" size="large" icon={<ArrowRight size={16} />} onClick={handleContinue}>
                Continue
              </Button>
              <Button size="large" icon={<SkipForward size={16} />} onClick={() => navigate('/workspace?mode=skip')}>
                Skip
              </Button>
            </div>
          </Space>
        </Card>
      </div>
    </div>
  );
}
