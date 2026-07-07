import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import ReactFlow, {
  ReactFlowProvider,
  addEdge,
  useNodesState,
  useEdgesState,
  Controls,
  Background,
  MiniMap,
  Handle,
  Position,
  MarkerType,
  type Connection,
  type Edge,
  type Node,
  type NodeClickHandler,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { Badge, Skeleton, Typography, Drawer, Descriptions } from 'antd';
import { Building2, User, AlertTriangle, X } from 'lucide-react';
import { fetchRiskChain } from '@/services/caseService';
import { clsx } from 'clsx';
import type { RiskNode, RiskChainData, CustomerDetails, CompanyDetails } from '@/types/case';

const { Text } = Typography;

const getRiskColor = (riskLevel: string) => {
  switch (riskLevel) {
    case 'Low':
      return '#52c41a';
    case 'Medium':
      return '#faad14';
    case 'High':
      return '#fa8c16';
    case 'Critical':
      return '#ff4d4f';
    default:
      return '#d9d9d9';
  }
};

interface CustomNodeData extends Omit<RiskNode, 'details'> {
  details?: CustomerDetails | CompanyDetails;
}

const CustomNode = ({ data }: { data: CustomNodeData }) => {
  const Icon = data.type === 'company' ? Building2 : User;
  const riskColor = getRiskColor(data.riskLevel);
  const hasRisk = data.riskLevel !== 'Low';

  return (
    <div
      className={clsx(
        'shadow-lg rounded-xl border-2 bg-white min-w-[160px] cursor-pointer transition-all hover:shadow-xl',
        data.isPrimary ? 'border-blue-500 ring-2 ring-blue-100' : 'border-gray-200'
      )}
    >
      <Handle type="target" position={Position.Top} className="!bg-gray-400" />
      <div className="p-4">
        <div className="flex items-center gap-2 mb-2">
          <Icon
            size={18}
            className={data.type === 'company' ? 'text-purple-500' : 'text-blue-500'}
          />
          <Text strong className="text-gray-800 text-sm">
            {data.name}
          </Text>
        </div>
        {data.customerId && (
          <Text type="secondary" className="text-xs block mb-2">
            {data.customerId}
          </Text>
        )}
        <div className="flex items-center gap-2">
          {hasRisk && <AlertTriangle size={14} style={{ color: riskColor }} />}
          <Badge
            color={riskColor}
            text={
              <span style={{ color: riskColor, fontWeight: 600, fontSize: '12px' }}>
                {data.riskLevel} Risk
              </span>
            }
          />
        </div>
        {data.isPrimary && (
          <div className="mt-2 pt-2 border-t border-gray-100">
            <Badge status="processing" text="Primary" />
          </div>
        )}
      </div>
      <Handle type="source" position={Position.Bottom} className="!bg-gray-400" />
    </div>
  );
};

const nodeTypes = {
  custom: CustomNode,
};

const RiskChainGraphContent = ({ caseId }: { caseId: string }) => {
  const reactFlowWrapper = useRef<HTMLDivElement>(null);
  const [nodes, setNodes, onNodesChange] = useNodesState<CustomNodeData>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [reactFlowInstance, setReactFlowInstance] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [selectedNode, setSelectedNode] = useState<RiskNode | null>(null);
  const [riskData, setRiskData] = useState<RiskChainData | null>(null);

  useEffect(() => {
    if (!caseId) {
      setNodes([]);
      setEdges([]);
      setLoading(false);
      setSelectedNode(null);
      setRiskData(null);
      return;
    }

    setLoading(true);
    fetchRiskChain(caseId)
      .then((data) => {
        setRiskData(data);
        
        const width = 800;
        const height = 600;
        const centerX = width / 2;
        const centerY = height / 2;
        const radius = 200;

        const transformedNodes: Node<CustomNodeData>[] = data.nodes.map((node, index) => {
          let x, y;
          if (node.isPrimary) {
            x = centerX - 80;
            y = centerY - 40;
          } else {
            const angle = ((index - 1) / (data.nodes.length - 1)) * 2 * Math.PI;
            x = centerX + Math.cos(angle) * radius - 80;
            y = centerY + Math.sin(angle) * radius - 40;
          }
          return {
            id: node.id,
            type: 'custom',
            position: { x, y },
            data: {
              ...node,
            },
          };
        });

        const transformedEdges: Edge[] = data.edges.map((edge) => ({
          id: edge.id,
          source: edge.source,
          target: edge.target,
          label: edge.label,
          animated: true,
          markerEnd: { type: MarkerType.ArrowClosed, color: '#1890ff' },
          style: { stroke: '#1890ff', strokeWidth: 2 },
          labelStyle: { fill: '#595959', fontSize: 12, fontWeight: 500 },
          labelBgStyle: { fill: '#fff', fillOpacity: 0.8 },
        }));

        setNodes(transformedNodes);
        setEdges(transformedEdges);
      })
      .catch((error) => {
        console.error('Failed to fetch risk chain:', error);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [caseId, setNodes, setEdges]);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  const onNodeClick: NodeClickHandler<CustomNodeData> = useCallback(
    (_event, node) => {
      if (riskData) {
        const foundNode = riskData.nodes.find(n => n.id === node.id);
        if (foundNode) {
          setSelectedNode(foundNode);
        }
      }
    },
    [riskData]
  );

  const renderCustomerDetails = (details: CustomerDetails) => (
    <Descriptions column={1} size="small">
      <Descriptions.Item label="Email">{details.email}</Descriptions.Item>
      <Descriptions.Item label="Mobile">{details.mobile}</Descriptions.Item>
      <Descriptions.Item label="Occupation">{details.occupation}</Descriptions.Item>
      <Descriptions.Item label="Employer">{details.employer}</Descriptions.Item>
      <Descriptions.Item label="Salary">{details.salary.toLocaleString()}</Descriptions.Item>
      <Descriptions.Item label="Nationality">{details.nationality}</Descriptions.Item>
      <Descriptions.Item label="Address">{details.address}</Descriptions.Item>
      <Descriptions.Item label="Customer Since">{details.customerSince}</Descriptions.Item>
    </Descriptions>
  );

  const renderCompanyDetails = (details: CompanyDetails) => (
    <Descriptions column={1} size="small">
      <Descriptions.Item label="Registration Number">{details.registrationNumber}</Descriptions.Item>
      <Descriptions.Item label="Industry">{details.industry}</Descriptions.Item>
      <Descriptions.Item label="Founded Year">{details.foundedYear}</Descriptions.Item>
      <Descriptions.Item label="Headquarters">{details.headquarters}</Descriptions.Item>
      <Descriptions.Item label="Employees">{details.employees.toLocaleString()}</Descriptions.Item>
      <Descriptions.Item label="Revenue">{details.revenue.toLocaleString()}</Descriptions.Item>
    </Descriptions>
  );

  if (loading) {
    return (
      <div className="w-full h-[600px] p-4">
        <Skeleton active paragraph={{ rows: 10 }} />
      </div>
    );
  }

  if (!caseId) {
    return (
      <div className="w-full h-[600px] flex items-center justify-center">
        <Text type="secondary">Please enter a Case ID to view the Risk Chain</Text>
      </div>
    );
  }

  return (
    <div className="flex w-full h-[600px]">
      <div ref={reactFlowWrapper} className="flex-1">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          onInit={setReactFlowInstance}
          onNodeClick={onNodeClick}
          nodeTypes={nodeTypes}
          fitView
          fitViewOptions={{ padding: 0.2 }}
        >
          <Background gap={16} size={1} />
          <Controls className="bg-white shadow-lg border border-gray-200 rounded-lg" />
          <MiniMap
            className="bg-white shadow-lg border border-gray-200 rounded-lg overflow-hidden"
            nodeStrokeColor={(n) => {
              const data = n.data as CustomNodeData;
              return getRiskColor(data.riskLevel);
            }}
            nodeColor={(n) => {
              const data = n.data as CustomNodeData;
              return data.isPrimary ? '#e6f7ff' : '#fff';
            }}
          />
        </ReactFlow>
      </div>

      <Drawer
        title={
          <div className="flex items-center gap-2">
            {selectedNode?.type === 'company' ? <Building2 size={20} /> : <User size={20} />}
            <span>{selectedNode?.name}</span>
          </div>
        }
        placement="right"
        onClose={() => setSelectedNode(null)}
        open={!!selectedNode}
        mask={true}
        maskClosable={true}
        width={400}
      >
        {selectedNode && (
          <div>
            <div className="mb-4">
              <Badge
                color={getRiskColor(selectedNode.riskLevel)}
                text={
                  <span style={{ color: getRiskColor(selectedNode.riskLevel), fontWeight: 600 }}>
                    {selectedNode.riskLevel} Risk
                  </span>
                }
              />
              {selectedNode.isPrimary && (
                <Badge status="processing" text="Primary" className="ml-2" />
              )}
            </div>
            {selectedNode.details ? (
              selectedNode.type === 'customer' ? (
                renderCustomerDetails(selectedNode.details as CustomerDetails)
              ) : (
                renderCompanyDetails(selectedNode.details as CompanyDetails)
              )
            ) : (
              <Text type="secondary">No details available</Text>
            )}
          </div>
        )}
      </Drawer>
    </div>
  );
};

export const RiskChainGraph = ({ caseId }: { caseId: string }) => {
  return (
    <ReactFlowProvider>
      <RiskChainGraphContent caseId={caseId} />
    </ReactFlowProvider>
  );
};
