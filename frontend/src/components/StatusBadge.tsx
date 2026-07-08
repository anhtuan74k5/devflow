import { Badge } from '@chakra-ui/react';

const statusColorMap: Record<string, string> = {
  TODO: 'gray',
  IN_PROGRESS: 'blue',
  DONE: 'green',
};

interface StatusBadgeProps {
  status: string;
}

export default function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <Badge colorScheme={statusColorMap[status] || 'gray'}>
      {status === 'IN_PROGRESS' ? 'IN PROGRESS' : status}
    </Badge>
  );
}
