import { Box, Heading, Text } from '@chakra-ui/react';

interface StatCardProps {
  label: string;
  value: number | string;
  color?: string;
}

export default function StatCard({ label, value, color = 'blue.500' }: StatCardProps) {
  return (
    <Box
      borderWidth={1}
      borderRadius="lg"
      p={5}
      textAlign="center"
      borderLeftWidth={4}
      borderLeftColor={color}
    >
      <Heading size="3xl" color={color}>
        {value}
      </Heading>
      <Text mt={2} color="gray.500" fontSize="sm">
        {label}
      </Text>
    </Box>
  );
}
