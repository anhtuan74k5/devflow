import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Heading } from '@chakra-ui/react';
import ProjectForm from '../components/ProjectForm';
import client from '../api/client';
import type { CreateProjectRequest } from '../types';

export default function CreateProjectPage() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (data: CreateProjectRequest) => {
    setIsSubmitting(true);
    try {
      const response = await client.post('/projects', data);
      navigate(`/projects/${response.data.data.id}`);
    } catch {
      // error handled by interceptor
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box maxW="lg">
      <Heading size="lg" mb={6}>
        Create project
      </Heading>
      <ProjectForm onSubmit={handleSubmit} isSubmitting={isSubmitting} />
    </Box>
  );
}
