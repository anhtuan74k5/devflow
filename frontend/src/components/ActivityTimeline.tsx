import {
  Box,
  VStack,
  HStack,
  Text,
  Circle,
  Divider,
  Spinner,
  Center,
} from '@chakra-ui/react';
import { formatUTCDate } from '../utils/formatDate';
import type { ActivityLog } from '../types';

interface ActivityTimelineProps {
  activities: ActivityLog[];
  isLoading: boolean;
}

export default function ActivityTimeline({
  activities,
  isLoading,
}: ActivityTimelineProps) {
  if (isLoading) {
    return (
      <Center h="100px">
        <Spinner size="md" />
      </Center>
    );
  }

  if (activities.length === 0) {
    return (
      <Text color="gray.500" textAlign="center" py={4}>
        No activity yet.
      </Text>
    );
  }

  return (
    <VStack align="stretch" spacing={0}>
      {activities.map((activity, index) => (
        <HStack key={activity.id} spacing={4} align="flex-start">
          <VStack spacing={0} align="center">
            <Circle size={3} bg="blue.400" />
            {index < activities.length - 1 && (
              <Divider orientation="vertical" borderColor="gray.300" h="100%" />
            )}
          </VStack>
          <Box pb={6}>
            <Text fontSize="sm">{activity.content}</Text>
            <Text fontSize="xs" color="gray.500" mt={1}>
              {formatUTCDate(activity.createdAt)}
            </Text>
          </Box>
        </HStack>
      ))}
    </VStack>
  );
}
