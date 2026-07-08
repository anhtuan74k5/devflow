import { useState } from 'react';
import { useParams, useNavigate, Link as RouterLink } from 'react-router-dom';
import { Box, Heading, Link } from '@chakra-ui/react';
import TaskForm from '../components/TaskForm';
import client from '../api/client';
import type { CreateTaskRequest } from '../types';

export default function CreateTaskPage() {
  const { id: projectId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (data: CreateTaskRequest) => {
    setIsSubmitting(true);
    try {
      await client.post(`/projects/${projectId}/tasks`, data);
      navigate(`/projects/${projectId}`);
    } catch {
      // handled by interceptor
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box maxW="lg">
      <Link as={RouterLink} to={`/projects/${projectId}`} color="blue.500" fontSize="sm" mb={4} display="inline-block">
        ← Back to project
      </Link>
      <Heading size="lg" mb={6}>
        Create task
      </Heading>
      <TaskForm onSubmit={handleSubmit} isSubmitting={isSubmitting} />
    </Box>
  );
}
