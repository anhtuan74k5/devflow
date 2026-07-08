import { Tag } from '@chakra-ui/react';

const priorityColorMap: Record<string, string> = {
  LOW: 'green',
  MEDIUM: 'yellow',
  HIGH: 'red',
  CRITICAL: 'purple',
};

interface PriorityTagProps {
  priority: string;
}

export default function PriorityTag({ priority }: PriorityTagProps) {
  return (
    <Tag colorScheme={priorityColorMap[priority] || 'gray'} variant="subtle">
      {priority}
    </Tag>
  );
}
