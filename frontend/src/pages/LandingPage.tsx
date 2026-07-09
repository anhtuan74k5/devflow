import { Box, Heading, Text, Button, VStack, SimpleGrid, Flex } from '@chakra-ui/react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

const features = [
  {
    title: 'Project Management',
    desc: 'Create, organize, and manage projects with ease. Full CRUD operations with owner-based access control.',
    icon: '📁',
  },
  {
    title: 'Task Tracking',
    desc: 'Track tasks from TODO to DONE. Assign team members, set priorities, and filter by status.',
    icon: '✅',
  },
  {
    title: 'Activity Feed',
    desc: 'Automatic activity logging via AOP. Every status change is recorded — like a social feed for your projects.',
    icon: '📊',
  },
  {
    title: 'Role-Based Access',
    desc: 'Granular permissions with USER and ADMIN roles. Admins manage everything; users own their projects.',
    icon: '🔐',
  },
  {
    title: 'Rich Text Editor',
    desc: 'Built-in EditorJS for task descriptions. Support for headers, lists, checklists, tables, code blocks, and more.',
    icon: '✍️',
  },
  {
    title: 'Real-Time Updates',
    desc: 'Instant status updates with visual badges and priority tags. Smooth animations with Framer Motion.',
    icon: '⚡',
  },
];

export default function LandingPage() {
  const navigate = useNavigate();

  return (
    <Box minH="100vh" bg="white">
      {/* Hero Section */}
      <Flex
        direction="column"
        align="center"
        justify="center"
        bg="linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 100%)"
        color="white"
        py={20}
        px={4}
      >
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: 'easeOut' }}
          style={{ width: '100%', maxWidth: '1024px', textAlign: 'center' }}
        >
          <Heading as="h1" fontSize={{ base: '4xl', md: '6xl' }} fontWeight="bold" mb={4}>
            Dev<span style={{ color: '#63b3ed' }}>Flow</span>
          </Heading>
          <Text fontSize={{ base: 'lg', md: 'xl' }} color="gray.400" mb={2}>
            Streamline Your Workflow
          </Text>
          <Text fontSize={{ base: 'md', md: 'lg' }} color="gray.500" maxW="xl" mx="auto" mb={10}>
            A modern, full-stack task management platform for development teams.
            Track projects, assign tasks, and monitor activity — all in one place.
          </Text>
          <Flex justify="center" gap={4} wrap="wrap">
            <Button
              size="lg"
              colorScheme="blue"
              px={8}
              onClick={() => navigate('/login')}
            >
              Sign In
            </Button>
            <Button
              size="lg"
              variant="outline"
              colorScheme="blue"
              px={8}
              onClick={() => navigate('/register')}
            >
              Sign Up
            </Button>
          </Flex>
        </motion.div>
      </Flex>

      {/* Features Section */}
      <Flex direction="column" align="center" py={20} px={4}>
        <Box maxW="1024px" w="full">
          <VStack spacing={12}>
            <Box textAlign="center">
              <Heading as="h2" size="xl" mb={2} color="gray.800">
                Everything you need to manage your projects
              </Heading>
              <Text color="gray.500" fontSize="lg">
                Built for developers, designed for teams
              </Text>
            </Box>

            <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} spacing={8} w="full">
              {features.map((feature, index) => (
                <motion.div
                  key={feature.title}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.5, delay: 0.1 * index }}
                  style={{ border: '1px solid', borderColor: '#e2e8f0', borderRadius: '12px', padding: '24px' }}
                  whileHover={{ boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}
                >
                  <Text fontSize="3xl" mb={3}>{feature.icon}</Text>
                  <Heading as="h3" size="md" mb={2} color="gray.800">
                    {feature.title}
                  </Heading>
                  <Text color="gray.600" fontSize="sm" lineHeight="tall">
                    {feature.desc}
                  </Text>
                </motion.div>
              ))}
            </SimpleGrid>
          </VStack>
        </Box>
      </Flex>

      {/* Tech Stack Section */}
      <Flex direction="column" align="center" bg="gray.50" py={16} px={4}>
        <Box maxW="1024px" w="full" textAlign="center">
          <Heading as="h2" size="xl" mb={8} color="gray.800">
            Built with modern technology
          </Heading>
          <SimpleGrid columns={{ base: 2, md: 4 }} spacing={6}>
            {[
              { name: 'Java 21', color: 'red.500' },
              { name: 'Spring Boot 3', color: 'green.500' },
              { name: 'React 18', color: 'blue.500' },
              { name: 'PostgreSQL', color: 'purple.500' },
              { name: 'TypeScript', color: 'blue.600' },
              { name: 'Chakra UI', color: 'teal.500' },
              { name: 'Docker', color: 'cyan.500' },
              { name: 'Framer Motion', color: 'pink.500' },
            ].map((tech) => (
              <Box
                key={tech.name}
                p={4}
                bg="white"
                borderRadius="lg"
                borderWidth={1}
                borderColor="gray.200"
              >
                <Text fontWeight="bold" color={tech.color}>
                  {tech.name}
                </Text>
              </Box>
            ))}
          </SimpleGrid>
        </Box>
      </Flex>

      {/* CTA Section */}
      <Flex direction="column" align="center" justify="center" py={16} px={4}>
        <Box maxW="1024px" w="full" textAlign="center">
          <Heading as="h2" size="xl" mb={4} color="gray.800">
            Ready to streamline your workflow?
          </Heading>
          <Text color="gray.500" mb={8} fontSize="lg">
            Get started in seconds. No credit card required.
          </Text>
          <Flex justify="center" gap={4} wrap="wrap">
            <Button
              size="lg"
              colorScheme="blue"
              px={8}
              onClick={() => navigate('/register')}
            >
              Get Started Free
            </Button>
            <Button
              size="lg"
              variant="outline"
              colorScheme="blue"
              px={8}
              onClick={() => navigate('/login')}
            >
              Sign In
            </Button>
          </Flex>
        </Box>
      </Flex>

      {/* Footer */}
      <Flex direction="column" align="center" borderTopWidth={1} borderColor="gray.200" py={8} px={4}>
        <Box maxW="1024px" w="full" textAlign="center">
          <Text color="gray.500" fontSize="sm">
            Built with ❤️ using Spring Boot & React &copy; {new Date().getFullYear()} DevFlow
          </Text>
        </Box>
      </Flex>
    </Box>
  );
}
