import { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Container,
  FormControl,
  FormLabel,
  Heading,
  Input,
  Link,
  Stack,
  Text,
} from '@chakra-ui/react';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    setIsSubmitting(true);
    try {
      await register({ username, password });
      navigate('/dashboard');
    } catch (err: unknown) {
      const message =
        err && typeof err === 'object' && 'response' in err
          ? (err as { response: { data: { message: string } } }).response.data.message
          : 'Something went wrong';
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Container maxW="sm" py={20}>
      <Stack spacing={6}>
        <Box textAlign="center">
          <Link as={RouterLink} to="/" color="blue.500" fontSize="sm">
            &larr; Back to Home
          </Link>
        </Box>
        <Heading textAlign="center">Create an account</Heading>
        <Box as="form" onSubmit={handleSubmit} borderWidth={1} borderRadius="lg" p={6}>
          <Stack spacing={4}>
            <FormControl isRequired>
              <FormLabel>Username</FormLabel>
              <Input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Choose a username"
              />
            </FormControl>
            <FormControl isRequired>
              <FormLabel>Password</FormLabel>
              <Input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Choose a password"
              />
            </FormControl>
            <FormControl isRequired>
              <FormLabel>Confirm password</FormLabel>
              <Input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Confirm your password"
              />
            </FormControl>
            <Button type="submit" colorScheme="blue" isLoading={isSubmitting}>
              Register
            </Button>
          </Stack>
        </Box>
        <Text textAlign="center">
          Already have an account?{' '}
          <Link as={RouterLink} to="/login" color="blue.500">
            Sign in
          </Link>
        </Text>
      </Stack>
    </Container>
  );
}
