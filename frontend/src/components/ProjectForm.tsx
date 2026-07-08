import { useState, useEffect } from 'react';
import {
  Box,
  Button,
  FormControl,
  FormLabel,
  Input,
  Textarea,
  Stack,
} from '@chakra-ui/react';
import type { CreateProjectRequest, Project } from '../types';

interface ProjectFormProps {
  initialData?: Project;
  onSubmit: (data: CreateProjectRequest) => Promise<void>;
  isSubmitting: boolean;
}

export default function ProjectForm({
  initialData,
  onSubmit,
  isSubmitting,
}: ProjectFormProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  useEffect(() => {
    if (initialData) {
      setName(initialData.name);
      setDescription(initialData.description);
    }
  }, [initialData]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({ name, description });
  };

  return (
    <Box as="form" onSubmit={handleSubmit} borderWidth={1} borderRadius="lg" p={6}>
      <Stack spacing={4}>
        <FormControl isRequired>
          <FormLabel>Project name</FormLabel>
          <Input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Enter project name"
          />
        </FormControl>
        <FormControl>
          <FormLabel>Description</FormLabel>
          <Textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Enter project description"
            rows={4}
          />
        </FormControl>
        <Button type="submit" colorScheme="blue" isLoading={isSubmitting}>
          {initialData ? 'Update' : 'Create'} project
        </Button>
      </Stack>
    </Box>
  );
}
