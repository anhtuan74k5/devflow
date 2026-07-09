import { useState, useEffect } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Heading,
  SimpleGrid,
  Link,
  Spinner,
  Center,
  Text,
  VStack,
} from '@chakra-ui/react';
import client from '../api/client';
import StatCard from '../components/StatCard';
import type { Project, ApiResponse } from '../types';

interface ProjectStats {
  projectId: number;
  projectName: string;
  total: number;
  todo: number;
  inProgress: number;
  done: number;
}

interface PageContent<T> {
  content: T[];
  page?: { totalPages: number; totalElements: number };
  totalPages?: number;
  totalElements?: number;
}

export default function DashboardPage() {
  const [stats, setStats] = useState<ProjectStats[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const projectsRes = await client.get<ApiResponse<PageContent<Project>>>('/projects', {
          params: { page: 0, size: 100, sort: 'id,desc' },
        });

        const projects: Project[] = projectsRes.data.data.content || [];

        const projectStats = await Promise.all(
          projects.map(async (project) => {
            const [todoRes, inProgressRes, doneRes] = await Promise.all([
              client.get<ApiResponse<PageContent<unknown>>>(`/projects/${project.id}/tasks`, {
                params: { status: 'TODO', page: 0, size: 1 },
              }),
              client.get<ApiResponse<PageContent<unknown>>>(`/projects/${project.id}/tasks`, {
                params: { status: 'IN_PROGRESS', page: 0, size: 1 },
              }),
              client.get<ApiResponse<PageContent<unknown>>>(`/projects/${project.id}/tasks`, {
                params: { status: 'DONE', page: 0, size: 1 },
              }),
            ]);

            const getTotal = (resp: { data: ApiResponse<PageContent<unknown>> }): number =>
              resp.data.data.page?.totalElements ||
              resp.data.data.totalElements ||
              0;
            return {
              projectId: project.id,
              projectName: project.name,
              total:
                getTotal(todoRes) +
                getTotal(inProgressRes) +
                getTotal(doneRes),
              todo: getTotal(todoRes),
              inProgress: getTotal(inProgressRes),
              done: getTotal(doneRes),
            };

          }),
        );

        setStats(projectStats);
      } catch {
        // handled by interceptor
      } finally {
        setIsLoading(false);
      }
    })();
  }, []);

  if (isLoading) {
    return (
      <Center h="200px">
        <Spinner size="xl" />
      </Center>
    );
  }

  const totals = stats.reduce(
    (acc, s) => ({
      total: acc.total + s.total,
      todo: acc.todo + s.todo,
      inProgress: acc.inProgress + s.inProgress,
      done: acc.done + s.done,
    }),
    { total: 0, todo: 0, inProgress: 0, done: 0 },
  );

  return (
    <Box maxW="6xl" mx="auto">
      <Heading size="lg" mb={6}>
        Dashboard
      </Heading>

      <SimpleGrid columns={{ base: 1, md: 4 }} spacing={4} mb={8}>
        <StatCard label="Total tasks" value={totals.total} color="blue.500" />
        <StatCard label="TODO" value={totals.todo} color="gray.500" />
        <StatCard label="In progress" value={totals.inProgress} color="orange.400" />
        <StatCard label="Done" value={totals.done} color="green.500" />
      </SimpleGrid>

      {stats.length === 0 ? (
        <Text color="gray.500">No projects yet.</Text>
      ) : (
        <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} spacing={4}>
          {stats.map((s) => (
            <Box key={s.projectId} borderWidth={1} borderRadius="lg" p={4}>
              <Link
                as={RouterLink}
                to={`/projects/${s.projectId}`}
                fontWeight="bold"
                color="blue.500"
              >
                {s.projectName}
              </Link>
              <VStack align="stretch" mt={3} spacing={1} fontSize="sm">
                <Text>Total: {s.total}</Text>
                <Text color="gray.500">TODO: {s.todo}</Text>
                <Text color="orange.400">In progress: {s.inProgress}</Text>
                <Text color="green.500">Done: {s.done}</Text>
              </VStack>
            </Box>
          ))}
        </SimpleGrid>
      )}
    </Box>
  );
}
