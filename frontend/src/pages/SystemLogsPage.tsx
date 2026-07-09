import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Heading,
  Button,
  HStack,
  Text,
  VStack,
  Spinner,
  Center,
} from '@chakra-ui/react';
import client from '../api/client';
import { formatUTCDate } from '../utils/formatDate';
import type { ActivityLog, ApiResponse } from '../types';

interface PageContent<T> {
  content: T[];
  page?: { totalPages: number; totalElements: number };
  totalPages?: number;
  totalElements?: number;
}

export default function SystemLogsPage() {
  const navigate = useNavigate();
  const [logs, setLogs] = useState<ActivityLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchLogs = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await client.get<ApiResponse<PageContent<ActivityLog>>>('/admin/activities', {
        params: { page, size: 20, sort: 'createdAt,desc' },
      });
      const d = res.data.data;
      setLogs(d.content || []);
      setTotalPages(d.page?.totalPages || d.totalPages || 0);
    } catch {
      // handled by interceptor
    } finally {
      setIsLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  if (isLoading) {
    return (
      <Center h="200px">
        <Spinner size="xl" />
      </Center>
    );
  }

  return (
    <Box maxW="6xl" mx="auto">
      <Heading size="lg" mb={6}>
        System Activity Logs
      </Heading>

      {logs.length === 0 ? (
        <Text color="gray.500">No system activity yet.</Text>
      ) : (
        <>
          <VStack align="stretch" spacing={3}>
            {logs.map((log) => (
              <Box
                key={log.id}
                borderWidth={1}
                borderRadius="md"
                p={3}
                cursor="pointer"
                _hover={{ bg: 'gray.50', borderColor: 'blue.300' }}
                onClick={() => navigate(`/projects/${log.projectId}`)}
                role="group"
                sx={{
                  '&:hover .log-content': { color: 'gray.800' },
                  '&:hover .log-project': { color: 'blue.700' },
                  '&:hover .log-date': { color: 'gray.700' },
                }}
              >
                <Text className="log-content" fontSize="sm" color="whiteAlpha.900">
                  {log.content}
                </Text>
                <HStack mt={1} spacing={4}>
                  <Text className="log-project" fontSize="xs" color="blue.300" fontWeight="medium">
                    Project #{log.projectId}
                  </Text>
                  <Text className="log-date" fontSize="xs" color="gray.400">
                    {formatUTCDate(log.createdAt)}
                  </Text>
                </HStack>
              </Box>
            ))}
          </VStack>

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
