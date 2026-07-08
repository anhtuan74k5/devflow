import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Button,
  Heading,
  HStack,
  Spinner,
  Center,
  Text,
  Stack,
  Select,
  Link,
} from '@chakra-ui/react';
import toast from 'react-hot-toast';
import client from '../api/client';
import TaskForm from '../components/TaskForm';
import StatusBadge from '../components/StatusBadge';
import PriorityTag from '../components/PriorityTag';
import { useAuth } from '../context/AuthContext';
import type { Task, CreateTaskRequest, ApiResponse } from '../types';

export default function TaskDetailPage() {
  const { id: projectId, taskId } = useParams<{ id: string; taskId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();


  const [task, setTask] = useState<Task | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [statusUpdating, setStatusUpdating] = useState(false);
  const [projectOwnerId, setProjectOwnerId] = useState<number | null>(null);

  const isOwnerOrAdmin = (ownerId: number | null): boolean => {
    if (!user) return false;
    if (user.role === 'ADMIN') return true;
    return user.id === ownerId;
  };

  useEffect(() => {
    (async () => {
      try {
        const [taskRes, projectRes] = await Promise.all([
          client.get<ApiResponse<Task>>(`/projects/${projectId}/tasks/${taskId}`),
          client.get<ApiResponse<{ ownerId: number }>>(`/projects/${projectId}`),
        ]);
        setTask(taskRes.data.data);
        setProjectOwnerId(projectRes.data.data.ownerId);
      } catch {
        // handled by interceptor
      } finally {
        setIsLoading(false);
      }
    })();
  }, [projectId, taskId]);


  const handleUpdate = async (formData: CreateTaskRequest) => {
    setIsSubmitting(true);
    try {
      const res = await client.put<ApiResponse<Task>>(
        `/projects/${projectId}/tasks/${taskId}`,
        formData,
      );
      setTask(res.data.data);
      setIsEditing(false);
      toast.success('Task updated');
    } catch {
      // handled by interceptor
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleStatusChange = async (
    e: React.ChangeEvent<HTMLSelectElement>,
  ) => {
    const newStatus = e.target.value as Task['status'];
    setStatusUpdating(true);
    try {
      const res = await client.patch<ApiResponse<Task>>(
        `/projects/${projectId}/tasks/${taskId}/status`,
        { status: newStatus },
      );
      setTask(res.data.data);
      toast.success(`Status changed to ${newStatus}`);
    } catch {
      // handled by interceptor
    } finally {
      setStatusUpdating(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this task permanently?')) return;
    try {
      await client.delete(`/projects/${projectId}/tasks/${taskId}`);
      toast.success('Task deleted');
      navigate(`/projects/${projectId}`);
    } catch {
      // handled by interceptor
    }
  };

  if (isLoading) {
    return (
      <Center h="200px">
        <Spinner size="xl" />
      </Center>
    );
  }

  if (!task) {
    return <Text>Task not found.</Text>;
  }

  return (
    <Box maxW="lg">
      <Link as={RouterLink} to={`/projects/${projectId}`} color="blue.500" fontSize="sm" mb={4} display="inline-block">
        ← Back to project
      </Link>

      <HStack justify="space-between" mb={6} mt={2}>
        <Heading size="lg">{task.title}</Heading>
        {isOwnerOrAdmin(projectOwnerId) && (
          <HStack spacing={2}>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setIsEditing(!isEditing)}
            >
              {isEditing ? 'Cancel' : 'Edit'}
            </Button>
            <Button
              colorScheme="red"
              variant="outline"
              size="sm"
              onClick={handleDelete}
            >
              Delete
            </Button>
          </HStack>
        )}
      </HStack>


      {isEditing ? (
        <TaskForm
          initialData={task}
          onSubmit={handleUpdate}
          isSubmitting={isSubmitting}
        />
      ) : (
        <Stack spacing={4}>
          <HStack spacing={4}>
            <StatusBadge status={task.status} />
            <PriorityTag priority={task.priority} />
          </HStack>

          <Text>
            <strong>Description:</strong>{' '}
            {task.description || 'No description'}
          </Text>
          <Text>
            <strong>Assignee:</strong> {task.assigneeUsername || 'Unassigned'}
          </Text>


          <Box>
            <Text mb={1} fontWeight="bold" fontSize="sm">
              Update status:
            </Text>
            <Select
              value={task.status}
              onChange={handleStatusChange}
              isDisabled={statusUpdating}
              w="200px"
              size="sm"
            >
              <option value="TODO">TODO</option>
              <option value="IN_PROGRESS">IN PROGRESS</option>
              <option value="DONE">DONE</option>
            </Select>
          </Box>
        </Stack>
      )}
    </Box>
  );
}
