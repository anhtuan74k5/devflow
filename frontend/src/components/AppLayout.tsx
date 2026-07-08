import { Outlet, useNavigate, useLocation, Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Flex,
  HStack,
  IconButton,
  Button,
  Menu,
  MenuButton,
  MenuList,
  MenuItem,
  Text,
  VStack,
  Link,
} from '@chakra-ui/react';
import { HamburgerIcon } from '@chakra-ui/icons';
import { useAuth } from '../context/AuthContext';

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { label: 'Dashboard', path: '/' },
    { label: 'Projects', path: '/projects' },
  ];

  if (user?.role === 'ADMIN') {
    navItems.push({ label: 'System Logs', path: '/admin/logs' });
  }

  return (
    <Flex h="100vh">
      {/* Sidebar */}
      <Box
        w="240px"
        bg="black"
        color="white"
        display={{ base: 'none', md: 'block' }}
        p={4}
      >
        <Text fontSize="xl" fontWeight="bold" mb={6} px={2}>
          DevFlow
        </Text>
        <VStack align="stretch" spacing={1}>
          {navItems.map((item) => (
            <Link
              key={item.path}
              as={RouterLink}
              to={item.path}
              px={2}
              py={2}
              borderRadius="md"
              bg={location.pathname === item.path ? 'blue.600' : 'transparent'}
              _hover={{ bg: 'blue.500' }}
            >
              {item.label}
            </Link>
          ))}
        </VStack>
      </Box>

      {/* Main area */}
      <Flex direction="column" flex={1} overflow="hidden">
        {/* Header */}
        <Box borderBottomWidth={1} px={4} py={3}>
          <Flex justify="space-between" align="center">
            <HStack>
              <IconButton
                aria-label="Menu"
                icon={<HamburgerIcon />}
                display={{ base: 'inline-flex', md: 'none' }}
                variant="ghost"
              />
            </HStack>
            <HStack spacing={4}>
              <Text fontSize="sm" color="gray.400">
                {user?.role === 'ADMIN' ? 'Admin' : 'User'}
              </Text>
              <Menu>
                <MenuButton as={Button} variant="ghost" size="sm">
                  {user?.username}
                </MenuButton>
                <MenuList>
                  <MenuItem onClick={handleLogout}>Logout</MenuItem>
                </MenuList>
              </Menu>
            </HStack>
          </Flex>
        </Box>

        {/* Page content via Outlet */}
        <Box flex={1} overflowY="auto" p={6}>
          <Outlet />
        </Box>
      </Flex>
    </Flex>
  );
}
