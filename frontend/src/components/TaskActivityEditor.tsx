import { useEffect, useRef, useState } from 'react';
import {
  Box,
  Button,
  VStack,
  Text,
  Spinner,
  Center,
  Divider,
  Heading,
  useToast,
} from '@chakra-ui/react';
import EditorJS from '@editorjs/editorjs';
import Header from '@editorjs/header';
import List from '@editorjs/list';
import Paragraph from '@editorjs/paragraph';
import Quote from '@editorjs/quote';
import CodeTool from '@editorjs/code';
import Delimiter from '@editorjs/delimiter';
import InlineCode from '@editorjs/inline-code';
import Table from '@editorjs/table';
import Underline from '@editorjs/underline';
import client from '../api/client';
import type { ApiResponse } from '../types';

// @ts-expect-error - editorjs tools without type declarations
import Checklist from '@editorjs/checklist';
// @ts-expect-error - editorjs tools without type declarations
import Marker from '@editorjs/marker';

interface TaskActivityData {
  id: number;
  taskId: number;
  content: string;
  editorType: string;
  createdAt: string;
}

interface Props {
  projectId: string;
  taskId: string;
}

// Inject global CSS for EditorJS marker and selection styles
const editorStylesId = 'editorjs-custom-styles';
if (typeof document !== 'undefined' && !document.getElementById(editorStylesId)) {
  const style = document.createElement('style');
  style.id = editorStylesId;
  style.textContent = `
    /* Fix marker highlight: black text on yellow background */
    .cdx-marker {
      background: rgba(245,235,111,0.29) !important;
      padding: 3px 0 !important;
      color: #000 !important;
    }
    /* Fix text selection color inside editor */
    .codex-editor ::selection {
      background: #b3d4fc !important;
      color: #000 !important;
    }
    .codex-editor mark {
      background: rgba(245,235,111,0.29) !important;
      color: #000 !important;
    }
    /* Fix inline code style */
    .inline-code {
      background: rgba(250,239,240,0.78) !important;
      color: #b44437 !important;
      padding: 2px 4px !important;
      border-radius: 3px !important;
      font-family: monospace !important;
      font-size: 0.9em !important;
    }
  `;
  document.head.appendChild(style);
}

