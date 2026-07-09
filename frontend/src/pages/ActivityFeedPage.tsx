import { useState, useEffect, useCallback } from 'react';
import { useParams, Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Heading,
  Button,
  HStack,
  Text,
  Link,
} from '@chakra-ui/react';
import client from '../api/client';
import ActivityTimeline from '../components/ActivityTimeline';
import type { ActivityLog, ApiResponse } from '../types';

interface PageContent<T> {
  content: T[];
  page?: { totalPages: number; totalElements: number };
  totalPages?: number;
  totalElements?: number;
}

export default function ActivityFeedPage() {
  const { id: projectId } = useParams<{ id: string }>();

  const [activities, setActivities] = useState<ActivityLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchActivities = useCallback(async () => {
    if (!projectId) return;
    setIsLoading(true);
    try {
      const res = await client.get<ApiResponse<PageContent<ActivityLog>>>(`/projects/${projectId}/activities`, {
        params: { page, size: 20, sort: 'createdAt,desc' },
      });
      const d = res.data.data;
      setActivities(d.content || []);
      setTotalPages(d.page?.totalPages || d.totalPages || 0);

    } catch {
      // handled by interceptor
    } finally {
      setIsLoading(false);
    }
  }, [projectId, page]);

  useEffect(() => {
    fetchActivities();
  }, [fetchActivities]);

  if (!projectId) {
    return (
      <Box maxW="4xl" mx="auto">
        <Heading size="lg" mb={6}>
          Activity feed
        </Heading>
        <Text color="gray.500">Please select a project to view its activity feed.</Text>
      </Box>
    );
  }

  return (
    <Box maxW="4xl" mx="auto">
      <Link as={RouterLink} to={`/projects/${projectId}`} color="blue.500" fontSize="sm" mb={4} display="inline-block">
        ← Back to project
      </Link>

      <Heading size="lg" mb={6}>
        Activity feed
      </Heading>

      <ActivityTimeline activities={activities} isLoading={isLoading} />

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
    </Box>
  );
}
