import { SearchCheck } from 'lucide-react';

type EmptyStateProps = {
  title: string;
  description: string;
};

export default function Empty({ title, description }: EmptyStateProps) {
  return (
    <div className="table-empty-state">
      <SearchCheck size={28} />
      <div>
        <strong>{title}</strong>
        <p>{description}</p>
      </div>
    </div>
  );
}
