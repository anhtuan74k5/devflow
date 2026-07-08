import { useState, useEffect } from 'react';
import {
  Box,
  Button,
  FormControl,
  FormLabel,
  Input,
  Textarea,
  Select,
  Stack,
} from '@chakra-ui/react';
import type { CreateTaskRequest, Task } from '../types';

interface TaskFormProps {
  initialData?: Task;
  onSubmit: (data: CreateTaskRequest) => Promise<void>;
  isSubmitting: boolean;
}

export default function TaskForm({
  initialData,
  onSubmit,
  isSubmitting,
}: TaskFormProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'>('MEDIUM');
  const [assigneeId, setAssigneeId] = useState<number | undefined>(undefined);

  useEffect(() => {
    if (initialData) {
      setTitle(initialData.title);
      setDescription(initialData.description);
      setPriority(initialData.priority);
      setAssigneeId(initialData.assigneeId ?? undefined);
    }
  }, [initialData]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const data: CreateTaskRequest = { title, description, priority };
    if (assigneeId !== undefined) {
      data.assigneeId = assigneeId;
    }
    onSubmit(data);
  };

  return (
    <Box as="form" onSubmit={handleSubmit} borderWidth={1} borderRadius="lg" p={6}>
      <Stack spacing={4}>
        <FormControl isRequired>
          <FormLabel>Title</FormLabel>
          <Input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Enter task title"
          />
        </FormControl>
        <FormControl>
          <FormLabel>Description</FormLabel>
          <Textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Enter task description"
            rows={3}
          />
        </FormControl>
        <FormControl isRequired>
          <FormLabel>Priority</FormLabel>
          <Select
            value={priority}
            onChange={(e) => setPriority(e.target.value as typeof priority)}
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </Select>
        </FormControl>
        <FormControl>
          <FormLabel>Assignee ID</FormLabel>
          <Input
            type="number"
            value={assigneeId ?? ''}
            onChange={(e) => {
              const val = e.target.value;
              setAssigneeId(val ? parseInt(val, 10) : undefined);
            }}
            placeholder="Enter user ID (optional)"
            min={1}
          />
        </FormControl>
        <Button type="submit" colorScheme="blue" isLoading={isSubmitting}>
          {initialData ? 'Update' : 'Create'} task
        </Button>
      </Stack>
    </Box>
  );
}
