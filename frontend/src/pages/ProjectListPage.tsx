import { useState, useEffect, useCallback } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Heading,
  HStack,
  Link,
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
import { useAuth } from '../context/AuthContext';
import type { Project, ApiResponse } from '../types';

interface PageResponse<T> {
  content: T[];
  page?: { totalPages: number };
  totalPages?: number;
}

function extractPageInfo<T>(resp: ApiResponse<PageResponse<T>>): { content: T[]; totalPages: number } {
  const d = resp.data;
  if (d.page) {
    return { content: d.content, totalPages: d.page.totalPages };
  }
  // fallback: direct fields
  return { content: d.content || [], totalPages: d.totalPages || 0 };
}

export default function ProjectListPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const navigate = useNavigate();
  const { user } = useAuth();

  const isOwnerOrAdmin = (ownerId: number): boolean => {
    if (!user) return false;
    if (user.role === 'ADMIN') return true;
    return user.id === ownerId;
  };

  const fetchProjects = useCallback(async () => {
    setIsLoading(true);
    try {
      const { data } = await client.get<ApiResponse<PageResponse<Project>>>('/projects', {
        params: { page, size: 10, sort: 'id,desc' },
      });

      const info = extractPageInfo<Project>(data);
      setProjects(info.content);
      setTotalPages(info.totalPages);
    } catch {
      // error handled by interceptor
    } finally {
      setIsLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchProjects();
  }, [fetchProjects]);

  if (isLoading) {
    return (
      <Center h="200px">
        <Spinner size="xl" />
      </Center>
    );
  }

  return (
    <Box>
      <HStack justify="space-between" mb={6}>
        <Heading size="lg">Projects</Heading>
        <Button colorScheme="blue" onClick={() => navigate('/projects/new')}>
          New project
        </Button>
      </HStack>

      {projects.length === 0 ? (
        <Text color="gray.500">No projects yet. Create your first project.</Text>
      ) : (
        <>
          <Table variant="simple">
            <Thead>
              <Tr>
                <Th>Name</Th>
                <Th>Description</Th>
                <Th>Owner</Th>
                <Th>Actions</Th>
              </Tr>
            </Thead>
            <Tbody>
              {projects.map((project) => (
                <Tr key={project.id}>
                  <Td>
                    <Link
                      as={RouterLink}
                      to={`/projects/${project.id}`}
                      color="blue.500"
                    >
                      {project.name}
                    </Link>
                  </Td>
                  <Td>{project.description}</Td>
                  <Td>{project.ownerUsername}</Td>
                  <Td>
                    <HStack spacing={2}>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => navigate(`/projects/${project.id}`)}
                      >
                        View
                      </Button>
                      {isOwnerOrAdmin(project.ownerId) && (
                        <Button
                          size="sm"
                          colorScheme="red"
                          variant="outline"
                          onClick={async () => {
                            if (!window.confirm('Delete this project permanently?')) return;
                            try {
                              await client.delete(`/projects/${project.id}`);
                              toast.success('Project deleted');
                              fetchProjects();
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
