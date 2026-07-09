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
  Divider,
  Link,
} from '@chakra-ui/react';
import toast from 'react-hot-toast';
import client from '../api/client';
import ProjectForm from '../components/ProjectForm';
import TaskListPage from './TaskListPage';
import { useAuth } from '../context/AuthContext';
import type { Project, CreateProjectRequest, ApiResponse } from '../types';


export default function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();


  const [project, setProject] = useState<Project | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isOwnerOrAdmin = (ownerId: number): boolean => {
    if (!user) return false;
    if (user.role === 'ADMIN') return true;
    return user.id === ownerId;
  };

  useEffect(() => {
    (async () => {
      try {
        const res = await client.get<ApiResponse<Project>>(`/projects/${id}`);
        setProject(res.data.data);
      } catch {
        // handled by interceptor
      } finally {
        setIsLoading(false);
      }
    })();
  }, [id]);

  const handleUpdate = async (formData: CreateProjectRequest) => {
    setIsSubmitting(true);
    try {
      const res = await client.put<ApiResponse<Project>>(`/projects/${id}`, formData);
      setProject(res.data.data);
      setIsEditing(false);
      toast.success('Project updated');
    } catch {
      // handled by interceptor
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this project permanently?')) return;
    try {
      await client.delete(`/projects/${id}`);
      toast.success('Project deleted');
      navigate('/projects');
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

  if (!project) {
    return <Text>Project not found.</Text>;
  }

  return (
    <Box maxW="4xl" mx="auto">
      <HStack mb={4}>
        {user?.role === 'ADMIN' && (
          <Button
            as={RouterLink}
            to="/admin/logs"
            variant="ghost"
            size="sm"
            colorScheme="blue"
          >
            ← Back to System Logs
          </Button>
        )}
      </HStack>

      <HStack justify="space-between" mb={6}>
        <Heading size="lg">{project.name}</Heading>
        {isOwnerOrAdmin(project.ownerId) && (
          <HStack spacing={2}>
            <Button
              variant="outline"
              onClick={() => setIsEditing(!isEditing)}
            >
              {isEditing ? 'Cancel' : 'Edit'}
            </Button>
            <Button colorScheme="red" variant="outline" onClick={handleDelete}>
              Delete
            </Button>
          </HStack>
        )}
      </HStack>


      {isEditing ? (
        <ProjectForm
          initialData={project}
          onSubmit={handleUpdate}
          isSubmitting={isSubmitting}
        />
      ) : (
        <Stack spacing={3}>
          <Text>
            <strong>Description:</strong>
          </Text>
          <Text whiteSpace="pre-wrap" pl={4}>
            {project.description || 'No description'}
          </Text>
          <Text>
            <strong>Owner:</strong> {project.ownerUsername}
          </Text>

        </Stack>
      )}

      <Divider my={8} />

      {isOwnerOrAdmin(project.ownerId) && (
        <HStack mb={4} spacing={4}>
          <Link as={RouterLink} to={`/projects/${id}/activities`} color="blue.500" fontSize="sm">
            View activity feed →
          </Link>
        </HStack>
      )}

      <TaskListPage projectOwnerId={project.ownerId} />
    </Box>

  );
}