export default function TaskActivityEditor({ projectId, taskId }: Props) {
  const editorRef = useRef<EditorJS | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const toast = useToast();
  const [isSaving, setIsSaving] = useState(false);
  const [activities, setActivities] = useState<TaskActivityData[]>([]);
  const [isLoadingActivities, setIsLoadingActivities] = useState(true);

  useEffect(() => {
    loadActivities();
  }, [projectId, taskId]);

  const loadActivities = async () => {
    setIsLoadingActivities(true);
    try {
      const res = await client.get<ApiResponse<{ content: TaskActivityData[] }>>(
        `/projects/${projectId}/tasks/${taskId}/activities`,
      );
      setActivities(res.data.data.content || []);
    } catch {
      // handled by interceptor
    } finally {
      setIsLoadingActivities(false);
    }
  };

  useEffect(() => {
    if (editorRef.current) return;

    const editor = new EditorJS({
      holder: 'task-activity-editor',
      tools: {
        header: {
          class: Header as any,
          inlineToolbar: true,
          config: { levels: [1, 2, 3, 4, 5, 6], defaultLevel: 3 },
        },
        paragraph: {
          class: Paragraph as any,
          inlineToolbar: true,
        },
        list: {
          class: List as any,
          inlineToolbar: true,
        },
        checklist: {
          class: Checklist as any,
          inlineToolbar: true,
        },
        quote: {
          class: Quote as any,
          inlineToolbar: true,
        },
        code: CodeTool as any,
        delimiter: Delimiter as any,
        inlineCode: InlineCode as any,
        table: {
          class: Table as any,
          inlineToolbar: true,
        },
        marker: {
          class: Marker as any,
          inlineToolbar: true,
        },
        underline: Underline as any,
      },
      placeholder: 'Write your activity report here...',
      autofocus: true,
      onReady: () => {
        // Make toolbar always visible on desktop by simulating a click
        const editorWrapper = document.getElementById('task-activity-editor');
        if (editorWrapper) {
          editorWrapper.click();
        }
      },
    });

    editorRef.current = editor;

    return () => {
      if (editorRef.current) {
        try {
          const destroyResult = (editorRef.current.destroy() as unknown) as Promise<void> | undefined;
          if (destroyResult && typeof destroyResult.then === 'function') {
            destroyResult.then(() => {
              editorRef.current = null;
            });
          } else {
            editorRef.current = null;
          }
        } catch {
          editorRef.current = null;
        }
      }
    };
  }, []);

  const handleSave = async () => {
    if (!editorRef.current) return;

    setIsSaving(true);
    try {
      const output = await editorRef.current.save();
      const content = JSON.stringify(output);

      await client.post(`/projects/${projectId}/tasks/${taskId}/activities`, {
        content,
        editorType: 'editorjs',
      });

      toast({
        title: 'Activity saved',
        description: 'Status changed to IN PROGRESS',
        status: 'success',
        duration: 3000,
      });

      // Clear editor
      editorRef.current.clear();

      // Reload activities
      await loadActivities();
    } catch {
      // handled by interceptor
    } finally {
      setIsSaving(false);
    }
  };

  const renderActivityContent = (content: string) => {
    try {
      const data = JSON.parse(content);
      if (data && data.blocks && Array.isArray(data.blocks) && data.blocks.length > 0) {
        const elements = data.blocks.map((block: any, idx: number) => {
          if (!block || !block.type) return null;
          switch (block.type) {
            case 'header':
              const level = block.data?.level || 3;
              return (
                <Text
                  key={idx}
                  fontSize={level === 1 ? '2xl' : level === 2 ? 'xl' : 'lg'}
                  fontWeight="bold"
                  mb={2}
                >
                  {block.data?.text || ''}
                </Text>
              );
            case 'paragraph':
              return (
                <Text key={idx} mb={2}>
                  {block.data?.text || ''}
                </Text>
              );
            case 'list':
              const ListTag = block.data?.style === 'ordered' ? 'ol' : 'ul';
              return (
                <Box as={ListTag} pl={4} mb={2} key={idx}>
                  {(block.data?.items || []).map((item: string, i: number) => (
                    <Box as="li" key={i}>
                      {item}
                    </Box>
                  ))}
                </Box>
              );
            case 'checklist':
              return (
                <Box key={idx} mb={2}>
                  {(block.data?.items || []).map((item: any, i: number) => {
                    const checked = item.checked === true;
                    const text = item.text || item.content || '';
                    return (
                      <Text key={i}>
                        {checked ? '☑' : '☐'} {text}
                      </Text>
                    );
                  })}
                </Box>
              );
            case 'quote':
              return (
                <Box
                  key={idx}
                  borderLeft="4px"
                  borderColor="blue.400"
                  pl={3}
                  py={2}
                  mb={2}
                  fontStyle="italic"
                  bg="gray.50"
                  _dark={{ bg: 'gray.700' }}
                >
                  <Text>{block.data?.text || ''}</Text>
                  {block.data?.caption && (
                    <Text fontSize="sm" color="gray.500" mt={1}>
                      — {block.data.caption}
                    </Text>
                  )}
                </Box>
              );
            case 'code':
              return (
                <Box
                  key={idx}
                  as="pre"
                  bg="gray.100"
                  _dark={{ bg: 'gray.800' }}
                  p={3}
                  borderRadius="md"
                  fontSize="sm"
                  overflowX="auto"
                  mb={2}
                  fontFamily="monospace"
                >
                  <code>{block.data?.code || ''}</code>
                </Box>
              );
            case 'delimiter':
              return <Divider key={idx} my={3} />;
            case 'table':
              return (
                <Box key={idx} overflowX="auto" mb={2}>
                  <Box
                    as="table"
                    borderWidth="1px"
                    width="100%"
                    sx={{ borderCollapse: 'collapse' }}
                  >
                    <tbody>
                      {(block.data?.content || []).map((row: string[], ri: number) => (
                        <Box as="tr" key={ri}>
                          {(row || []).map((cell: string, ci: number) => (
                            <Box
                              as="td"
                              key={ci}
                              borderWidth="1px"
                              p={2}
                              fontSize="sm"
                            >
                              {cell || ''}
                            </Box>
                          ))}
                        </Box>
                      ))}
                    </tbody>
                  </Box>
                </Box>
              );
            default:
              return null;
          }
        });
        // Filter out nulls and ensure we have at least one element
        const validElements = elements.filter(Boolean);
        if (validElements.length > 0) {
          return <>{validElements}</>;
        }
      }
    } catch {
      // If not valid JSON, show as plain text
    }
    return <Text whiteSpace="pre-wrap">{content || ''}</Text>;
  };

  return (
    <VStack spacing={6} align="stretch" w="full">
      {/* Editor */}
      <Box>
        <Text fontWeight="bold" fontSize="sm" mb={2}>
          Activity Report:
        </Text>
        <Box
          ref={containerRef}
          id="task-activity-editor"
          borderWidth="1px"
          borderRadius="md"
          p={4}
          minH="200px"
          bg="white"
          _dark={{ bg: 'gray.800' }}
          sx={{
            '.ce-block__content': { maxWidth: '100%' },
            '.ce-toolbar__content': { maxWidth: '100%' },
            // Fix marker highlight: black text on yellow background
            '.cdx-marker': {
              background: 'rgba(245,235,111,0.29)',
              padding: '3px 0',
              color: '#000 !important',
            },
            // Fix inline code style
            '.inline-code': {
              background: 'rgba(250,239,240,0.78)',
              color: '#b44437',
              padding: '2px 4px',
              borderRadius: '3px',
              fontFamily: 'monospace',
              fontSize: '0.9em',
            },
            // Ensure toolbar is visible on desktop
            '.ce-toolbar': {
              opacity: '1 !important',
              visibility: 'visible !important',
            },
            '.ce-toolbar__plus': {
              opacity: '1 !important',
              visibility: 'visible !important',
            },
            '.ce-toolbar__settings-btn': {
              opacity: '1 !important',
              visibility: 'visible !important',
            },
          }}
        />
        <Button
          mt={3}
          colorScheme="blue"
          onClick={handleSave}
          isLoading={isSaving}
          loadingText="Saving..."
          size="sm"
        >
          Save Activity
        </Button>
      </Box>

      <Divider />

      {/* Previous Activities */}
      <Box>
        <Heading size="sm" mb={4}>
          Previous Activities
        </Heading>
        {isLoadingActivities ? (
          <Center py={4}>
            <Spinner size="sm" />
          </Center>
        ) : activities.length === 0 ? (
          <Text color="gray.500" fontSize="sm">
            No activities yet.
          </Text>
        ) : (
          <VStack spacing={4} align="stretch">
            {activities.map((activity) => (
              <Box
                key={activity.id}
                borderWidth="1px"
                borderRadius="md"
                p={4}
                bg="gray.50"
                _dark={{ bg: 'gray.700' }}
              >
                <Text fontSize="xs" color="gray.500" mb={2}>
                  {new Date(activity.createdAt).toLocaleString()}
                </Text>
                {renderActivityContent(activity.content)}
              </Box>
            ))}
          </VStack>
        )}
      </Box>
    </VStack>
  );
}
