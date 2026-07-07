import React, { useEffect, useRef } from 'react';
import mermaid from 'mermaid';

mermaid.initialize({
  startOnLoad: false,
  theme: 'default',
  securityLevel: 'loose',
});

interface MermaidRendererProps {
  code: string;
}

export const MermaidRenderer: React.FC<MermaidRendererProps> = ({ code }) => {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!containerRef.current) return;

    const id = `mermaid-${Math.random().toString(36).substr(2, 9)}`;
    try {
      containerRef.current.innerHTML = '';
      const { svg } = mermaid.render(id, code);
      containerRef.current.innerHTML = svg;
    } catch (error) {
      console.error('Mermaid render error:', error);
      containerRef.current.innerHTML = `<div class="text-red-500">Failed to render diagram</div>`;
    }
  }, [code]);

  return <div ref={containerRef} className="mermaid-container my-4" />;
};
