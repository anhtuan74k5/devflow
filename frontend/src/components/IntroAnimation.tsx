import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Box, Text } from '@chakra-ui/react';

interface IntroAnimationProps {
  onComplete: () => void;
}

/**
 * Concept 3: "Split Reveal" — Cinematic intro animation.
 *
 * Flow:
 * 1. Two black panels (top & bottom) cover the screen.
 * 2. A horizontal line draws across the center, then "DevFlow" text fades in.
 * 3. Subtitle "Streamline Your Workflow" appears below.
 * 4. The two panels slide apart (top goes up, bottom goes down), revealing the app.
 */
export default function IntroAnimation({ onComplete }: IntroAnimationProps) {
  const [phase, setPhase] = useState<'line' | 'text' | 'subtitle' | 'split' | 'done'>('line');

  useEffect(() => {
    const timers: ReturnType<typeof setTimeout>[] = [];

    timers.push(setTimeout(() => setPhase('text'), 600));
    timers.push(setTimeout(() => setPhase('subtitle'), 1400));
    timers.push(setTimeout(() => setPhase('split'), 2200));
    timers.push(setTimeout(() => {
      setPhase('done');
      onComplete();
    }, 3500));

    return () => timers.forEach(clearTimeout);
  }, [onComplete]);

  return (
    <AnimatePresence>
      {phase !== 'done' && (
        <Box
          position="fixed"
          inset={0}
          zIndex={9999}
          overflow="hidden"
          bg="#0a0a0a"
        >
          {/* Top panel */}
          <motion.div
            initial={{ y: 0 }}
            animate={phase === 'split' ? { y: '-100%' } : { y: 0 }}
            transition={{
              duration: 1.3,
              ease: [0.7, 0, 0.3, 1],
            }}
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              width: '100%',
              height: '50vh',
              background: '#0a0a0a',
              zIndex: 2,
            }}
          />

          {/* Bottom panel */}
          <motion.div
            initial={{ y: 0 }}
            animate={phase === 'split' ? { y: '100%' } : { y: 0 }}
            transition={{
              duration: 1.3,
              ease: [0.7, 0, 0.3, 1],
            }}
            style={{
              position: 'absolute',
              bottom: 0,
              left: 0,
              width: '100%',
              height: '50vh',
              background: '#0a0a0a',
              zIndex: 2,
            }}
          />

          {/* Center content */}
          <Box
            position="absolute"
            top="50%"
            left="50%"
            transform="translate(-50%, -50%)"
            textAlign="center"
            zIndex={3}
          >
            {/* Horizontal line */}
            <motion.div
              initial={{ scaleX: 0 }}
              animate={phase === 'line' || phase === 'text' || phase === 'subtitle' || phase === 'split'
                ? { scaleX: 1 }
                : { scaleX: 0 }}
              transition={{ duration: 0.8, ease: 'easeOut' }}
              style={{
                height: 2,
                width: 120,
                background: 'linear-gradient(90deg, transparent, #63b3ed, transparent)',
                margin: '0 auto 24px',
                transformOrigin: 'center',
              }}
            />

            {/* DevFlow text */}
            <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={
                phase === 'text' || phase === 'subtitle' || phase === 'split'
                  ? { y: 0, opacity: 1 }
                  : { y: 20, opacity: 0 }
              }
              transition={{ duration: 0.6, ease: 'easeOut' }}
            >
              <Text
                fontSize="5xl"
                fontWeight="bold"
                letterSpacing="tight"
                color="white"
                userSelect="none"
              >
                Dev<span style={{ color: '#63b3ed' }}>Flow</span>
              </Text>
            </motion.div>

            {/* Subtitle */}
            <motion.div
              initial={{ y: 10, opacity: 0 }}
              animate={
                phase === 'subtitle' || phase === 'split'
                  ? { y: 0, opacity: 1 }
                  : { y: 10, opacity: 0 }
              }
              transition={{ duration: 0.5, ease: 'easeOut' }}
            >
              <Text
                fontSize="sm"
                color="gray.500"
                mt={3}
                letterSpacing="wider"
                textTransform="uppercase"
                userSelect="none"
              >
                Streamline Your Workflow
              </Text>
            </motion.div>
          </Box>
        </Box>
      )}
    </AnimatePresence>
  );
}
