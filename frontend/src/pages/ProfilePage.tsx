import { Box, Heading, VStack, Text, Divider } from '@chakra-ui/react';
import { useAuth } from '../context/AuthContext';

export default function ProfilePage() {
  const { user } = useAuth();

  if (!user) {
    return <Text>Not logged in.</Text>;
  }

  return (
    <Box maxW="lg" mx="auto">
      <Heading size="lg" mb={6}>
        Profile
      </Heading>
      <Box borderWidth={1} borderRadius="lg" p={6}>
        <VStack align="stretch" spacing={4}>
          <Box>
            <Text fontWeight="bold" fontSize="sm" color="gray.500">
              ID
            </Text>
            <Text fontSize="md">{user.id}</Text>
          </Box>
          <Divider />
          <Box>
            <Text fontWeight="bold" fontSize="sm" color="gray.500">
              Username
            </Text>
            <Text fontSize="md">{user.username}</Text>
          </Box>
          <Divider />
          <Box>
            <Text fontWeight="bold" fontSize="sm" color="gray.500">
              Role
            </Text>
            <Text fontSize="md">{user.role}</Text>
          </Box>
        </VStack>
      </Box>
    </Box>
  );
}
