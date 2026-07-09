import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Heading,
  HStack,
  Select,
  Table,
  Tbody,
  Td,
  Th,
  Thead,
  Tr,
  Spinner,
  Center,
  Text,
} from '@chakra-ui/react';
import toast from 'react-hot-toast';
import client from '../api/client';
import StatusBadge from '../components/StatusBadge';
import PriorityTag from '../components/PriorityTag';
import { useAuth } from '../context/AuthContext';
import type { Task, ApiResponse } from '../types';

interface PageContent<T> {
  content: T[];
  page?: { totalPages: number; totalElements: number };
  totalPages?: number;
  totalElements?: number;
}

interface TaskListPageProps {
  projectOwnerId?: number | null;
}

export default function TaskListPage({ projectOwnerId: propOwnerId }: TaskListPageProps = {}) {
  const { id: projectId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [projectOwnerId, setProjectOwnerId] = useState<number | null>(propOwnerId ?? null);

  const isOwnerOrAdmin = (ownerId: number | null): boolean => {
    if (!user) return false;
    if (user.role === 'ADMIN') return true;
    return user.id === ownerId;
  };

  const fetchTasks = useCallback(async () => {
    setIsLoading(true);
    try {
      // Only fetch project owner if not provided via prop
      let ownerId = propOwnerId ?? null;
      if (ownerId === null) {
        const projectRes = await client.get<ApiResponse<{ ownerId: number }>>(`/projects/${projectId}`);
        ownerId = projectRes.data.data.ownerId;
        setProjectOwnerId(ownerId);
      }

      const params: Record<string, string> = { page: String(page), size: '10' };

      if (statusFilter) {
        params.status = statusFilter;
      }

      const tasksRes = await client.get<ApiResponse<PageContent<Task>>>(`/projects/${projectId}/tasks`, { params });
      const d = tasksRes.data.data;
      setTasks(d.content || []);
      setTotalPages(d.page?.totalPages || d.totalPages || 0);

    } catch {
      // handled by interceptor
    } finally {
      setIsLoading(false);
    }
  }, [projectId, page, statusFilter, propOwnerId]);

  // 🐛 FIX: fetchTasks was never being called — added useEffect to trigger it
  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  if (isLoading) {
    return (
      <Center h="200px">
        <Spinner size="xl" />
      </Center>
    );
  }

  return (
    <Box>
      <HStack justify="space-between" mb={4}>
        <Heading size="md">Tasks</Heading>
        <Button
          colorScheme="blue"
          size="sm"
          onClick={() => navigate(`/projects/${projectId}/tasks/new`)}
        >
          New task
        </Button>
      </HStack>


      <HStack mb={4}>
        <Select
          placeholder="All statuses"
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value);
            setPage(0);
          }}
          w="200px"
          size="sm"
        >
          <option value="TODO">TODO</option>
          <option value="IN_PROGRESS">IN PROGRESS</option>
          <option value="DONE">DONE</option>
        </Select>
      </HStack>

      {tasks.length === 0 ? (
        <Text color="gray.500">No tasks found.</Text>
      ) : (
        <>
          <Table variant="simple" size="sm">
            <Thead>
              <Tr>
                <Th>Title</Th>
                <Th>Status</Th>
                <Th>Priority</Th>
                <Th>Assignee</Th>
                <Th>Actions</Th>
              </Tr>
            </Thead>
            <Tbody>
              {tasks.map((task) => (
                <Tr key={task.id}>
                  <Td>{task.title}</Td>
                  <Td><StatusBadge status={task.status} /></Td>
                  <Td><PriorityTag priority={task.priority} /></Td>
                  <Td>{task.assigneeUsername || '—'}</Td>
                  <Td>
                    <HStack spacing={2}>
                      {isOwnerOrAdmin(projectOwnerId) || user?.id === task.assigneeId ? (
                        <Button
                          size="xs"
                          variant="outline"
                          onClick={() =>
                            navigate(`/projects/${projectId}/tasks/${task.id}`)
                          }
                        >
                          View
                        </Button>
                      ) : (
                        <Button
                          size="xs"
                          variant="outline"
                          isDisabled
                        >
                          View
                        </Button>
                      )}
                      {isOwnerOrAdmin(projectOwnerId) && (
                        <Button
                          size="xs"
                          colorScheme="red"
                          variant="outline"
                          onClick={async () => {
                            if (!window.confirm('Delete this task permanently?')) return;
                            try {
                              await client.delete(`/projects/${projectId}/tasks/${task.id}`);
                              toast.success('Task deleted');
                              fetchTasks();
                            } catch {
                              // handled by interceptor
                            }
                          }}
                        >
                          Delete
                        </Button>
                      )}
                    </HStack>
                  </Td>
                </Tr>
              ))}
            </Tbody>
          </Table>

          <HStack justify="center" mt={4} spacing={2}>
            <Button
              size="sm"
              isDisabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
            >
              Previous
            </Button>
            <Text fontSize="sm">
              Page {page + 1} of {totalPages}
            </Text>
            <Button
              size="sm"
              isDisabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </Button>
          </HStack>
        </>
      )}
    </Box>
  );
}
